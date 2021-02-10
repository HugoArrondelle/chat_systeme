package com.chat.Models;


import java.io.File;
import java.util.Date;


public class FileMessage extends Message {
    private File message;
    private String fileName;
    private boolean image;
    public final static String id = "F";

    public FileMessage(File message, String fileName, boolean received) {
        super(received);
        this.message = message;
        this.fileName = fileName;
        this.image = Application.isImage(fileName);
    }

    public FileMessage(File message, String fileName, boolean received, Date createdAt) {
        super(received, createdAt);
        this.message = message;
        this.fileName = fileName;
        this.image = Application.isImage(fileName);
    }

    public File getMessage() {
        return message;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isImage() {
        return image;
    }

    @Override
    public String toString() {
        return id + ":" + (received ? "1" : "0") + ":" + createDate.getTime() + ":" + fileName + ":" + message.getPath();
    }
}
