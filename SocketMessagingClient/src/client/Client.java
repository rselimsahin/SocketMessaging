package client;

import models.User;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 *  Client is the class used by client.
 *  First connects the client to the Server
 *  Then manages the communication between server & client
 */
public class Client {
    private static final String userManual = "SMT User Manual 1.0\n" +
            "man            display manual page\n" +
            "send message   send messages to other users\n" +
            "inbox          displays user's inbox\n" +
            "outbox         displays user's outbox\n" +
            "exit           exits the system\n";

    private static final String adminManual = "SMT Admin Manual 1.0\n" +
            "man                    display manual page\n" +
            "send message           send messages to other users\n" +
            "inbox                  displays user's inbox\n" +
            "outbox                 displays user's outbox\n" +
            "add user               adds a new user to the system\n" +
            "get user               gets the user with provided username\n" +
            "get users              displays all users on database\n" +
            "get admins             displays all admins on database\n" +
            "update user            updates the user with provided username\n" +
            "delete user            deletes the user with provided username\n" +
            "exit                   exits the system\n";

    public static void main(String[] args) {
        // Server's address is acquired as main functions first argument
        String address = args[0];
        // Port is acquired as main functions second argument
        int port = Integer.parseInt(args[1]);
        BufferedReader input;
        try{
            // Initialize the socket connection
            System.out.println("Connecting to " + address + " on port " + port);
            Socket client = new Socket(address, port);

            // Initialize input and output streams that will be used for communicating with client
            InputStream inFromServer = client.getInputStream();
            DataInputStream inp = new DataInputStream(inFromServer);
            input = new BufferedReader(new InputStreamReader(System.in));

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            // Check if connection to the server is successful
            if(!inp.readUTF().equals("Connection established!")){
                System.out.println("Server connection failure!!!");
                // Close the socket connection and the inp/out streams
                client.close();
                out.close();
                inp.close();
                return;
            }
            else{
                System.out.println("Connection to the server on " + client.getRemoteSocketAddress() + " established!");
            }

            // String to read message from input
            String line;
            // String to read the message sent from server
            String received;

            // Get and send credentials
            System.out.println("Welcome to the SMT\nPlease login to the system\nUsername: ");
            String username;
            String password;
            boolean adminLogin = false;
            while (true){
                username = input.readLine();
                System.out.println("Password:");
                password = input.readLine();
                out.writeUTF(username);
                out.writeUTF(password);

                received = inp.readUTF();
                if(received.equals("Login failed!")){
                    System.out.println("Login failed!\nPlease try again...\nUsername: ");
                }
                else if(received.equals("Login successful: Administrator")){
                    adminLogin = true;
                    System.out.println("Logged in as an Administrator\nWhat is your request? (man for manual)");
                    break;
                }
                else{
                    System.out.println("Login successful!\nWhat is your request? (man for manual)");
                    break;
                }
            }

            // Keep reading until "exit" command is received
            while (true) {
                try{
                    line = input.readLine();
                    if(line.equals("man") && !adminLogin){
                        System.out.println(userManual +
                                "What is your next request? (man for manual)");
                        continue;
                    }
                    else if(line.equals("man") && adminLogin){
                        System.out.println(adminManual +
                                "What is your next request? (man for manual)");
                        continue;
                    }
                    else if(line.equals("send message")){
                        out.writeUTF("send message");

                        System.out.println("Receiver: ");
                        String receiver = input.readLine();

                        System.out.println("Message: ");
                        String message_text = input.readLine();

                        Date date= new Date();
                        Timestamp timestamp = new Timestamp(date.getTime());

                        models.Message message = new models.Message(message_text, username, receiver, timestamp, false);
                        line = message.serialize();
                    }
                    else if(line.equals("add user") && adminLogin){
                        out.writeUTF("add user");

                        System.out.println("Name: ");
                        String u_name = input.readLine();

                        System.out.println("Surname: ");
                        String u_surname = input.readLine();

                        System.out.println("Birthdate(year-month-day): ");
                        String u_birthdate_raw = input.readLine();
                        java.sql.Date u_birthdate = java.sql.Date.valueOf(u_birthdate_raw);

                        System.out.println("Gender: ");
                        String u_gender = input.readLine();

                        System.out.println("email: ");
                        String u_email = input.readLine();

                        System.out.println("isadmin(y or n): ");
                        String u_isadmin_raw = input.readLine();

                        boolean u_isadmin = false;
                        if(u_isadmin_raw.equals("y")){
                            u_isadmin = true;
                        }

                        System.out.println("password: ");
                        String u_password = input.readLine();

                        User user = new User(u_name, u_surname, u_birthdate, u_gender, u_email, u_isadmin, u_password);
                        line = user.serialize();

                    }
                    else if(line.equals("get user") && adminLogin){
                        out.writeUTF("get user");

                        System.out.println("Username: ");
                        String u_name = input.readLine();
                        out.writeUTF(u_name);
                        line = "";
                    }
                    else if(line.equals("update user") && adminLogin){
                        out.writeUTF("update user");

                        System.out.println("Username: ");
                        String u_name = input.readLine();
                        out.writeUTF(u_name);

                        System.out.println("Field to update(name, surname, birthdate, gender, email, isadmin, password): ");
                        String field = input.readLine();
                        out.writeUTF(field);

                        System.out.println("New value: ");
                        String value = input.readLine();
                        out.writeUTF(value);
                        line = "";
                    }
                    else if(line.equals("delete user") && adminLogin){
                        out.writeUTF("delete user");

                        System.out.println("Username: ");
                        String u_name = input.readLine();
                        out.writeUTF(u_name);
                        line = "";
                    }

                    if(!line.equals(""))
                        out.writeUTF(line);
                    received=inp.readUTF();
                    if(received.equals("exit"))
                        break;
                    System.out.println(received);
                    System.out.println("What is your next request? (man for manual)");
                }
                catch(IOException i) {
                    i.printStackTrace();
                }
            }

            System.out.println("Exiting now... ");

            try{
                TimeUnit.SECONDS.sleep(1);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            // Close the socket connection and the inp/out streams
            client.close();
            out.close();
            inp.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
