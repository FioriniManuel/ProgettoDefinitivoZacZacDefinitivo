package com.ispw.progettoispw.Controller.ControllerGrafico;

import com.ispw.progettoispw.Controller.ControllerApplicativo.MainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class MainGUIController extends GraphicController {

    @FXML private ToggleButton togglePersistenza;
    @FXML private RadioButton primaInterfaccia;
    @FXML private RadioButton secondaInterfaccia;

    private final MainController main = new MainController();

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        primaInterfaccia.setToggleGroup(group);
        secondaInterfaccia.setToggleGroup(group);
        primaInterfaccia.setSelected(true);
    }

    @FXML
    public void continueButtonOnAction(ActionEvent event) {

        if (togglePersistenza.isSelected()) main.persistenza();
        else main.memory();

        if (primaInterfaccia.isSelected()) {
            switchSafe("IniziamoView.fxml", "Iniziamo");
        } else {
            switchSafe("LoginViewAlternative.fxml", "Login");
        }
    }
}
