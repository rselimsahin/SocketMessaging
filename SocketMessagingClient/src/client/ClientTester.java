package client;

import models.Message;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ClientTester extends Thread {
    private static String[] commandPool = {};
    private static String[] mainArgs = {"localhost", "15017"};
    private String[] login = {"admin", "admin"};
    private int userIndex;

    private ClientTester(int userIndex){
        this.userIndex = userIndex;
    }

    public void run() {
        final int PORT = 15017;
        final String address = "localhost";
        final String[] commandPool = {"inbox", "outbox", "get users", "send message", "get admins" };
        final String[] users = {"admin", "Caroline", "Abigail", "Corazon"};
        final String[] passwords = {"admin", "parasol", "abby123", "conquistador"};
        final String[] messages = {"Hey!", "Whats up!", "Hello", "How is it going?", "Are you there?"};
        BufferedReader input;
        try{
            // Initialize the socket connection
            System.out.println("Connecting to " + address + " on port " + PORT);
            Socket client = new Socket(address, PORT);
            System.out.println("Connected to " + client.getRemoteSocketAddress());

            // Initialize input and output streams that will be used for communicating with client
            InputStream inFromServer = client.getInputStream();
            DataInputStream inp = new DataInputStream(inFromServer);

            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            // String to read message from input
            String line;
            // String to read the message sent from server
            String received;

            inp.readUTF();
            out.writeUTF(users[userIndex]);
            out.writeUTF(passwords[userIndex]);
            int commandIndex;
            // Keep reading until "exit" command is received
            for(int i = 0; i < 10; i++) {
                try{
                    received = inp.readUTF();
                    if(received.equals("exit"))
                        break;
                    System.out.println(received);
                    if(i==9)
                        line="exit";
                    else{
                        Random rand = new Random();
                        commandIndex = rand.nextInt(5);
                        line = commandPool[commandIndex];
                        System.out.println("Client: " + users[userIndex] + " is executing the command: " + line);
                    }
                    if(line == "send message"){
                        Random rand = new Random();
                        out.writeUTF("send message");
                        rand = new Random();
                        int index = rand.nextInt(4);
                        String receiver = users[index];

                        rand = new Random();
                        index = rand.nextInt(4);
                        String message_text = messages[index];

                        Date date= new Date();
                        Timestamp timestamp = new Timestamp(date.getTime());

                        Message message = new Message(message_text, users[userIndex], receiver, timestamp, false);

                        line = (message.serialize());
                    }
                    out.writeUTF(line);
                    Random rand = new Random();
                    TimeUnit.MILLISECONDS.sleep(20*rand.nextInt(100));


                }
                catch(Exception e) {
                    e.printStackTrace();
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

    public static void main(String[] args) throws InterruptedException{
        for(int userIndex = 0; userIndex < 4; userIndex++){
            Thread t = new ClientTester(userIndex);
            t.start();
            TimeUnit.SECONDS.sleep(1);
        }
    }
}
