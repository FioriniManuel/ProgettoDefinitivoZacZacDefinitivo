package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HomeBarbiereGUIController extends GraphicController {

    @FXML private Label nomeBarbiere;
    @FXML private Button btnEsci;

    private final LoginController loginController = new LoginController();

    @FXML
    private void initialize() {
        String barberName = LoginController.getName();
        nomeBarbiere.setText((barberName != null && !barberName.isBlank()) ? barberName : "Barbiere");

        btnEsci.setOnAction(e -> doLogout());
    }

    @FXML
    private void listaPrenotazioniOnAction() {
        switchSafe("ListaPrenotazioniView.fxml", "Prenotazioni");
    }

    @FXML
    private void notImplementedOnAction() {
        switchSafe("GestisciFidelityCardView.fxml", "Gestione Fidelity");
    }

    private void doLogout() {
        LoginController.logOut();
        switchSafe("LoginView.fxml", "Zac Zac");
    }
}
