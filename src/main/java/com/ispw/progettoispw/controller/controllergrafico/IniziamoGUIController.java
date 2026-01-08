package com.ispw.progettoispw.controller.controllergrafico;

import javafx.event.ActionEvent;

public class IniziamoGUIController extends GraphicController {


    public void loginButtonOnAction(ActionEvent event) {
        switchSafe("LoginView.fxml", "ZAC ZAC");
    }


    public void prenotazioneButtonOnAction(ActionEvent event) {
        switchSafe("RegistrationView.fxml", "Registrati");
    }
}
