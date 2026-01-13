package com.ispw.progettoispw.controller.controllergrafico.principale;

import com.ispw.progettoispw.controller.controllergrafico.GraphicController;

public class IniziamoGUIController extends GraphicController {


    public void loginButtonOnAction() {
        switchSafe("LoginView.fxml", "ZAC ZAC");
    }


    public void prenotazioneButtonOnAction() {
        switchSafe("RegistrationView.fxml", "Registrati");
    }
}
