package models;

import java.sql.Date;

public class User {
    private int userID = 0;
    private String name;
    private String surname;
    private Date birthdate;
    private String gender;
    private String email;
    private boolean isadmin;
    private String password;

    public User(){}
    public User(String name, String surname, Date birthdate, String gender, String email, boolean isadmin, String password) {
        this.name = name;   // name is unique
        this.surname = surname;
        this.birthdate = birthdate;
        this.gender = gender;
        this.email = email;
        this.isadmin = isadmin;
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", birthdate=" + birthdate +
                ", gender='" + gender + '\'' +
                ", email='" + email + '\'' +
                ", isadmin=" + isadmin +
                ", password='" + password + '\'' +
                '}';
    }

    public String serialize() {
        return      userID +
                "|" + name +
                "|" + surname +
                "|" + birthdate +
                "|" + gender +
                "|" + email +
                "|" + isadmin +
                "|" + password +
                "|";
    }

    public User deSerialize(String serial){
        int serial_len = serial.length();
        char c;
        String word = "";
        int field = 0;
        for(int i = 0; i < serial_len; i++){
            c = serial.charAt(i);
            if(c == '|'){
                if(field == 0){
                    userID = Integer.parseInt(word);
                }
                else if(field == 1){
                    name = word;
                }
                else if(field == 2){
                    surname = word;
                }
                else if(field == 3){
                    birthdate = java.sql.Date.valueOf(word);
                }
                else if(field == 4){
                    gender = word;
                }
                else if(field == 5){
                    email = word;
                }
                else if(field == 6){
                    isadmin = word.equals("true");
                }
                else{
                    password = word;
                }


                field++;
                word = "";
            }
            else{
                word += c;
            }
        }
        return this;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public boolean isIsadmin() {
        return isadmin;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsadmin(boolean isadmin) {
        this.isadmin = isadmin;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
