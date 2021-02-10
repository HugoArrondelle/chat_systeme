package com.chat.Models;

import java.util.Date;

public class StringMessage extends Message {
    private String message;
    public final static String id = "S";

    public StringMessage(String message, boolean received) {
        super(received);
        this.message = message;
    }

    public StringMessage(String message, boolean received, Date createdAt) {
        super(received, createdAt);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return id + ":" + (received ? "1" : "0") + ":" + createDate.getTime() + ":" + message;
    }
}
