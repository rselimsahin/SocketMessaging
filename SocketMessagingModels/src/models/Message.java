package models;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private String message_text;
    private String sender;
    private String receiver;
    private Timestamp sendTime;
    private boolean read;

    public Message(){}

    public Message(String message_text, String sender, String receiver, Timestamp sendTime, boolean read) {
        this.message_text = message_text;
        this.sender = sender;
        this.receiver = receiver;
        this.sendTime = sendTime;
        this.read = read;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message_text='" + message_text + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver=" + receiver+
                ", sendTime=" + sendTime +
                ", read=" + read +
                '}';
    }


    public String serialize() {
        return  message_text +
                "|" + sender +
                "|" + receiver+
                "|" + sendTime +
                "|" + read +
                "|";
    }


    public Message deSerialize(String serial){
        int serial_len = serial.length();
        char c;
        String word = "";
        int field = 0;
        for(int i = 0; i < serial_len; i++){
            c = serial.charAt(i);
            if(c == '|'){
                if(field == 0){
                    message_text = word;
                }
                else if(field == 1){
                    sender = word;
                }
                else if(field == 2){
                    receiver = word;
                }
                else if(field == 3){
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                        Date parsedDate = dateFormat.parse(word);
                        sendTime = new java.sql.Timestamp(parsedDate.getTime());
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                else if(field == 4) {
                    read = word.equals("true");
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


    public String getMessage_text() {
        return message_text;
    }

    public void setMessage_text(String message_text) {
        this.message_text = message_text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Timestamp getSendTime() {
        return sendTime;
    }

    public void setSendTime(Timestamp sendTime) {
        this.sendTime = sendTime;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
