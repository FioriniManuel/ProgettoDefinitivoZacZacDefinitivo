package com.ispw.progettoispw.controller.controllergrafico;

import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.controller.controllerapplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerapplicativo.CouponController;
import com.ispw.progettoispw.controller.controllerapplicativo.LoginController;
import com.ispw.progettoispw.controller.controllerapplicativo.LoyaltyController;
import com.ispw.progettoispw.enu.AppointmentStatus;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PrenotazioniTabGUIController extends GraphicController {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private static final String MSG_NO_APPOINTMENTS = "Nessuna prenotazione per la data selezionata.";
    private static final String MSG_TECH_ERROR = "Errore tecnico.";
    private static final String MSG_ALREADY_CANCELLED = "Appuntamento già cancellato.";
    private static final String MSG_ALREADY_COMPLETED = "Appuntamento già completato.";
    private static final String MSG_CANNOT_CANCEL_COMPLETED = "Impossibile cancellare: appuntamento già completato.";

    @FXML private DatePicker datePicker;
    @FXML private Label dateLabel;
    @FXML private ListView<BookingBean> appointmentsList;

    private final CouponController couponController = new CouponController();
    private final BookingController bookingController = new BookingController();
    private final LoyaltyController loyal = new LoyaltyController();

    private String barberId;

    @FXML
    private void initialize() {
        setupUi();
        barberId = LoginController.getId();

        LocalDate today = LocalDate.now();
        setSelectedDate(today);

        setupListeners();
        setupAppointmentsList();

        loadAppointments(today);
    }

    private void setupUi() {
        appointmentsList.setPlaceholder(new Label(MSG_NO_APPOINTMENTS));
    }

    private void setupListeners() {
        datePicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            setSelectedDate(newV);
            loadAppointments(newV);
        });
    }

    private void setupAppointmentsList() {
        appointmentsList.setCellFactory(list -> new AppointmentCell());
    }

    private void setSelectedDate(LocalDate date) {
        datePicker.setValue(date);
        dateLabel.setText(DF.format(date));
    }

    private void loadAppointments(LocalDate day) {
        if (barberId == null || barberId.isBlank()) {
            appointmentsList.setItems(FXCollections.observableArrayList());
            return;
        }

        List<BookingBean> list = bookingController.listAppointmentsForBarberOnDayVM(barberId, day);
        appointmentsList.setItems(FXCollections.observableArrayList(list));
    }

    private String buildRowText(BookingBean b) {
        String nomeServizio = (b.getServiceName() != null && !b.getServiceName().isBlank())
                ? b.getServiceName()
                : "Servizio";

        String start = (b.getStartTime() == null) ? "--:--" : b.getStartTime().format(TF);
        String end   = (b.getEndTime() == null) ? "--:--" : b.getEndTime().format(TF);
        String range = start + "-" + end;

        String price = (b.getPrezzoTotale() == null) ? "-" : (b.getPrezzoTotale().toPlainString() + " €");
        String stato = (b.getStatus() == null) ? "-" : b.getStatus().name();

        return nomeServizio + " | " + range + " | " + price + " | " + stato;
    }

    private boolean isFinalState(BookingBean b) {
        return b.getStatus() == AppointmentStatus.CANCELLED || b.getStatus() == AppointmentStatus.COMPLETED;
    }

    private void handleComplete(BookingBean b, Runnable refreshUi) {
        if (b == null) return;

        if (b.getStatus() == AppointmentStatus.CANCELLED) { showInfo(MSG_ALREADY_CANCELLED); return; }
        if (b.getStatus() == AppointmentStatus.COMPLETED) { showInfo(MSG_ALREADY_COMPLETED); return; }

        try {
            bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.COMPLETED);
            b.setStatus(AppointmentStatus.COMPLETED);

            int pt = couponController.computePointsToAward(b.getPrezzoTotale());
            loyal.addPoints(b.getClienteId(), pt);

            refreshUi.run();
        } catch (ValidazioneException | OggettoInvalidoException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError(MSG_TECH_ERROR);
        }
    }

    private void handleCancel(BookingBean b, Runnable refreshUi) {
        if (b == null) return;

        if (b.getStatus() == AppointmentStatus.CANCELLED) { showInfo(MSG_ALREADY_CANCELLED); return; }
        if (b.getStatus() == AppointmentStatus.COMPLETED) { showInfo(MSG_CANNOT_CANCEL_COMPLETED); return; }

        try {
            bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.CANCELLED);
            b.setStatus(AppointmentStatus.CANCELLED);

            refreshUi.run();
        } catch (ValidazioneException | OggettoInvalidoException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError(MSG_TECH_ERROR);
        }
    }

    private final class AppointmentCell extends ListCell<BookingBean> {

        private final Label lbl = new Label();
        private final Button completeBtn = new Button("Completa");
        private final Button cancelBtn = new Button("Cancella");
        private final HBox box = new HBox(10, lbl, completeBtn, cancelBtn);

        private AppointmentCell() {
            styleButtons();
            wireActions();
        }

        private void styleButtons() {
            cancelBtn.setStyle("-fx-background-color: #ff0b0b; -fx-text-fill: white;");
            completeBtn.setStyle("-fx-background-color: #63c755; -fx-text-fill: white;");
        }

        private void wireActions() {
            completeBtn.setOnAction(e -> handleComplete(getItem(), this::render));
            cancelBtn.setOnAction(e -> handleCancel(getItem(), this::render));
        }

        @Override
        protected void updateItem(BookingBean b, boolean empty) {
            super.updateItem(b, empty);
            if (empty || b == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            render();
        }

        private void render() {
            BookingBean b = getItem();
            if (b == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            lbl.setText(buildRowText(b));
            updateButtons(b);
            setGraphic(box);
        }

        private void updateButtons(BookingBean b) {
            boolean disable = isFinalState(b);
            completeBtn.setDisable(disable);
            cancelBtn.setDisable(disable);
        }
    }

    @FXML
    public void indietroButtonOnAction() {
        switchSafe("HomeBarbiereView.fxml", "Home");
    }
}
