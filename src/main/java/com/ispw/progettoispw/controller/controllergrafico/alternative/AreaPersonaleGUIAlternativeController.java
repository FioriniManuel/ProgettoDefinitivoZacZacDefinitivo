package com.ispw.progettoispw.controller.controllergrafico.alternative;

import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.controller.controllerapplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerapplicativo.LoginController;
import com.ispw.progettoispw.controller.controllergrafico.GraphicController;
import com.ispw.progettoispw.enu.AppointmentStatus;
import com.ispw.progettoispw.exception.BusinessRuleException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AreaPersonaleGUIAlternativeController extends GraphicController {

    private static final String MSG_NO_BOOKINGS = "Nessuna prenotazione trovata.";
    private static final String MSG_NOT_LOGGED = "Utente non loggato.";
    private static final String MSG_MISSING_ID = "Impossibile cancellare: id appuntamento mancante.";
    private static final String MSG_CANCELLED = "Prenotazione cancellata.";
    private static final String MSG_TECH_ERROR = "Errore tecnico. Riprova più tardi.";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private ListView<BookingBean> listView;
    @FXML private Button backButton;
    @FXML private Button fidelityButton;

    private final BookingController bookingController = new BookingController();
    private final ObservableList<BookingBean> items = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        setupListView();
        loadAppointments();
    }

    private void setupListView() {
        if (listView == null) return;

        listView.setItems(items);
        listView.setPlaceholder(new Label(MSG_NO_BOOKINGS));
        listView.setCellFactory(lv -> new BookingCell());
    }

    private void loadAppointments() {
        items.clear();

        String clientId = LoginController.getId();
        if (isBlank(clientId)) {
            if (listView != null) {
                listView.setPlaceholder(new Label(MSG_NOT_LOGGED));
            }
            return;
        }

        List<BookingBean> lista = bookingController.listCustomerAppointmentsVM(clientId);
        if (lista == null || lista.isEmpty()) {
            if (listView != null) {
                listView.setPlaceholder(new Label(MSG_NO_BOOKINGS));
            }
            return;
        }

        items.addAll(lista);
    }

    private void onCancel(BookingBean b) {
        if (b == null || isBlank(b.getAppointmentId())) {
            showInfo(MSG_MISSING_ID);
            return;
        }

        try {
            bookingController.cancelCustomerAppointment(b.getAppointmentId());
            showInfo(MSG_CANCELLED);
            loadAppointments();

        } catch (ValidazioneException | OggettoInvalidoException | BusinessRuleException e) {
            showError(e.getMessage());
        } catch (Exception ex) {
            showError(MSG_TECH_ERROR);
        }
    }

    private final class BookingCell extends ListCell<BookingBean> {

        private final Label info = new Label();
        private final Button cancel = new Button("Cancella");
        private final HBox row = new HBox(10, info, cancel);

        private BookingCell() {
            cancel.setStyle("-fx-background-color:#ff0b0b; -fx-text-fill:white;");
            cancel.setOnAction(e -> onCancel(getItem()));
            row.setFillHeight(true);
        }

        @Override
        protected void updateItem(BookingBean b, boolean empty) {
            super.updateItem(b, empty);
            if (empty || b == null) {
                setText(null);
                setGraphic(null);
                return;
            }
            render(b);
        }

        private void render(BookingBean b) {
            info.setText(buildRowText(b));
            cancel.setDisable(isCancelDisabled(b));
            setGraphic(row);
            setText(null);
        }

        // Spostato qui (richiesta Sonar): è logica UI della cella
        private boolean isCancelDisabled(BookingBean b) {
            return b == null
                    || b.getStatus() == AppointmentStatus.CANCELLED
                    || b.getStatus() == AppointmentStatus.COMPLETED
                    || isBlank(b.getAppointmentId());
        }

        // Spostato qui (richiesta Sonar): costruzione testo è responsabilità della cella
        private String buildRowText(BookingBean b) {
            String data = formatDate(b.getDay());
            String inizio = formatTime(b.getStartTime());
            String fine = formatTime(b.getEndTime());
            String prezzo = formatPrice(b.getPrezzoTotale());
            String stato = (b.getStatus() == null) ? "-" : b.getStatus().name();
            String svc = defaultIfBlank(b.getServiceName(), "Servizio");

            // Nota: nel tuo snippet c'è getCouponcode() (c minuscola). Mantengo quello per compatibilità.
            String coupon = defaultIfBlank(b.getCouponcode(), "-");

            return svc + " | " + data + " " + inizio + "-" + fine
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
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @FXML
    private void onBack() {
        switchSafe("HomeViewAlternative.fxml", "Home");
    }

    @FXML
    private void onFidelity() {
        switchSafe("FidelityCardViewAlternative.fxml", "Fidelity Card");
    }
}
