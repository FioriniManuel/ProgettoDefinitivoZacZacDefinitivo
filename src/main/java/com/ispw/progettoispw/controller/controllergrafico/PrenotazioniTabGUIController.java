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

    private static final DateTimeFormatter DATEF = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIMEF = DateTimeFormatter.ofPattern("HH:mm");

    private static final String MSG_NONE_APPOINTMENTS = "Nessuna prenotazione per la data selezionata.";
    private static final String ERROR = "Errore tecnico.";
    private static final String CANCELLED = "Appuntamento già cancellato.";
    private static final String COMPLETED = "Appuntamento già completato.";
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
        appointmentsList.setPlaceholder(new Label(MSG_NONE_APPOINTMENTS));
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
        dateLabel.setText(DATEF.format(date));
    }

    private void loadAppointments(LocalDate day) {
        if (barberId == null || barberId.isBlank()) {
            appointmentsList.setItems(FXCollections.observableArrayList());
            return;
        }

        List<BookingBean> list = bookingController.listAppointmentsForBarberOnDayVM(barberId, day);
        appointmentsList.setItems(FXCollections.observableArrayList(list));
    }



    private void handleComplete(BookingBean b, Runnable refreshUi) {
        if (b == null) return;

        if (b.getStatus() == AppointmentStatus.CANCELLED) { showInfo(CANCELLED); return; }
        if (b.getStatus() == AppointmentStatus.COMPLETED) { showInfo(COMPLETED); return; }

        try {
            bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.COMPLETED);
            b.setStatus(AppointmentStatus.COMPLETED);

            int pt = couponController.computePointsToAward(b.getPrezzoTotale());
            loyal.addPoints(b.getClienteId(), pt);

            refreshUi.run();
        } catch (ValidazioneException | OggettoInvalidoException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError(ERROR);
        }
    }

    private void handleCancel(BookingBean b, Runnable refreshUi) {
        if (b == null) return;

        if (b.getStatus() == AppointmentStatus.CANCELLED) { showInfo(CANCELLED); return; }
        if (b.getStatus() == AppointmentStatus.COMPLETED) { showInfo(MSG_CANNOT_CANCEL_COMPLETED); return; }

        try {
            bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.CANCELLED);
            b.setStatus(AppointmentStatus.CANCELLED);

            refreshUi.run();
        } catch (ValidazioneException | OggettoInvalidoException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError(ERROR);
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
            boolean disable = isFinalState(b.getStatus());
            completeBtn.setDisable(disable);
            cancelBtn.setDisable(disable);
        }

    /* ===========================
       METODI SPOSTATI QUI
       =========================== */

        private boolean isFinalState(AppointmentStatus status) {
            return status == AppointmentStatus.CANCELLED
                    || status == AppointmentStatus.COMPLETED;
        }

        private String buildRowText(BookingBean b) {
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
    }


    @FXML
    public void indietroButtonOnAction() {
        switchSafe("HomeBarbiereView.fxml", "Home");
    }
}
