    package database;

import models.Message;
import models.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


    /**
     * Database Manager as a singleton class provides database connection and management
     * Database has two tables as USERS and MESSAGESx
     * Database includes a default admin with name: admin & password: admin
     */
    public class DatabaseManager {
        private static DatabaseManager instance;
        private static Connection c;

        public static DatabaseManager getInstance(){
            if (instance == null)
                instance = new DatabaseManager();

            return instance;
        }

        private DatabaseManager(){
            try{
                // Initialize drivers for PostgreSQL
                Class.forName("org.postgresql.Driver");
                c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb", "postgres", "admin");

                // Close auto commit function
                c.setAutoCommit(false);

                System.out.println("Successfully connected to the database");

            }
            catch(Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }

            // String to store query
            String query;

            try {
                // Clear previous table for test purposes
                //query = "DROP TABLE IF EXISTS USERS CASCADE";
                //execQuery(c, query);
                //System.out.println("Cleared previous USERS table");

                // Create users table
                query = "CREATE TABLE IF NOT EXISTS USERS " +
                        "(" +
                        "ID             SERIAL PRIMARY KEY NOT NULL," +
                        "NAME           TEXT            NOT NULL    UNIQUE," +
                        "SURNAME        TEXT            NOT NULL," +
                        "BIRTHDATE      DATE                    ," +
                        "GENDER         TEXT            NOT NULL," +
                        "EMAIL          TEXT            NOT NULL," +
                        "ISADMIN        BOOLEAN         NOT NULL  DEFAULT FALSE," +
                        "PASSWORD       TEXT            NOT NULL" +
                        ")";
                execQuery(c, query);
                System.out.println("Created USERS table!");

                // Clear previous table for test purposes
                //query = "DROP TABLE IF EXISTS MESSAGES CASCADE";
                //execQuery(c, query);
                //System.out.println("Cleared previous MESSAGES table");

                // Create messages table
                query = "CREATE TABLE IF NOT EXISTS MESSAGES " +
                        "(" +
                        "USERID         SERIAL REFERENCES USERS(ID)     ON DELETE NO ACTION    NOT NULL," +
                        "MESSAGETEXT    TEXT                        NOT NULL," +
                        "SENDER         TEXT                        NOT NULL," +
                        "RECEIVER       TEXT                        NOT NULL," +
                        "SENDTIME       TIMESTAMP                           ," +
                        "ISREAD         BOOLEAN                     NOT NULL" +
                        ")";
                execQuery(c, query);
                System.out.println("Created MESSAGES table!");

                // Add the default admin to the database
                //User admin = new User("admin", "admin", java.sql.Date.valueOf("2019-06-17"), "-", "-", true, "admin");
                //addUser(admin);
                //System.out.println("Created default admin!");
            }
            catch(Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
        }

        /**
         * Executes the provided query on the given connection
         * sThen commits to the database
         * @param c connection to database
         * @param query query to be sent
         */
        private static void execQuery(Connection c, String query){
            try{
                Statement s = c.createStatement();
                s.executeUpdate(query);
                s.close();
                c.commit();

            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
        }

        /**
         * Prints the given result set to the terminal
         * @param rs ResultSet
         */
        public static void printQueryResult(ResultSet rs){
            try{
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnCount = rsMetaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) System.out.print("   ");
                    System.out.print(rsMetaData.getColumnName(i));
                }
                System.out.println();
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        if(i>1)
                            System.out.print("   ");
                        String columnValue = rs.getString(i);
                        System.out.print(columnValue);
                    }
                    System.out.println();
                }
            }
            catch (SQLException e){
                e.printStackTrace();
            }
        }

        /**
         * Parses the given query result and returns it as a string
         * @param rs resultset
         * @return queryResult
         */

        public static String getQueryResult(ResultSet rs){
            String result = "";
            try{
                ResultSetMetaData rsMetaData = rs.getMetaData();
                int columnCount = rsMetaData.getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) result = result.concat("   ");
                    result = result.concat(rsMetaData.getColumnName(i));
                }
                result = result.concat("\n");
                boolean isempty=true;
                while (rs.next()) {
                    isempty=false;
                    for (int i = 1; i <= columnCount; i++) {
                        if(i>1)
                            result = result.concat("   ");
                        String columnValue = rs.getString(i);
                        result = result.concat(columnValue);
                    }
                    result = result.concat("\n");
                }
                if(isempty)
                    return "";
            }
            catch (SQLException e){
                e.printStackTrace();
            }

            return result;
        }

        /**
         * Inserts given User to the database
         * @param user User to be inserted to database
         */
        public static void addUser(User user){
            String name = user.getName();
            String surname = user.getSurname();
            Date birthdate = user.getBirthdate();
            String gender = user.getGender();
            String email = user.getEmail();
            boolean isadmin = user.isIsadmin();
            String password = user.getPassword();
            try{
                String query = "INSERT INTO USERS(NAME, SURNAME, BIRTHDATE, GENDER, EMAIL, ISADMIN, PASSWORD) "
                        + "VALUES (" + "\'" + name + "\',\'" + surname + "\',\'" +  birthdate + "\',\'"
                        + gender + "\',\'" + email + "\'," + isadmin + ",\'" + password + "\');";
                System.out.println(query);
                Statement s = c.createStatement();
                s.executeUpdate(query);
                s.close();
                c.commit();
                /*  Set user ID
                ResultSet rs = s.executeQuery("SELECT ID FROM USERS WHERE NAME=\'" + name + "\';");
                rs.next();
                int userID =  rs.getInt("ID");
                 */
            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
        }


        /**
         * Gets all users that are not administrators from database
         * @return users
         */
        public static List<User> getAllUsers(){
            List<User> users = new ArrayList<>();
            User user;
            try{
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM USERS WHERE ISADMIN=FALSE");
                System.out.println("Getting all users");
                while(rs.next()){
                    int userID = rs.getInt("ID");
                    String name = rs.getString("NAME");
                    String surname = rs.getString("SURNAME");
                    Date date = rs.getDate("BIRTHDATE");
                    String gender = rs.getString("GENDER");
                    String email = rs.getString("EMAIL");
                    boolean isadmin = rs.getBoolean("ISADMIN");
                    String password = rs.getString("PASSWORD");
                    user = new User(name, surname, date, gender, email, isadmin, password);
                    user.setUserID(userID);
                    users.add(user);
                }
                s.close();

            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }

            return users;
        }

        /**
         * Gets all administrators from database
         * @return admins
         */
        public static List<User> getAllAdmins() {
            List<User> admins = new ArrayList<>();
            User admin;
            try{
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM USERS WHERE ISADMIN=TRUE");
                System.out.println("Getting all administrators");
                while(rs.next()){
                    int userID = rs.getInt("ID");
                    String name = rs.getString("NAME");
                    String surname = rs.getString("SURNAME");
                    Date date = rs.getDate("BIRTHDATE");
                    String gender = rs.getString("GENDER");
                    String email = rs.getString("EMAIL");
                    boolean isadmin = rs.getBoolean("ISADMIN");
                    String password = rs.getString("PASSWORD");
                    admin = new User(name, surname, date, gender, email, isadmin, password);
                    admin.setUserID(userID);
                    admins.add(admin);
                }
                s.close();

            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }

            return admins;
        }

        /**
         * Gets user with given name from database
         * @param name name of user to be selected
         * @return User
         */
        public static User getUserWithName(String name){
            User user;
            try{
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM USERS WHERE NAME = \'" + name + "\'");
                System.out.println("Getting user with name "+name);
                if(rs.next()){
                    int userID = rs.getInt("ID");
                    //String uname = rs.getString("NAME");
                    String surname = rs.getString("SURNAME");
                    Date date = rs.getDate("BIRTHDATE");
                    String gender = rs.getString("GENDER");
                    String email = rs.getString("EMAIL");
                    boolean isadmin = rs.getBoolean("ISADMIN");
                    String password = rs.getString("PASSWORD");
                    user = new User( name, surname, date, gender, email, isadmin, password);
                    user.setUserID(userID);
                    s.close();
                    return user;
                }
                s.close();

            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }

            return null;
        }

        /**
         * Updates the user's specified field with given value
         * Then returns user's new version
         * @param name name of user to be updated
         * @param fieldToUpdate field name to be updated
         * @param newValue  new value to be inserted to the database
         * @return User
         */
        public static User updateUserWithName(String name, String fieldToUpdate, String newValue){
            User user;
            try{
                user=getUserWithName(name);
                if(user == null){
                    return null;
                }
                String query = "UPDATE USERS SET " + fieldToUpdate + "=\'" + newValue + "\' WHERE NAME =\'" + name + "\';";
                System.out.println(query);
                execQuery(c, query);
                System.out.println("Updated user with name "+name);
                user=getUserWithName(name);
                return user;
            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }

            return null;
        }


        /**
         * Deletes user with given name
         * Then returns deleted user
         * @param name name of user to be deleted
         * @return User
         */
        public static User deleteUserWithName(String name){
            User user;
            try{
                user=getUserWithName(name);
                if(user == null){
                    return null;
                }
                String query = "DELETE from USERS where NAME =\'" + name + "\';";
                execQuery(c, query);
                System.out.println("Deleted user with name "+name);
                return user;
            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }

            return null;
        }

        /**
         * Adds a message to the database !messages are not unique
         * @param message message to be inserted to the database
         */
        public static void addMessage(models.Message message){
            int userID = getUserWithName(message.getSender()).getUserID();
            try{
                String query = "INSERT INTO MESSAGES(USERID, MESSAGETEXT, SENDER, RECEIVER, SENDTIME, ISREAD) "
                        + "VALUES (" + userID + ",\'" + message.getMessage_text()+ "\',\'" + message.getSender() + "\',\'" + message.getReceiver()
                        + "\',\'" + message.getSendTime()+ "\'," +  message.isRead() + ");";
                System.out.println(query);
                Statement s = c.createStatement();
                s.executeUpdate(query);
                s.close();
                c.commit();
            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
        }

        /**
         * Gets the messages sent by user
         * @param name name of user requesting outbox
         * @return List<models.Message>
         */
        public static List<models.Message> getOutboxOfUser(String name){
            List<models.Message> outbox = new ArrayList<>();
            models.Message message;
            try{
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM MESSAGES WHERE SENDER = \'" + name + "\'");
                System.out.println("Getting outbox of user "+name);
                while(rs.next()){
                    int userID = rs.getInt("USERID");
                    String message_text = rs.getString("MESSAGETEXT");
                    String sender = rs.getString("SENDER");
                    String receiver = rs.getString("RECEIVER");
                    Timestamp sendTime = rs.getTimestamp("SENDTIME");
                    boolean isread = rs.getBoolean("ISREAD");

                    message = new Message(message_text, sender, receiver, sendTime, isread);
                    outbox.add(message);
                }
                s.close();

            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }

            return outbox;
        }

        /**
         * Gets messages received by user
         * @param name name of user requesting inbox
         * @return List<models.Message>
         */
        public static List<models.Message> getInboxOfUser(String name){
            List<models.Message> inbox = new ArrayList<>();
            models.Message message;
            try{
                Statement s = c.createStatement();
                ResultSet rs = s.executeQuery("SELECT * FROM MESSAGES WHERE RECEIVER = \'" + name + "\'");
                System.out.println("Getting inbox of user "+name);
                while(rs.next()){
                    int userID = rs.getInt("USERID");
                    String message_text = rs.getString("MESSAGETEXT");
                    String sender = rs.getString("SENDER");
                    String receiver = rs.getString("RECEIVER");
                    Timestamp sendTime = rs.getTimestamp("SENDTIME");
                    boolean isread = rs.getBoolean("ISREAD");
                    message = new Message(message_text, sender, receiver, sendTime, isread);
                    inbox.add(message);
                }
                s.close();

                // Set messages as read
                String query = "UPDATE MESSAGES SET ISREAD=TRUE WHERE RECEIVER =\'" + name + "\';";
                System.out.println(query);
                execQuery(c, query);

            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }

            return inbox;
        }

        /**
         * Close the database connection
         */
        public static void closeDB(){
            try{
                c.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
