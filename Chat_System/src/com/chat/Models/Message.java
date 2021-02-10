package com.chat.Models;

import java.util.Date;

public abstract class Message {
    protected boolean received;
    protected Date createDate;

    public Message(boolean received) {
        this.received = received;
        this.createDate = new Date();
    }

    public Message(boolean received, Date createdAt) {
        this.received = received;
        this.createDate = createdAt;
    }

    public boolean isReceived() {
        return received;
    }

    public Date getCreatedAt() {
        return createDate;
    }

    abstract public Object getMessage();

    @Override
    abstract public String toString();
}
