package com.ispw.progettoispw.controller.controllergrafico;

import javafx.event.ActionEvent;

public class IniziamoGUIController extends GraphicController {


    public void loginButtonOnAction() {
        switchSafe("LoginView.fxml", "ZAC ZAC");
    }


    public void prenotazioneButtonOnAction() {
        switchSafe("RegistrationView.fxml", "Registrati");
    }
}
