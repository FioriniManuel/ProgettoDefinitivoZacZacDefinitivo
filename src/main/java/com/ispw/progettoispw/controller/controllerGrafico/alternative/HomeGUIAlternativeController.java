package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Controller.ControllerGrafico.GraphicController;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class HomeGUIAlternativeController extends GraphicController {

    @FXML private Label nameLabel;
    @FXML private RadioButton areaPersonaleRadio;
    @FXML private RadioButton prenotaRadio;
    @FXML private Button backButton;
    @FXML private Button continueButton;

    private final ToggleGroup choiceGroup = new ToggleGroup();

    @FXML
    public void initialize() {
        String displayName = LoginController.getName();
        nameLabel.setText((displayName == null || displayName.isBlank()) ? "Ciao!" : "Ciao, " + displayName + "!");

        areaPersonaleRadio.setToggleGroup(choiceGroup);
        prenotaRadio.setToggleGroup(choiceGroup);

        continueButton.setDefaultButton(true);
        backButton.setCancelButton(true);
    }

    @FXML
    private void onBack() {
        LoginController.logOut();
        switchSafe("LoginViewAlternative.fxml", "Login");
    }

    @FXML
    private void onContinue() {
        if (choiceGroup.getSelectedToggle() == null) {
            showWarn("Scegli un'opzione prima di continuare.");
            return;
        }

        if (areaPersonaleRadio.isSelected()) {
            switchSafe("AreaPersonaleViewAlternative.fxml", "Area Personale");
        } else {
            switchSafe("PrenotazioneViewAlternative.fxml", "Prenota Appuntamento");
        }
    }

    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
