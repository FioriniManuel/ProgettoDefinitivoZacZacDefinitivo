package com.ispw.progettoispw.controller.controllergrafico;

import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.controller.controllerapplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerapplicativo.LoginController;
import com.ispw.progettoispw.enu.AppointmentStatus;
import com.ispw.progettoispw.exception.BusinessRuleException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeTuePrenotazioniGUIController extends GraphicController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String MSG_NOT_LOGGED = "Utente non loggato.";
    private static final String MSG_NONE_FOUND = "Nessuna prenotazione trovata.";
    private static final String MSG_ALREADY_CANCELLED = "Prenotazione già cancellata.";
    private static final String MSG_MISSING_APPOINTMENT_ID = "Impossibile cancellare: id appuntamento mancante.";
    private static final String MSG_CANCELLED = "Prenotazione cancellata.";
    private static final String MSG_TECH_ERROR = "Errore tecnico. Riprova più tardi.";

    @FXML private VBox prenotazioniBox;

    private final BookingController bookingController = new BookingController();

    @FXML
    private void initialize() {
        loadPrenotazioni();
    }

    private void loadPrenotazioni() {
        prenotazioniBox.getChildren().clear();

        String clientId = LoginController.getId();
        if (isBlank(clientId)) {
            showMessage(MSG_NOT_LOGGED);
            return;
        }

        List<BookingBean> lista = bookingController.listCustomerAppointmentsVM(clientId);
        if (lista == null || lista.isEmpty()) {
            showMessage(MSG_NONE_FOUND);
            return;
        }

        lista.forEach(this::addPrenotazioneRow);
    }

    private void addPrenotazioneRow(BookingBean b) {
        Label info = new Label(buildRowText(b));
        Button cancelBtn = buildCancelButton(b);

        HBox row = new HBox(10, info, cancelBtn);
        prenotazioniBox.getChildren().add(row);
    }

    private Button buildCancelButton(BookingBean b) {
        Button cancelBtn = new Button("Cancella");
        cancelBtn.setStyle("-fx-background-color: #ff0b0b; -fx-text-fill: white;");
        cancelBtn.setOnAction(ev -> onCancelAppointment(b));
        cancelBtn.setDisable(isCancelDisabled(b));
        return cancelBtn;
    }

    private boolean isCancelDisabled(BookingBean b) {
        return b == null
                || b.getStatus() == AppointmentStatus.CANCELLED
                || isBlank(b.getAppointmentId());
    }

    private void onCancelAppointment(BookingBean b) {
        if (!canCancel(b)) return;

        try {
            bookingController.cancelCustomerAppointment(b.getAppointmentId());
            showInfo(MSG_CANCELLED);
            loadPrenotazioni();
        } catch (ValidazioneException | OggettoInvalidoException | BusinessRuleException e) {
            showError(e.getMessage());
        } catch (Exception ex) {
            showError(MSG_TECH_ERROR);
        }
    }

    private boolean canCancel(BookingBean b) {
        if (b == null) return false;

        if (b.getStatus() == AppointmentStatus.CANCELLED) {
            showInfo(MSG_ALREADY_CANCELLED);
            return false;
        }
        if (isBlank(b.getAppointmentId())) {
            showInfo(MSG_MISSING_APPOINTMENT_ID);
            return false;
        }
        return true;
    }

    private String buildRowText(BookingBean b) {
        if (b == null) return "-";

        String service = defaultIfBlank(b.getServiceName(), "Servizio");
        String data = formatDate(b.getDay());
        String inizio = formatTime(b.getStartTime());
        String fine = formatTime(b.getEndTime());
        String prezzo = formatPrice(b.getPrezzoTotale());
        String coupon = defaultIfBlank(b.getCouponcode(), "-");
        String stato = (b.getStatus() == null) ? "-" : b.getStatus().name();

        return service + " | " + data + " " + inizio + "-" + fine
                + " | Prezzo: " + prezzo + " | Coupon: " + coupon + " | Stato: " + stato;
    }

    private String formatDate(LocalDate d) {
        return (d == null) ? "-" : d.format(DATE_FMT);
    }

    private String formatTime(LocalTime t) {
        return (t == null) ? "-" : t.format(TIME_FMT);
    }

    private String formatPrice(BigDecimal p) {
        return (p == null) ? "-" : (p.toPlainString() + " €");
    }

    private String defaultIfBlank(String s, String fallback) {
        return isBlank(s) ? fallback : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private void showMessage(String msg) {
        prenotazioniBox.getChildren().add(new Label(msg));
    }

    @FXML
    private void esciLeTuePrenotazionionAction() {
        switchSafe("HomeView.fxml", "Home");
    }
}
