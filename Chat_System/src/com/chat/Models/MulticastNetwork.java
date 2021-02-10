package com.chat.Models;

import com.chat.Erreurs.Errors;
import com.chat.Main;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class MulticastNetwork {
    private InetAddress mcIp;
    private MulticastSocket mcSocket;
    private ListProperty<User> userList = new SimpleListProperty<>();
    private Thread task;

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

    public MulticastNetwork(String mcAddr, int mcPort) {
        try {
            mcIp = InetAddress.getByName(mcAddr);
            mcSocket = new MulticastSocket(mcPort);
            mcSocket.joinGroup(mcIp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createTask() {
        //DEBUG THREAD
        System.out.println("MulticastNetwork Thread Started");
        task = new Thread(() -> {
            Data data;
            try {
                mcSocket.setSoTimeout(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            while (Main.getUser().getIsConnected()) {
                try {
                    data = receive();
                    if (data.protocol == Protocol.getUserList || !data.addrMac.equals(Main.getUser().getAddrMac()))
                        receivePDU(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //DEBUG THREAD
            System.out.println("MulticastNetwork Thread Finished");
        });
        task.interrupt();
    }

    private Data receive() throws Exception {
        DatagramPacket data_packet;
        byte[] data;

        data = new byte[1024];
        data_packet = new DatagramPacket(data, data.length);
        mcSocket.receive(data_packet);
        return new Data((new DataInputStream(new ByteArrayInputStream(data))).readUTF(), data_packet.getAddress());
    }

    private void send(Data data) throws Exception {
        byte[] data_byte;
        DatagramPacket data_packet;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        //DEBUG
        System.out.println("S : " + data);
        (new DataOutputStream(output)).writeUTF(data.toString());
        data_byte = output.toByteArray();
        data_packet = new DatagramPacket(data_byte, data_byte.length, mcIp, mcSocket.getLocalPort());
        mcSocket.send(data_packet);
    }

    private void initUserList() {
        Data data;
        boolean continueResearch = true;

        userList.set(FXCollections.observableArrayList());
        try {
            send(new Data(Protocol.getUserList, ""));
            mcSocket.setSoTimeout(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (continueResearch) {
            try {
                data = receive();
                if (data.protocol == Protocol.activeUser && !userListContains(data.addrMac))
                    userListAdd(data.addrMac, data.data, data.addrIp);
            } catch (Exception e) {
                if (e instanceof SocketTimeoutException)
                    continueResearch = false;
                else
                    e.printStackTrace();
            }
        }
    }

    public ListProperty<User> getUserList() {
      return userList;
    }

    private boolean userListContains(String addrMac) {
        return userList.get().stream().anyMatch(e -> e.getAddrMac().equals(addrMac));
    }

    private boolean userListContainsUsername(String username) {
        return userList.get().stream().anyMatch(e -> e.toString().equals(username));
    }

    private void userListAdd(String addrMac, String username, InetAddress addrIp) {
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
                createTask();
                task.start();
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
        //DEBUG
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
            mcSocket.leaveGroup(mcIp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkAddrMac(String addrMac) {
        if (userList.get() == null) {
            initUserList();
        }
        return !userListContains(addrMac);
    }

    private boolean checkUsername(String username) {
        if (userList.get() == null) {
            initUserList();
        }
        return !userListContainsUsername(username);
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





    private void receivePDU(Data data) {
        //DEBUG
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
                if (!userListContains(data.addrMac)) {
                    userListAdd(data.addrMac, data.data, data.addrIp);
                    //DEBUG
                    System.out.println(userList.get());
                }
                break;
            case inactiveUser:
                if (userListContains(data.addrMac)) {
                    userListRemove(data.addrMac);
                    //DEBUG
                    System.out.println(userList.get());
                }
                break;
            case editUsername:
                if (userListContains(data.addrMac)) {
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
