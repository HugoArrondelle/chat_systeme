package com.chat.Models;

import com.chat.Main;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ChatSession extends Thread{
    private User user;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private ListProperty<Message> messageList = new SimpleListProperty<>();
    private volatile boolean proceed = true;
    private ListChangeListener<? super Message> updateMessagesFromMessageList;
    private ListChangeListener<? super Message> updateMessageFromConvList;
    private ChangeListener<? super String> updateUserFromConv;
    public final static String endString = "\t\t\t";

    public ChatSession(Socket socket, User user) {
        this.socket = socket;
        this.user = user;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageList.set(FXCollections.observableArrayList());
        messageList.get().addAll(History.getHistory(user.getAddrMac()));
        start();
    }

    public void send(String message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!message.equals(endString)) {
            Message m = new StringMessage(message, false);
            messageList.get().add(m);
            History.addToHistory(user.getAddrMac(), m);
        }
        //DEBUG
        System.out.println(messageList.get());
    }

    public void send(File message) {
        byte[] byteMessage = Application.fileToByteArray(message);
        try {
            out.writeObject(byteMessage);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message m = Application.uploadFile(byteMessage, false);
        messageList.get().add(m);
        History.addToHistory(user.getAddrMac(), m);
        //DEBUG
        System.out.println(messageList.get());
    }



    @Override
    public void run() {
        //DEBUG THREAD
        System.out.println("ChatSession " + user.toString() + " Thread Started");
        Object obj;
        try {
            while (proceed && (obj = in.readObject()) != null) {
                if (obj instanceof String) {
                    if (obj.equals(endString)) Main.removeChatSession(this);
                    else {
                        Message message = new StringMessage((String)obj, true);
                        messageList.get().add(message);
                        History.addToHistory(user.getAddrMac(), message);
                    }
                }

                else if (obj instanceof byte[]) {
                    Message message = Application.uploadFile((byte[])obj, true);
                    messageList.get().add(message);
                    History.addToHistory(user.getAddrMac(), message);
                }


                //DEBUG
                System.out.println(messageList.get());
            }
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //DEBUG THREAD
        System.out.println("ChatSession " + user.toString() + " Thread Finished");
    }

    public User getUser() {
        return user;
    }

    public ListProperty<Message> getMessageList() {
        return messageList;
    }

    public ListChangeListener<? super Message> getUpdateMessagesFromMessageList() {
        return updateMessagesFromMessageList;
    }

    public void setUpdateMessagesFromMessageList(ListChangeListener<? super Message> listener) {
        updateMessagesFromMessageList = listener;
    }

    public ListChangeListener<? super Message> getUpdateMessageFromConvList() {
        return updateMessageFromConvList;
    }

    public void setUpdateMessageFromConvList(ListChangeListener<? super Message> listener) {
        updateMessageFromConvList = listener;
    }

    public ChangeListener<? super String> getUpdateUserFromConv() {
        return updateUserFromConv;
    }

    public void setUpdateUserFromConv(ChangeListener<? super String> listener) {
        updateUserFromConv = listener;
    }

    public void closeChatSession() {
        proceed = false;
    }


}
