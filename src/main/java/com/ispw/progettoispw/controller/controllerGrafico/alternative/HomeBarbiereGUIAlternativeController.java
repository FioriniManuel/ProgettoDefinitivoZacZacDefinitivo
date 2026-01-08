package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.CouponController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoyaltyController;
import com.ispw.progettoispw.Controller.ControllerGrafico.GraphicController;
import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.Exception.OggettoInvalidoException;
import com.ispw.progettoispw.Exception.ValidazioneException;
import com.ispw.progettoispw.bean.BookingBean;
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

    @FXML private Label nomeBarbiere;
    @FXML private Button btnEsci;

    @FXML private TextField dateTextField; // dd/MM/yyyy
    @FXML private ListView<BookingBean> appointmentsList;

    private final LoginController login = new LoginController();
    private final BookingController bookingController = new BookingController();
    private final CouponController couponController = new CouponController();
    private final LoyaltyController loyal = new LoyaltyController();

    private String barberId;

    private final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        barberId = LoginController.getId();
        String barberName = LoginController.getName();
        nomeBarbiere.setText((barberName != null && !barberName.isBlank()) ? barberName : "Barbiere");

        btnEsci.setOnAction(e -> doLogout());

        if (appointmentsList != null) {
            appointmentsList.setPlaceholder(new Label("Nessuna prenotazione per la data selezionata."));
        }

        if (dateTextField != null) {
            dateTextField.setTextFormatter(new TextFormatter<>(dateMaskFilter()));
            LocalDate today = LocalDate.now();
            dateTextField.setText(DF.format(today));
            dateTextField.setOnAction(e -> applyDateFromField());
            dateTextField.focusedProperty().addListener((obs, was, is) -> {
                if (!is) applyDateFromField();
            });
        }

        if (appointmentsList != null) {
            appointmentsList.setCellFactory(list -> new ListCell<>() {
                private final Label lbl = new Label();
                private final Button completeBtn = new Button("Completa");
                private final Button cancelBtn = new Button("Cancella");
                private final HBox box = new HBox(10, lbl, completeBtn, cancelBtn);

                {
                    cancelBtn.setStyle("-fx-background-color: #ff0b0b; -fx-text-fill: white;");
                    completeBtn.setStyle("-fx-background-color: #63c755; -fx-text-fill: white;");

                    completeBtn.setOnAction(e -> onComplete(getItem(), lbl, completeBtn, cancelBtn));
                    cancelBtn.setOnAction(e -> onCancel(getItem(), lbl, completeBtn, cancelBtn));
                }

                @Override
                protected void updateItem(BookingBean b, boolean empty) {
                    super.updateItem(b, empty);
                    if (empty || b == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        lbl.setText(buildRowText(b));
                        completeBtn.setDisable(b.getStatus() == AppointmentStatus.CANCELLED
                                || b.getStatus() == AppointmentStatus.COMPLETED);
                        cancelBtn.setDisable(b.getStatus() == AppointmentStatus.CANCELLED
                                || b.getStatus() == AppointmentStatus.COMPLETED);
                        setGraphic(box);
                    }
                }
            });
        }

        applyDateFromField();
    }

    private void onComplete(BookingBean b, Label lbl, Button completeBtn, Button cancelBtn) {
        if (b == null) return;

        if (b.getStatus() == AppointmentStatus.CANCELLED) { showInfo("Appuntamento già cancellato."); return; }
        if (b.getStatus() == AppointmentStatus.COMPLETED) { showInfo("Appuntamento già completato."); return; }

        try {
            bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.COMPLETED);
            b.setStatus(AppointmentStatus.COMPLETED);

            int pt = couponController.computePointsToAward(b.getPrezzoTotale());
            if (b.getClienteId() != null && !b.getClienteId().isBlank()) {
                loyal.addPoints(b.getClienteId(), pt);
            }

            lbl.setText(buildRowText(b));
            completeBtn.setDisable(true);
            cancelBtn.setDisable(true);

        } catch (ValidazioneException | OggettoInvalidoException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Errore tecnico.");
        }
    }

    private void onCancel(BookingBean b, Label lbl, Button completeBtn, Button cancelBtn) {
        if (b == null) return;

        if (b.getStatus() == AppointmentStatus.CANCELLED) { showInfo("Appuntamento già cancellato."); return; }
        if (b.getStatus() == AppointmentStatus.COMPLETED) { showInfo("Impossibile cancellare: già completato."); return; }

        try {
            bookingController.updateAppointmentStatus(b.getAppointmentId(), AppointmentStatus.CANCELLED);
            b.setStatus(AppointmentStatus.CANCELLED);

            lbl.setText(buildRowText(b));
            completeBtn.setDisable(true);
            cancelBtn.setDisable(true);

        } catch (ValidazioneException | OggettoInvalidoException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Errore tecnico.");
        }
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

        String raw = dateTextField.getText() == null ? "" : dateTextField.getText().trim();
        LocalDate day;
        try {
            day = LocalDate.parse(raw, DF);
        } catch (Exception ex) {
            day = LocalDate.now();
            dateTextField.setText(DF.format(day));
            showInfo("Formato data non valido. Usa dd/MM/yyyy (es. " + DF.format(day) + ").");
        }

        loadAppointments(day);
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
                ? b.getServiceName() : "Servizio";
        String range = (b.getStartTime() == null || b.getEndTime() == null)
                ? "-" : (b.getStartTime().format(TF) + "-" + b.getEndTime().format(TF));
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
