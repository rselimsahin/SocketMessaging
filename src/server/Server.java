package server;

import database.DatabaseManager;
import models.User;
import models.Message;

import javax.jws.soap.SOAPBinding;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.Timestamp;
import java.util.Date;

/**
 *   Server class implemented as threads. With every client connected a new Server thread is started.
 *   When client disconnects from server via sending "exit" command Server thread terminates.
 *
 */
public class Server extends Thread{
    private static ServerSocket serverSocket;
    private Socket clientSocket;
    private Server(Socket clientSocket){
        this.clientSocket=clientSocket;
        // If you need timeout on your server uncomment next line
        //serverSocket.setSoTimeout(10000);
    }

    public void run() {

            try {

                System.out.println("Connected to: "+ clientSocket.getRemoteSocketAddress());

                // Input and output streams that will be used for communicating with client
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                DataInputStream inp = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

                out.writeUTF("Connection established!");

                try{
                    String username = "";
                    String password = "";
                    boolean adminLogin = false; // Clients authority: False if logged in user is admin, True otherwise
                    int loginTryCount=0;        // How many times the client tried to login and failed
                    String reply;
                    while(true){
                        // Get username from client
                        username = inp.readUTF();
                        System.out.println(clientSocket.getRemoteSocketAddress() + "'s username: " + username);

                        // Get password from client
                        password = inp.readUTF();
                        System.out.println(clientSocket.getRemoteSocketAddress() + "'s password: " + password);


                        if(checkCredentials(username, password)){
                            break;
                        }
                        else{
                            out.writeUTF("Login failed!");
                        }
                    }

                    // Check the authority of client, set related variables and inform client about successful login.
                    if(DatabaseManager.getUserWithName(username).isIsadmin()){
                        adminLogin = true;
                        out.writeUTF("Login successful: Administrator");
                        System.out.println("An administrator logged in");
                    }
                    else{
                        out.writeUTF("Login successful: User");
                    }

                    /*
                        This loop works until client sends "exit" command.
                        Asks client to enter a command and executes it.
                        A reply is constructed after execution.
                        At the end of every loop reply is sent to client
                        Next loop starts when next command is received
                     */
                    System.out.println("receiving first command");
                    String received = inp.readUTF();
                    System.out.println("executing first command");
                    while(true){
                        // If client sends "man" command display the manual page
                        // Users and Admins have different manual pages
                        // If client sends "inbox" command display client's inbox
                        if (received.equals("inbox")){
                            reply = ("Displaying users inbox\n"+DatabaseManager.getInboxOfUser(username).toString());
                        }
                        // If client sends "outbox" command display client's outbox
                        else if (received.equals("outbox")){
                            reply = ("Displaying users outbox\n"+DatabaseManager.getOutboxOfUser(username).toString());
                        }
                        /*
                        *   If client sends "send message" command:
                        *       Server asks the client receivers name and message context
                        *       A new message object is created with current timestamp and marked as not read
                        *       Then user is informed that the message is sent
                         */
                        else if (received.equals("send message")){
                            received = inp.readUTF();
                            Message message = new Message();
                            message.deSerialize(received);
                            if(DatabaseManager.getUserWithName(message.getReceiver()) == null){
                                reply = "User not found!";
                            }
                            else{
                                DatabaseManager.addMessage(message);
                                reply = ("Message sent to " + message.getReceiver());
                            }
                        }
                        /*
                        *   This command is only executable by administrators
                        *   If client sends "add user" command:
                        *       Get the related data fields as input from client
                        *       Create a new User object with given data
                        *       Send that user object to dbm to add user to database
                         */
                        else if (received.equals("add user") && adminLogin){
                            received = inp.readUTF();
                            User user = new User();
                            user.deSerialize(received);
                            DatabaseManager.addUser(user);

                            reply = ("Created user");
                        }
                        /*
                         *   This command is only executable by administrators
                         *   Displays all the users(not administrators) when "get users" command is received
                         */
                        else if(received.equals("get users") && adminLogin){
                            reply = ("Displaying all users\n"+DatabaseManager.getAllUsers().toString());
                        }
                        /*
                         *   This command is only executable by administrators
                         *   Displays all the administrators when "get admins" command is received
                         */
                        else if(received.equals("get admins") && adminLogin) {
                            reply = ("Displaying all administrators\n"+DatabaseManager.getAllAdmins().toString());
                        }
                        /*
                         *   This command is only executable by administrators
                         *   Displays the user with given name when "get user" command is received
                         */
                        else if(received.equals("get user") && adminLogin){
                            String u_name = inp.readUTF();

                            System.out.println("Getting user:" + u_name);
                            User user = DatabaseManager.getUserWithName(u_name);
                            System.out.println(user);
                            if(user == null)
                                reply = ("user not found");
                            else
                                reply = (user.toString());
                        }
                        /*
                         *   This command is only executable by administrators
                         *   Updates the user's specified field with given value when "update user" command is received
                         */
                        else if (received.equals("update user") && adminLogin){

                            String u_name = inp.readUTF();

                            String fieldToUpdate = inp.readUTF();

                            String newValue = inp.readUTF();

                            DatabaseManager.updateUserWithName(u_name, fieldToUpdate, newValue);

                            reply = ("Updated user: " + u_name);
                        }
                        /*
                         *   This command is only executable by administrators
                         *   Deletes the user with given name when "delete user" command is received
                         */
                        else if(received.equals("delete user") && adminLogin){
                            String u_name = inp.readUTF();

                            System.out.println("Deleting user:" + u_name);
                            User user = DatabaseManager.deleteUserWithName(u_name);
                            System.out.println(user);
                            if(user == null)
                                reply = ("user not found");
                            else
                                reply = ("Deleted: " + user.toString());
                        }
                        /*
                         *  If "exit" command is received from user:
                         *      Close client socket
                         *      Close input and output streams
                         *      Then thread terminates
                         *
                         */
                        else if(received.equals("exit")){
                            System.out.println("Client " + username +" is now exiting...");
                            out.writeUTF("exit");
                            this.clientSocket.close();
                            out.close();
                            inp.close();
                            break;
                        }
                        else if(received.equals("get users") ||
                                received.equals("get admins") ||
                                received.equals("add user") ||
                                received.equals("get user") ||
                                received.equals("update user") ||
                                received.equals("delete user")){
                            reply = "You do not have the privilege to execute the command: "+ received;
                        }
                        else{
                            reply = ("Unrecognized command! " + received);
                        }
                        System.out.println("reading next command");
                        out.writeUTF(reply);
                        received=inp.readUTF();
                        System.out.println("executing next command");
                    }

                }
                catch(IOException i){
                    i.printStackTrace();
                }
            }catch (SocketTimeoutException s) {
                System.out.println("Server timed out!");
            }
            catch (IOException e){
                e.printStackTrace();
            }

    }

