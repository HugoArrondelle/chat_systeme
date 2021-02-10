package com.chat.Controllers;


import com.chat.Main;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;


public class AuthController implements Initializable {
    private Main app;
    private double xOffset = 0;
    private double yOffset = 0;
    public TextField pseudo;
    public Label authError;
    public Label labelSysteme;
    public Label labelConnexion;
    public Label enterLabel;


    //Link to the Main
    public void setApp(Main app) {
        this.app = app;
    }


    //Initialize controller
    @Override
    public void initialize(URL var1, ResourceBundle var2) {
        labelSysteme.setText("Système de Chat");
        labelConnexion.setText("Connexion");
        pseudo.setPromptText("Pseudo");
    }

    //Close the app
    public void onCloseClicked(MouseEvent me) {
        Main.closeChatSessionServer();
        Platform.exit();
    }
    public void onMinimizeClicked(MouseEvent me) {
        Main.getStage().setIconified(true);
    }


    //Check username on enter key pressed or on enter button clicked
    public void onEnterClicked(Event e){
        String pseudo = this.pseudo.getText();
        try{
            if (Main.getUser().checkUsername(pseudo) && Main.getMcNetwork().signIn(pseudo)){
                app.startChat();
            }
        } catch (Exception err) {
            authError.setText(err.getMessage());
        }


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

}
