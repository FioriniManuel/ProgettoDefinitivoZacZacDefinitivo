package com.ispw.progettoispw.controller.controllergrafico;

import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.exception.ViewLoadException;
import com.ispw.progettoispw.pattern.WindowManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import java.time.format.DateTimeFormatter;

public abstract class GraphicController {

    protected GraphicController() {
    }
    protected static final DateTimeFormatter TIMEF = DateTimeFormatter.ofPattern("HH:mm");

    // GraphicController.java
    protected String formatBookingRow(BookingBean b) {
        String nomeServizio = (b.getServiceName() != null && !b.getServiceName().isBlank())
                ? b.getServiceName()
                : "Servizio";

        String start = (b.getStartTime() == null)
                ? "--:--"
                : b.getStartTime().format(TIMEF);

        String end = (b.getEndTime() == null)
                ? "--:--"
                : b.getEndTime().format(TIMEF);

        String range = start + "-" + end;

        String price = (b.getPrezzoTotale() == null)
                ? "-"
                : (b.getPrezzoTotale().toPlainString() + " €");

        String stato = (b.getStatus() == null)
                ? "-"
                : b.getStatus().name();

        return nomeServizio + " | " + range + " | " + price + " | " + stato;
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