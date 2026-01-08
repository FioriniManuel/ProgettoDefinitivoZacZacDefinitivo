package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.util.Optional;

public class HomeGUIController extends GraphicController {

    @FXML private Label welcomeLabel;

    private final LoginController loginText = new LoginController();

    @FXML
    public void initialize() {
        String nomeCompleto = LoginController.getName();
        welcomeLabel.setText(nomeCompleto == null ? "" : nomeCompleto);
    }

    @FXML
    public void prenotaAppuntamentoButtonOnAction(ActionEvent event) {
        switchSafe("PrenotazioneView.fxml", "Listino");
    }

    @FXML
    public void fidelityCardOnAction(ActionEvent event) {
        switchSafe("FidelityCardView.fxml", "Fidelity Card");
    }

    @FXML
    public void leTuePrenotazioniButtonOnAction(ActionEvent event) {
        switchSafe("LeTuePrenotazioniView.fxml", "Le Tue Prenotazioni");
    }

    @FXML
    public void exitButtonOnAction(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Conferma uscita");
        confirm.setHeaderText("Vuoi davvero uscire?");
        confirm.setContentText("Premi Conferma per disconnetterti oppure Annulla per restare.");
        confirm.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        ButtonType conferma = new ButtonType("Conferma", ButtonBar.ButtonData.OK_DONE);
        ButtonType annulla  = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(conferma, annulla);

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == conferma) {
            LoginController.logOut();
            switchSafe("LoginView.fxml", "Login");
        }
    }
}
