package com.ispw.progettoispw.controller.controllerGrafico;

import com.ispw.progettoispw.controller.controllerApplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerApplicativo.LoginController;
import com.ispw.progettoispw.enu.AppointmentStatus;
import com.ispw.progettoispw.exception.BusinessRuleException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.bean.BookingBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeTuePrenotazioniGUIController extends GraphicController {

    @FXML private VBox prenotazioniBox;

    private final BookingController bookingController = new BookingController();
    private final LoginController login = new LoginController();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        loadPrenotazioni();
    }

    private void loadPrenotazioni() {
        prenotazioniBox.getChildren().clear();

        String clientId = LoginController.getId();
        if (clientId == null || clientId.isBlank()) {
            prenotazioniBox.getChildren().add(new Label("Utente non loggato."));
            return;
        }

        List<BookingBean> lista = bookingController.listCustomerAppointmentsVM(clientId);
        if (lista == null || lista.isEmpty()) {
            prenotazioniBox.getChildren().add(new Label("Nessuna prenotazione trovata."));
            return;
        }

        for (BookingBean b : lista) {
            String data   = b.getDay() == null ? "-" : b.getDay().format(dateFmt);
            String inizio = b.getStartTime() == null ? "-" : b.getStartTime().format(timeFmt);
            String fine   = b.getEndTime() == null ? "-" : b.getEndTime().format(timeFmt);
            String prezzo = b.getPrezzoTotale() == null ? "-" : b.getPrezzoTotale().toPlainString() + " €";
            String stato  = b.getStatus() == null ? "-" : b.getStatus().name();
            String coupon = b.getCouponCode() == null ? "-" : b.getCouponCode();

            Label info = new Label(
                    (b.getServiceName() == null ? "Servizio" : b.getServiceName())
                            + " | " + data + " " + inizio + "-" + fine
                            + " | Prezzo: " + prezzo + " | Coupon: " + coupon + " | Stato: " + stato
            );

            Button cancelBtn = new Button("Cancella");
            cancelBtn.setStyle("-fx-background-color: #ff0b0b; -fx-text-fill: white;");
            cancelBtn.setOnAction(ev -> onCancelAppointment(b));

            HBox row = new HBox(10, info, cancelBtn);
            prenotazioniBox.getChildren().add(row);
        }
    }

    private void onCancelAppointment(BookingBean b) {
        if (b.getStatus() == AppointmentStatus.CANCELLED) {
            showInfo("Prenotazione già cancellata.");
            return;
        }
        if (b.getAppointmentId() == null || b.getAppointmentId().isBlank()) {
            showInfo("Impossibile cancellare: id appuntamento mancante.");
            return;
        }

        try {
            bookingController.cancelCustomerAppointment(b.getAppointmentId());
            showInfo("Prenotazione cancellata.");
            loadPrenotazioni();

        } catch (ValidazioneException | OggettoInvalidoException | BusinessRuleException e) {
            showError(e.getMessage());
        } catch (Exception ex) {
            showError("Errore tecnico. Riprova più tardi.");
        }
    }

    @FXML
    private void esciLeTuePrenotazionionAction() {
        switchSafe("HomeView.fxml", "Home");
    }
}
