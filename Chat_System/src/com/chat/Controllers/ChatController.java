package com.chat.Controllers;

import com.chat.Main;
import com.chat.Models.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    private Main app;
    private double xOffset = 0;
    private double yOffset = 0;
    private ListChangeListener<? super User> updateUsersFromUserList;
    private ChangeListener<? super Number> scrollPaneVvalue;
    private ListChangeListener<? super ChatSession> updateConvsFromConvList;

    public void setApp(Main app) {
        this.app = app;
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        tabSelected = tabUser;
        settingsList = (VBox)listControl.getContent();
        listControl.setContent(userList);
        setTitleListControl("Utilisateurs");
        listControl.setStyle("-fx-background-color:#E3E3E3;");




        usernameBox.managedProperty().bind(usernameBox.visibleProperty());
        editUsernameBox.managedProperty().bind(editUsernameBox.visibleProperty());

        editUsernameBox.setVisible(false);
        usernameBox.setVisible(true);

        usernameLabel.setText(Main.getUser().toString());
        editUsernameField.setText(Main.getUser().toString());

        noConvLabel.setText("Pas de conversation");
        noChatStarted.setVisible(true);






        Main.getMcNetwork().getUserList().get().forEach(this::addUserToUserList);


        //Add a listener on userList
        updateUsersFromUserList = e -> {
            while (e.next()) {
                for (User rm : e.getRemoved()) {
                    Platform.runLater(() -> removeUserFromUserList(rm));
                }
                for (User add : e.getAddedSubList()) {
                    Platform.runLater(() -> addUserToUserList(add));
                }
            }
        };
        Main.getMcNetwork().getUserList().addListener(updateUsersFromUserList);



        updateConvsFromConvList = e -> {
            while (e.next()) {
                for (ChatSession rm : e.getRemoved()) {
                    Platform.runLater(() -> removeConvFromChatList(rm));
                    if (rm == chatSessionActive) {
                        Platform.runLater(() -> {
                            noChatStarted.setVisible(true);
                            closeChat();
                            chatSessionActive = null;
                        });
                    }
                }
                for (ChatSession add : e.getAddedSubList()) {
                    Platform.runLater(() -> addConvToChatList(add));
                }
            }
        };
        Main.getChatSessionList().addListener(updateConvsFromConvList);
        //Add a listener on messageListPaneVvalue
        scrollPaneVvalue = (observable, oldValue, newValue) -> {
            if(needScrolling && oldValue.doubleValue() == messageListPane.getVmax()) {
                messageListPane.setVvalue(messageListPane.getVmax());
                needScrolling = false;
            }
        };
        messageListPane.vvalueProperty().addListener(scrollPaneVvalue);


    }


    /**************************************
            * Action Windows *
    **************************************/

    public void onCloseClicked(MouseEvent me) {
            Main.getMcNetwork().signOutAndLeave();
            Main.removeAllChatSession();
            Main.closeChatSessionServer();
            Main.getMcNetwork().getUserList().removeListener(updateUsersFromUserList);
            Main.getChatSessionList().removeListener(updateConvsFromConvList);
            messageListPane.vvalueProperty().removeListener(scrollPaneVvalue);
            Platform.exit();
        }

    public void onMinimizeClicked(MouseEvent me) {
        Main.getStage().setIconified(true);
    }

    //Take coord of the scene
    public void onWindowPressed(MouseEvent mouseEvent) {
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }

    //Move the scene based on the origin coord
    public void onWindowDragged(MouseEvent mouseEvent) {
        Main.getStage().setX(mouseEvent.getScreenX() - xOffset);
        Main.getStage().setY(mouseEvent.getScreenY() - yOffset);
    }
    /**************************************
                * MENU BAR *
     **************************************/
    private StackPane tabSelected = null;
    public StackPane tabUser;
    public StackPane tabChat;
    public StackPane tabSettings;

    private void selectedTab(StackPane tab) {
        tabSelected.getStyleClass().clear();
        tabSelected = tab;
        tabSelected.getStyleClass().add("bg-black");
    }

    public void onUserTabClicked(MouseEvent mouseEvent) {
        selectedTab((StackPane)mouseEvent.getSource());
        setTitleListControl("Utilisateurs");
        listControl.setContent(userList);
    }

    public void onChatTabClicked(MouseEvent mouseEvent) {
        selectedTab((StackPane)mouseEvent.getSource());
        setTitleListControl("Conversation");
        listControl.setContent(chatList);
    }

    public void onSettingsTabClicked(MouseEvent mouseEvent) {
        selectedTab((StackPane)mouseEvent.getSource());
        setTitleListControl("Paramètre");
        listControl.setContent(settingsList);

    }

    public void onSignoutTabClicked(MouseEvent mouseEvent) {
        Main.getMcNetwork().signOut();
        Main.removeAllChatSession();
        Main.getMcNetwork().getUserList().removeListener(updateUsersFromUserList);
        Main.getChatSessionList().removeListener(updateConvsFromConvList);
        messageListPane.vvalueProperty().removeListener(scrollPaneVvalue);
        app.startAuth();
    }


    /**************************************
            * Control Panel *
     **************************************/
    private VBox userList = new VBox();
    private VBox chatList = new VBox();
    private VBox settingsList;
    public Label titleListControl;
    public ScrollPane listControl;
    public Label darkModeLabel;
    public CheckBox isDarkMode;
    public Label languageLabel;
    public ComboBox languageSelected;
    public Label notifLabel;
    public CheckBox isNotifAllowed;
    public Label deleteDataLabel;
    public Button deleteDataBtn;


    private void setTitleListControl(String title) {
        titleListControl.setText(title);
    }

    private void addUserToUserList(User user) {

        HBox hb = new HBox();
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.setPadding(new Insets(5, 30, 5, 30));
        hb.setId("hb_user_" + user.toString());

        VBox vb = new VBox();
        vb.setSpacing(2);
        hb.getChildren().add(vb);

        Label n = new Label(user.toString());
        n.getStyleClass().add("font-weight-bold");
        n.getStyleClass().add("font-size-m");
        vb.getChildren().add(n);

        Label d = new Label(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(user.getCreateDate()));
        d.setPrefHeight(16);
        vb.getChildren().add(d);

        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        hb.getChildren().add(r);

        ImageView i = new ImageView(new Image("/images/Chat.png"));
        i.setFitHeight(25);
        i.setFitWidth(25);
        i.setPickOnBounds(true);
        i.setPreserveRatio(true);
        i.setCursor(Cursor.HAND);
        i.setOnMouseClicked(e -> {
            selectedTab(tabChat);
            setTitleListControl("Conversation");
            listControl.setContent(chatList);

            openChat(user);
        });
        hb.getChildren().add(i);

        userList.getChildren().add(hb);

        Separator s = new Separator();
        s.setPadding(new Insets(5, 30, 5, 30));
        userList.getChildren().add(s);

        user.setUpdateUserFromUserList((observableValue, old_username, new_username) -> Platform.runLater(() -> {
            hb.setId("hb_user_" + new_username);
            n.setText(new_username);
        }));
        user.getUsername().addListener(user.getUpdateUserFromUserList());


    }

    private void removeUserFromUserList(User user) {
        int i = userList.getChildren().indexOf(userList.lookup("#hb_user_" + user.toString()));
        if (i != -1) {
            userList.getChildren().remove(i+1); //Remove the separator also
            userList.getChildren().remove(i);
        }
        user.getUsername().removeListener(user.getUpdateUserFromUserList());
    }

    private void addConvToChatList(ChatSession cs) {
        VBox vb = new VBox();
        vb.setPadding(new Insets(5, 30, 5, 30));
        vb.setCursor(Cursor.HAND);
        vb.setSpacing(2);
        vb.setOnMouseClicked(e -> openChat(cs));
        vb.setId("vb_cs_" + cs.getUser().toString());

        HBox hb1 = new HBox();
        vb.getChildren().add(hb1);

        Label n = new Label(cs.getUser().toString());
        n.getStyleClass().add("font-weight-bold");
        n.getStyleClass().add("font-size-m");
        hb1.getChildren().add(n);

        Region r1 = new Region();
        HBox.setHgrow(r1, Priority.ALWAYS);
        hb1.getChildren().add(r1);

        int nbMessages = cs.getMessageList().size();
        String date;
        String message = "";
        if (nbMessages == 0) {
            date = new SimpleDateFormat("HH:mm").format(new Date());
        } else {
            date = new SimpleDateFormat("HH:mm").format(cs.getMessageList().get(cs.getMessageList().size()-1).getCreatedAt());
            Message mes = cs.getMessageList().get(cs.getMessageList().size()-1);
            if (mes instanceof StringMessage) message = ((StringMessage)mes).getMessage();
            else if (mes instanceof FileMessage) message = (mes.isReceived() ? cs.getUser().toString() + " sentYou " : " youSent ") +  ((FileMessage)mes).getFileName();
        }

        Label d = new Label(date);
        hb1.getChildren().add(d);

        HBox hb2 = new HBox();
        hb2.setAlignment(Pos.CENTER_LEFT);
        vb.getChildren().add(hb2);

        Label m = new Label(message);
        m.setStyle("-fx-text-overrun: word-ellipsis;");
        hb2.getChildren().add(m);

        Region r2 = new Region();
        HBox.setHgrow(r2, Priority.ALWAYS);
        hb2.getChildren().add(r2);

        int nb_notifs = 0; //to remove
        StackPane sp = new StackPane();
        HBox.setMargin(sp, new Insets(0, 0, 0, 10));
        sp.setVisible(nb_notifs > 0);
        hb2.getChildren().add(sp);

        Circle c = new Circle(8);
        c.getStyleClass().add("notifs");
        sp.getChildren().add(c);

        Label nb = new Label(String.valueOf(nb_notifs));
        nb.getStyleClass().add("font-weight-bold");
        sp.getChildren().add(nb);

        chatList.getChildren().add(vb);

        Separator s = new Separator();
        s.setPadding(new Insets(5, 30, 5, 30));
        chatList.getChildren().add(s);

        cs.getUser().setUpdateUserFromConvList((observableValue, old_username, new_username) -> Platform.runLater(() -> {
            vb.setId("vb_cs_" + new_username);
            n.setText(new_username);
        }));
        cs.getUser().getUsername().addListener(cs.getUser().getUpdateUserFromConvList());
        cs.setUpdateMessageFromConvList(e -> {
            while (e.next()) {
                Message add = e.getAddedSubList().get(e.getAddedSize()-1);
                Platform.runLater(() -> {
                    d.setText(new SimpleDateFormat("HH:mm").format(add.getCreatedAt()));
                    if (add instanceof StringMessage) m.setText(((StringMessage)add).getMessage());
                    else if (add instanceof FileMessage) m.setText((add.isReceived() ? cs.getUser().toString() + " sentYou " : " youSent ") +  ((FileMessage)add).getFileName());
                });
                  }
        });
        cs.getMessageList().addListener(cs.getUpdateMessageFromConvList());
    }



    private void removeConvFromChatList(ChatSession cs) {
        int i = chatList.getChildren().indexOf(chatList.lookup("#vb_cs_" + cs.getUser().toString()));
        if (i != -1) {
            chatList.getChildren().remove(i+1); //Remove the separator also
            chatList.getChildren().remove(i);
        }
        cs.getUser().getUsername().removeListener(cs.getUser().getUpdateUserFromConvList());
        cs.getMessageList().removeListener(cs.getUpdateMessageFromConvList());
    }


    public void onDeleteDataClicked(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("confirm");
        alert.setHeaderText(null);
        alert.setContentText("deleteDataConfirm");
        alert.showAndWait().filter(res -> res == ButtonType.OK).ifPresent(res -> {
            Main.removeAllChatSession();
            Application.deleteDataDirectory();
        });
    }


    /**************************************
        * Username Panel *
     **************************************/
    public HBox usernameBox;
    public HBox editUsernameBox;
    public Label usernameLabel;
    public TextField editUsernameField;
    public Label connectedAsLabel;

    public void editUsername(MouseEvent mouseEvent) {
        editUsernameBox.setVisible(true);
        usernameBox.setVisible(false);
        editUsernameField.requestFocus();
        editUsernameField.end(); //Place the cursor at the end
    }

    public void checkUsername(Event event) {
        String new_username = editUsernameField.getText();
        try {
            if (new_username.equals("") || new_username.equals(usernameLabel.getText()) || User.checkUsername(new_username) && Main.getMcNetwork().editUsername(new_username)) {
                if(!new_username.equals("")) {
                    usernameLabel.setText(new_username); //if nothing is entered we keep the old username
                } else {
                    editUsernameField.setText(usernameLabel.getText()); //so we need to reset the textfield
                }
                editUsernameBox.setVisible(false);
                usernameBox.setVisible(true);
                Main.getStage().getScene().getRoot().requestFocus();
            }
        } catch (Exception err) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("notFound");
            alert.setHeaderText(null);
            alert.setContentText(err.getMessage());
            alert.showAndWait();
        }
    }


    /**************
     * CHAT PANEL *
     **************/
    public VBox noChatStarted;
    public Label chatName;
    public ScrollPane messageListPane;
    public boolean needScrolling = false;
    public VBox messageList;
    public TextField messageField;
    private ChatSession chatSessionActive = null;
    public Label noConvLabel;

    public void sendMessage(Event event) {
        if (messageField.getText().length() > 0) {
            chatSessionActive.send(messageField.getText());
            messageField.setText("");
        }
    }

    private void openChat(ChatSession cs) {
        if (chatSessionActive != null) closeChat();
        chatSessionActive = cs;
        chatName.setText(cs.getUser().toString());
        messageList.getChildren().clear();
        messageListPane.setVvalue(messageListPane.getVmax());
        cs.getMessageList().get().forEach(this::addMessageToMessageList);
        if (noChatStarted.isVisible()) noChatStarted.setVisible(false);

        cs.setUpdateMessagesFromMessageList(e -> {
            while (e.next()) {
                for (Message add : e.getAddedSubList()) {
                    Platform.runLater(() -> addMessageToMessageList(add));
                }
            }
        });
        cs.getMessageList().addListener(cs.getUpdateMessagesFromMessageList());
        cs.setUpdateUserFromConv((observableValue, old_username, new_username) -> Platform.runLater(() -> {
            chatName.setText(new_username);
        }));
        cs.getUser().getUsername().addListener(cs.getUpdateUserFromConv());
    }

    private void openChat(User user) {
        Main.getChatSessionList().get().stream()
                .filter(cs -> cs.getUser().toString().equals(user.toString()))
                .findFirst()
                .ifPresentOrElse(this::openChat, () -> {
                    try {
                        ChatSession cs = new ChatSession(new Socket(user.getAddrIp(), Main.srvPort), user);
                        Main.newChatSession(cs);
                        openChat(cs);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void closeChat() {
        chatSessionActive.getMessageList().removeListener(chatSessionActive.getUpdateMessagesFromMessageList());
        chatSessionActive.getUser().getUsername().removeListener(chatSessionActive.getUpdateUserFromConv());
    }

    private void addMessageToMessageList(Message message) {
        HBox hb = new HBox();

        if (!message.isReceived()){
            Region r = new Region();
            HBox.setHgrow(r, Priority.ALWAYS);
            hb.getChildren().add(r);
        }

        Tooltip t = new Tooltip(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(message.getCreatedAt()));

        if (message instanceof StringMessage) {
            Label m = new Label(((StringMessage)message).getMessage());
            m.getStyleClass().add(message.isReceived() ? "message-receiver" : "message-emetter");
            m.setMaxWidth(250);
            m.setPadding(new Insets(10, 15, 10, 15));
            m.setTooltip(t);
            hb.getChildren().add(m);
        }
        else if (message instanceof FileMessage) {
            FileMessage file = (FileMessage)message;
            if (file.isImage()) {
                ImageView i = new ImageView(new Image("file:///" + file.getMessage().getAbsolutePath(), 250, 0, true, true, false));
                i.setCursor(Cursor.HAND);
                i.setOnMouseClicked(e -> downloadFile(file));
                Tooltip.install(i, t);
                hb.getChildren().add(i);
            } else {
                Label m = new Label(file.getFileName());
                m.getStyleClass().add(message.isReceived() ? "message-receiver" : "message-emetter");
                m.getStyleClass().add("font-weight-bold");
                m.setUnderline(true);
                m.setMaxWidth(250);
                m.setPadding(new Insets(10, 15, 10, 15));
                m.setTooltip(t);
                m.setCursor(Cursor.HAND);
                m.setOnMouseClicked(e -> downloadFile(file));
                hb.getChildren().add(m);
            }
        }


        messageList.getChildren().add(hb);
        needScrolling = true;
    }

    private void downloadFile(FileMessage fm) {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName(fm.getFileName());
        File dest = fc.showSaveDialog(Main.getStage());
        if (dest != null) Application.downloadFile(fm.getMessage(), dest);
    }

    public void selectFile(MouseEvent mouseEvent) {
        try {
            File f = new FileChooser().showOpenDialog(Main.getStage());
            if (f != null) chatSessionActive.send(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onChatClosed(MouseEvent mouseEvent) {
        Main.removeChatSession(chatSessionActive);
    }
}
