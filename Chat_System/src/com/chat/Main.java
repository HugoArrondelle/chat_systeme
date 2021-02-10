package com.chat;

import com.chat.Controllers.AuthController;
import com.chat.Controllers.ChatController;
import com.chat.Models.ChatSession;
import com.chat.Models.ChatSessionServeur;
import com.chat.Models.MulticastNetwork2;
import com.chat.Models.User;
import javafx.application.Application;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private static Stage primaryStage;
    private Parent root;
    private static ListProperty<ChatSession> chatSessionList = new SimpleListProperty<>();
    private static User user;
    private final static String mcAddr = "228.3.14.15"; //Multicast address used by Multicast Network
    private final static int mcPort = 8765; //Multicast port used by Multicast Network
    private static MulticastNetwork2 mcNetwork;
    public final static int srvPort = 9876; //Port used to wait and accept Chat Session
    private static ChatSessionServeur csServer;


    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            initStage();
            startAuth();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void startAuth() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chat/Views/AuthWindow2.fxml"));
            root = loader.load();
            AuthController controller = loader.getController();
            controller.setApp(this);
            //startScene(531,560);
            startScene(500,400);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void startChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chat/Views/ChatWindow.fxml"));
            root = loader.load();
            ChatController controller = loader.getController();
            controller.setApp(this);
            startScene(800,650);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initStage() {
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/Chat.png")));
        primaryStage.setTitle("ChatSystem");
        primaryStage.setAlwaysOnTop(false);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
    }

    private void startScene(double v, double v1) {
        primaryStage.setScene(new Scene(root, v, v1));
        primaryStage.getScene().setFill(Color.TRANSPARENT);
        primaryStage.show();
        //Next 3 lines, center the window on screen
        //To know why we don't use the javafx function centerOnScreen(), see https://stackoverflow.com/a/29560563/11957830
        Rectangle2D bounds  = Screen.getPrimary().getVisualBounds();
        primaryStage.setX((bounds.getWidth() - primaryStage.getWidth()) / 2);
        primaryStage.setY((bounds.getHeight() - primaryStage.getHeight()) / 2);
        root.requestFocus();
    }


    public static void setUser(String username) throws Exception {
        if (username != null) user = new User(username);
        else user = null;
    }

    public static void removeChatSession(ChatSession cs) {
        cs.send(ChatSession.endString);
        cs.closeChatSession();
        chatSessionList.get().remove(cs);
    }

    public static void removeAllChatSession() {
        chatSessionList.get().forEach(cs -> {
            cs.send(ChatSession.endString);
            cs.closeChatSession();
        });
        chatSessionList.get().clear();
    }

    public static void closeChatSessionServer() {
        csServer.close();
    }
    public static ListProperty<ChatSession> getChatSessionList() { return chatSessionList; }

    public static MulticastNetwork2 getMcNetwork() {
        return mcNetwork;
    }

    public static Stage getStage() {
        return primaryStage;
    }

    public static User getUser() {
        return user;
    }

    public static void newChatSession(ChatSession cs) { chatSessionList.get().add(cs); }

    public static void main(String[] args) {

        mcNetwork = new MulticastNetwork2(mcAddr, mcPort);

        csServer = new ChatSessionServeur(srvPort);

        chatSessionList.set(FXCollections.observableArrayList());

        launch(args);
    }
}
