
package com.chat.Models;

import com.chat.Main;
import com.chat.Erreurs.Errors;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.*;
import java.rmi.UnexpectedException;

public class MulticastNetwork2 {

    private InetAddress multicasIP;
    private MulticastSocket multicastSocket;
    private ListProperty<User> userList = new SimpleListProperty<>();
    private Thread threadMulticast;

    private enum Protocol {
        getUserList,
        activeUser,
        inactiveUser,
        editUsername
    }

    private static class Data {
        private InetAddress addrIp;
        private String addrMac;
        private Protocol protocol;
        private String data;
        private Data(Protocol protocol, String data) {
            this.addrMac = Main.getUser().getAddrMac();
            this.protocol = protocol;
            this.data = data;
        }
        private Data(String data, InetAddress addrIp) {
            this.addrIp = addrIp;
            String[] data_split = data.split(":", 3);
            this.addrMac = data_split[0];
            this.protocol = Protocol.valueOf(data_split[1]);
            this.data = data_split[2];
        }
        @Override
        public String toString() {
            return addrMac + ":" + protocol + ":" + data;
        }
    }

    public MulticastNetwork2(String mcAddr, int mcPort) {
        try
        {
            multicasIP = InetAddress.getByName(mcAddr);
            multicastSocket = new MulticastSocket(mcPort);
            multicastSocket.joinGroup(multicasIP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createThread() {
        //DEBUG THREAD
        System.out.println("MulticastNetwork Thread Started");
        threadMulticast = new Thread(() -> {
            Data data;
            try {
                multicastSocket.setSoTimeout(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (Main.getUser().getIsConnected()) {
                try {
                    data = receive();
                    if (data.protocol == Protocol.getUserList || !data.addrMac.equals(Main.getUser().getAddrMac()))
                        receiveMulticast(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println("MulticastNetwork Thread Finished");
        });
        threadMulticast.interrupt();
    }



    private Data receive() throws Exception {
        DatagramPacket data_packet;
        byte[] data;
        data = new byte[1024];
        data_packet = new DatagramPacket(data, data.length);
        multicastSocket.receive(data_packet);
        return new Data((new DataInputStream(new ByteArrayInputStream(data))).readUTF(), data_packet.getAddress());

    }

    private void send(Data data) throws Exception {
        byte[] data_byte;
        DatagramPacket data_packet;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        System.out.println("Send multicast data : " + data);
        (new DataOutputStream(output)).writeUTF(data.toString());
        data_byte = output.toByteArray();
        data_packet = new DatagramPacket(data_byte, data_byte.length, multicasIP, multicastSocket.getLocalPort());
        multicastSocket.send(data_packet);
    }

    public void initializeUserList() {
        Data data ;
        boolean recherche = true ;
        userList.set(FXCollections.observableArrayList());

        try {
            send(new Data(Protocol.getUserList , ""));
            multicastSocket.setSoTimeout(1000);
        }catch (Exception e){
            e.printStackTrace();
        }

        while(recherche){
            try {
                data = receive();
                if ((data.protocol == Protocol.activeUser) && (!containUserList(data.addrMac)))
                {
                    addUserList(data.addrMac, data.data, data.addrIp);
                }
            }catch (Exception e) {
                if (e instanceof SocketTimeoutException)
                    recherche = false;
                else
                    e.printStackTrace();
            }
        }
    }

    public ListProperty<User> getUserList() {
        return userList;
    }

    public boolean containUserList(String addrMAc){
        return userList.get().stream().anyMatch(e-> e.getAddrMac().equals(addrMAc));
    }
    public boolean containUserListUsername(String username){
        return userList.get().stream().anyMatch(e->e.toString().equals(username));
    }



    private void addUserList(String addrMac, String username, InetAddress addrIp) {
        try {
            userList.get().add(new User(addrMac, username, addrIp));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void userListRemove(String addrMac) {
        userList.get().stream().filter(e -> e.getAddrMac().equals(addrMac)).findFirst().ifPresent(e -> userList.get().remove(e));
    }

    public boolean signIn(String username) throws Exception {
        Main.setUser(username);
        if (checkAddrMac(Main.getUser().getAddrMac())) {
            if (checkUsername(username)) {
                Main.getUser().setIsConnected();
                createThread();
                threadMulticast.start();
                //DEBUG
                System.out.println("Your username : " + username + "@" + Main.getUser().getAddrMac());
                System.out.println(userList.get());
                try {
                    send(new Data(Protocol.activeUser, username));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            } else {
                try {
                    userList = new SimpleListProperty<>();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                throw new Errors("authErrorUsernameUsed");
            }
        } else {
            try {
                userList = new SimpleListProperty<>();
            } catch (Exception e) {
                e.printStackTrace();
            }
            throw new Errors("authErrorAddrMacUsed");
        }

    }


    public void signOut(){
        Main.getUser().setIsConnected();
        userList = new SimpleListProperty<>();
        System.out.println(userList.get());
        try {
            send(new Data(Protocol.inactiveUser, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void signOutAndLeave(){
        signOut();
        try {
            multicastSocket.leaveGroup(multicasIP);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public boolean checkAddrMac (String addrMac)
    {
        if (userList.get() == null)
        {
            initializeUserList();
        }
        return !containUserList(addrMac);
    }
    public boolean checkUsername (String username)
    {
        if (userList.get() == null)
        {
            initializeUserList();
        }
        return !containUserListUsername(username);
    }


    public boolean editUsername(String username) throws Exception {
        if (checkUsername(username)) {
            Main.getUser().editUsername(username);
            try {
                send(new Data(Protocol.editUsername, username));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        } else {
            throw new Errors("authErrorUsernameUsed");
        }
    }











    private void receiveMulticast(Data data)
    {
        System.out.println("R : " + data);
        switch (data.protocol) {
        	case getUserList:
                try {
                    send(new Data(Protocol.activeUser, Main.getUser().toString()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case activeUser:
                if (!containUserList(data.addrMac)) {
                    addUserList(data.addrMac, data.data, data.addrIp);
                    System.out.println(userList.get());
                }
                break;
            case inactiveUser:
                if (containUserList(data.addrMac)) {
                    userListRemove(data.addrMac);
                    System.out.println(userList.get());
                }
                break;
            case editUsername:
                if (containUserList(data.addrMac)) {
                    userList.get().stream()
                            .filter(e -> e.getAddrMac().equals(data.addrMac))
                            .findFirst()
                            .ifPresent(e -> e.editUsername(data.data));
                    //DEBUG
                    System.out.println(userList.get());
                }
                break;
        }
    }






}
