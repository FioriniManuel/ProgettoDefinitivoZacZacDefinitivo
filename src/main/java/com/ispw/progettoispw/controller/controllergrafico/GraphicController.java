package com.ispw.progettoispw.controller.controllergrafico;

import com.ispw.progettoispw.exception.ViewLoadException;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public abstract class GraphicController {

    protected GraphicController() {
    }

    public static void showAlert(String msg){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
    protected void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    protected void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    /* =========================
       LABEL HELPERS
       ========================= */

    protected void setError(Label label, String msg) {
        if (label != null) {
            label.setText(msg);
        } else {
            showError(msg);
        }
    }

    protected void clear(Label label) {
        if (label != null) {
            label.setText("");
        }
    }
    protected void switchSafe(String fxmlFile, String title) {
        try {
            WindowManager.getInstance().switchScene(fxmlFile, title);
        } catch (ViewLoadException e) {
            // come richiesto: usa showAlert SOLO per cambio view
            GraphicController.showAlert("Errore tecnico. Riavvia l'applicazione.");
        }


    }}