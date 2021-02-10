package com.chat.Models;

import com.chat.Main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatSessionServeur extends Thread {

    private ServerSocket srvSocket;
    private volatile boolean proceed = true;

    public ChatSessionServeur(int port){
        try{
            srvSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }

    @Override
    public void run(){
        System.out.println("Thread ChatSessionServeur Start");
        try {
            while(proceed){
                Socket s = srvSocket.accept();
                User u = Main.getMcNetwork().getUserList().get().stream().filter(e-> e.getAddrIp().equals(s.getInetAddress())).findFirst().orElse(null);
                ChatSession cs = new ChatSession(s, u);
                Main.newChatSession(cs);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("ChatSessionServer Thread Finished");
    }
    public void close() {
        proceed = false;
        try {
            srvSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
