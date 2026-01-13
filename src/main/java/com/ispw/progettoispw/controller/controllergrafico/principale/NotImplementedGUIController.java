package com.ispw.progettoispw.controller.controllergrafico.principale;

import com.ispw.progettoispw.controller.controllergrafico.GraphicController;
import javafx.fxml.FXML;

public class NotImplementedGUIController extends GraphicController {

    @FXML
    public void indietroButtonOnAction() {
        switchSafe("HomeBarbiereView.fxml", "Home");
    }
}
