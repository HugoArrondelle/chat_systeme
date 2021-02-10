package com.chat.Models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import com.chat.Erreurs.Errors;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.Enumeration;

public class User {

    private StringProperty username = new SimpleStringProperty();
    private InetAddress addrIp = null;
    private String addrMac;
    private Date createDate;
    private volatile boolean IsConnected = false;
    private ChangeListener<? super String> updateUserFromUserList;
    private ChangeListener<? super String> updateUserFromConvList;



    public User(String Macaddr, String username, InetAddress IPaddr){
        this.addrMac = Macaddr ;
        this.addrIp = IPaddr;
        this.username.set(username);
        this.createDate = new Date();
    }

    public User(String username){
        this.username.set(username);
        this.createDate = new Date();

        try {
            NetworkInterface networkInterface = null;
            for (Enumeration<NetworkInterface> i = NetworkInterface.getNetworkInterfaces(); i.hasMoreElements();) {
                networkInterface = i.nextElement();
                if (networkInterface.getHardwareAddress() != null && networkInterface.supportsMulticast()) {
                    addrIp = networkInterface.getInetAddresses().nextElement();
                    break;
                }
            }

            if (addrIp == null)
            {
                System.out.println("Erreur addrIp");
            }

            //to make the mac address
            StringBuilder sb = new StringBuilder(10);
            for (byte b : networkInterface.getHardwareAddress()) sb.append(String.format("%02x", b));
            this.addrMac = sb.toString();


        }catch (Exception e){
            System.out.println("Erreur");
        }

    }

    public static boolean checkUsername(String username) throws Exception {
        if (username.length() >= 3 ){
            if (username.length() <= 10){
                if (username.matches("^[a-zA-Z_0-9]*$")){
                    return true;
                } else throw new Errors("Character error" );
            } else throw new Errors("Username > 10" );
        } else throw new Errors("Username < 3" );
    }


    public StringProperty getUsername() {
        return username;
    }

    public String getAddrMac(){
        return addrMac;
    }

    public InetAddress getAddrIp(){
        return addrIp;
    }

    public Date getCreateDate(){
        return createDate;
    }

    public boolean getIsConnected(){
        return IsConnected;
    }

    public void setIsConnected(){
        IsConnected = !IsConnected;
    }

    public void editUsername(String newUsername){
        this.username.set(newUsername);
    }

    public ChangeListener<? super String> getUpdateUserFromUserList() {
        return updateUserFromUserList;
    }

    public void setUpdateUserFromUserList(ChangeListener<? super String> listener) {
        updateUserFromUserList = listener;
    }

    public ChangeListener<? super String> getUpdateUserFromConvList() {
        return updateUserFromConvList;
    }

    public void setUpdateUserFromConvList(ChangeListener<? super String> listener) {
        updateUserFromConvList = listener;
    }


    @Override
    public String toString(){
        return username.get();
    }

}
