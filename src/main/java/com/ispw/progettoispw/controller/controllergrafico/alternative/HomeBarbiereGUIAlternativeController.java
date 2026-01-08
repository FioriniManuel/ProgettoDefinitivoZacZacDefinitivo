package com.ispw.progettoispw.controller.controllergrafico.alternative;

import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.controller.controllerapplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerapplicativo.CouponController;
import com.ispw.progettoispw.controller.controllerapplicativo.LoginController;
import com.ispw.progettoispw.controller.controllerapplicativo.LoyaltyController;
import com.ispw.progettoispw.controller.controllergrafico.GraphicController;
import com.ispw.progettoispw.enu.AppointmentStatus;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.function.UnaryOperator;

public class HomeBarbiereGUIAlternativeController extends GraphicController {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private static final String PLACEHOLDER_NO_APPOINTMENTS = "Nessuna prenotazione per la data selezionata.";
    private static final String MSG_TECH_ERROR = "Errore tecnico.";
    private static final String MSG_ALREADY_CANCELLED = "Appuntamento già cancellato.";
    private static final String MSG_ALREADY_COMPLETED = "Appuntamento già completato.";
    private static final String MSG_CANNOT_CANCEL_COMPLETED = "Impossibile cancellare: già completato.";

    @FXML private Label nomeBarbiere;
    @FXML private Button btnEsci;

    @FXML private TextField dateTextField; // dd/MM/yyyy
    @FXML private ListView<BookingBean> appointmentsList;

    private final BookingController bookingController = new BookingController();
    private final CouponController couponController = new CouponController();
    private final LoyaltyController loyal = new LoyaltyController();

    private String barberId;

    @FXML
    private void initialize() {
        setupBarberInfo();
        setupLogout();

        setupAppointmentsList();
        setupDateField();

        applyDateFromField();
    }

    private void setupBarberInfo() {
        barberId = LoginController.getId();

        String barberName = LoginController.getName();
        String displayName = (barberName != null && !barberName.isBlank()) ? barberName : "Barbiere";
        nomeBarbiere.setText(displayName);
    }

    private void setupLogout() {
        btnEsci.setOnAction(e -> doLogout());
    }

    private void setupAppointmentsList() {
        if (appointmentsList == null) return;

        appointmentsList.setPlaceholder(new Label(PLACEHOLDER_NO_APPOINTMENTS));
        appointmentsList.setCellFactory(list -> new AppointmentCell());
    }

    private void setupDateField() {
        if (dateTextField == null) return;

        LocalDate today = LocalDate.now();
        dateTextField.setTextFormatter(new TextFormatter<>(dateMaskFilter()));
        dateTextField.setText(DF.format(today));

        dateTextField.setOnAction(e -> applyDateFromField());
        dateTextField.focusedProperty().addListener((obs, was, is) -> {
            if (is) return; // guard clause -> riduce complessità
            applyDateFromField();
        });
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
            completeBtn.setOnAction(e -> onComplete(getItem(), this::render));
            cancelBtn.setOnAction(e -> onCancel(getItem(), this::render));
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
    }

    private boolean isFinalState(AppointmentStatus status) {
        return status == AppointmentStatus.CANCELLED || status == AppointmentStatus.COMPLETED;
    }

    private void onComplete(BookingBean b, Runnable refreshUi) {
        if (b == null) return;

        if (b.getStatus() == AppointmentStatus.CANCELLED) { showInfo(MSG_ALREADY_CANCELLED); return; }
        if (b.getStatus() == AppointmentStatus.COMPLETED) { showInfo(MSG_ALREADY_COMPLETED); return; }

        try {
            bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.COMPLETED);
            b.setStatus(AppointmentStatus.COMPLETED);

            addLoyaltyPointsIfPossible(b);

            refreshUi.run();
        } catch (ValidazioneException | OggettoInvalidoException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError(MSG_TECH_ERROR);
        }
    }

    private void onCancel(BookingBean b, Runnable refreshUi) {
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

    private void addLoyaltyPointsIfPossible(BookingBean b) {
        if (b.getClienteId() == null || b.getClienteId().isBlank()) return;

        int pt = couponController.computePointsToAward(b.getPrezzoTotale());
        loyal.addPoints(b.getClienteId(), pt);
    }

    private UnaryOperator<Change> dateMaskFilter() {
        return c -> {
            String newText = c.getControlNewText();
            if (newText.length() > 10) return null;
            if (!newText.matches("[0-9/]*")) return null;
            return c;
        };
    }

    private void applyDateFromField() {
        if (appointmentsList == null || dateTextField == null) return;

        LocalDate day = parseOrFallbackDate(dateTextField.getText());
        dateTextField.setText(DF.format(day));
        loadAppointments(day);
    }

    private LocalDate parseOrFallbackDate(String rawInput) {
        String raw = rawInput == null ? "" : rawInput.trim();
        try {
            return LocalDate.parse(raw, DF);
        } catch (Exception ex) {
            LocalDate today = LocalDate.now();
            showInfo("Formato data non valido. Usa dd/MM/yyyy (es. " + DF.format(today) + ").");
            return today;
        }
    }

    private void loadAppointments(LocalDate day) {
        if (appointmentsList == null) return;

        if (barberId == null || barberId.isBlank()) {
            appointmentsList.setItems(FXCollections.observableArrayList());
            return;
        }

        List<BookingBean> list = bookingController.listAppointmentsForBarberOnDayVM(barberId, day);
        appointmentsList.setItems(FXCollections.observableArrayList(list));
    }

    private String buildRowText(BookingBean b) {
        String nomeServizio = (b.getServiceName() != null && !b.getServiceName().isBlank())
                ? b.getServiceName() : "Servizio";

        String range = (b.getStartTime() == null || b.getEndTime() == null)
                ? "-"
                : (b.getStartTime().format(TF) + "-" + b.getEndTime().format(TF));

        String price = (b.getPrezzoTotale() == null) ? "-" : (b.getPrezzoTotale().toPlainString() + " €");
        String stato = (b.getStatus() == null) ? "-" : b.getStatus().name();

        return nomeServizio + " | " + range + " | " + price + " | " + stato;
    }

    @FXML
    private void notImplementedOnAction() {
        switchSafe("NotImplementedView.fxml", "Gestione Fidelity");
    }

    private void doLogout() {
        LoginController.logOut();
        switchSafe("LoginViewAlternative.fxml", "Zac Zac");
    }
}
