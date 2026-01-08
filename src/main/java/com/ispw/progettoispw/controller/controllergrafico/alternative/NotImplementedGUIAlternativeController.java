package com.ispw.progettoispw.controller.controllergrafico.alternative;

import com.ispw.progettoispw.controller.controllergrafico.GraphicController;
import javafx.fxml.FXML;

public class NotImplementedGUIAlternativeController extends GraphicController {

    @FXML
    public void indietroButtonOnAction() {
        switchSafe("HomeBarbiereViewAlternative.fxml", "Home");
    }
}
