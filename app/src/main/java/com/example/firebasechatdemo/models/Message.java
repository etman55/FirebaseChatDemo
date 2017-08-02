package com.example.firebasechatdemo.models;

/**
 * Created by Etman on 8/2/2017.
 */

public class Message {

    private String message;
    private String type;
    private long time_stamp;
    private boolean seen;

    public Message() {
    }

    public Message(String message, String type, long time_stamp, boolean seen) {
        this.message = message;
        this.type = type;
        this.time_stamp = time_stamp;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