    public static void main(String[] args) throws IOException {
        // Port is acquired as main functions first argument
        int port = Integer.parseInt(args[0]);

        /* Serialization test
        User user = new User("ahmet", "yilmaz", java.sql.Date.valueOf("1979-06-17"), "male", "ahmmm@gmail.com", false, "ahmmm");
        System.out.println(user.serialize());
        User n_user = new User();
        n_user.deSerialize(user.serialize());
        System.out.println(n_user.serialize());

        Date date= new Date();
        Timestamp timestamp = new Timestamp(date.getTime());
        Message message = new Message("Hey", "Ahmet", "Mehmet" , timestamp, false);
        System.out.println(message.serialize());
        Message n_message = new Message();
        n_message.deSerialize(message.serialize());
        System.out.println(n_message.serialize());
        */


        // Initialize database manager
        DatabaseManager dbm = DatabaseManager.getInstance();

        try{
            // Initialize the serversocket
            serverSocket = new ServerSocket(port);
            System.out.println("Server is online\nWaiting for clients...");
        }
        catch (Exception e){
            serverSocket.close();
            e.printStackTrace();
        }
        /*
         *  Wait for a new client to connect
         *  If a new client is connected start a new thread for that client
         */

        while(true){
            try{

                Socket clientSocket = serverSocket.accept();

                Thread t = new Server(clientSocket);
                t.start();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     *  Checks if the username matches with password
     *  Returns true if it is a match, else otherwise
     */
    private boolean checkCredentials(String username, String password){
        User user = DatabaseManager.getUserWithName(username);
        if(user == null)
            return false;
        System.out.println(user);
        return (user.getPassword().equals(password));
    }

}
