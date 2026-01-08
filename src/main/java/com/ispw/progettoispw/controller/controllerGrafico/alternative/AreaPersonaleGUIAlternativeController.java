package com.ispw.progettoispw.Controller.ControllerGrafico.Alternative;

import com.ispw.progettoispw.Controller.ControllerApplicativo.BookingController;
import com.ispw.progettoispw.Controller.ControllerApplicativo.LoginController;
import com.ispw.progettoispw.Controller.ControllerGrafico.GraphicController;
import com.ispw.progettoispw.Enum.AppointmentStatus;
import com.ispw.progettoispw.Exception.BusinessRuleException;
import com.ispw.progettoispw.Exception.OggettoInvalidoException;
import com.ispw.progettoispw.Exception.ValidazioneException;
import com.ispw.progettoispw.bean.BookingBean;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AreaPersonaleGUIAlternativeController extends GraphicController {

    @FXML private ListView<BookingBean> listView;
    @FXML private Button backButton;
    @FXML private Button fidelityButton;

    private final BookingController bookingController = new BookingController();

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    private final ObservableList<BookingBean> items = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        listView.setItems(items);
        listView.setPlaceholder(new Label("Nessuna prenotazione trovata."));

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(BookingBean b, boolean empty) {
                super.updateItem(b, empty);
                if (empty || b == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String data   = b.getDay()       != null ? b.getDay().format(dateFmt) : "-";
                String inizio = b.getStartTime() != null ? b.getStartTime().format(timeFmt) : "-";
                String fine   = b.getEndTime()   != null ? b.getEndTime().format(timeFmt) : "-";
                String prezzo = b.getPrezzoTotale() != null ? b.getPrezzoTotale().toPlainString() + " €" : "-";
                String stato  = b.getStatus() != null ? b.getStatus().name() : "-";
                String svc    = b.getServiceName() != null ? b.getServiceName() : "Servizio";
                String coupon = b.getCouponCode() != null ? b.getCouponCode() : "-";

                Label info = new Label(
                        svc + " | " + data + " " + inizio + "-" + fine +
                                " | Prezzo: " + prezzo + " | Coupon: " + coupon + " | Stato: " + stato
                );

                Button cancel = new Button("Cancella");
                cancel.setStyle("-fx-background-color:#ff0b0b; -fx-text-fill:white;");
                cancel.setDisable(b.getStatus() == AppointmentStatus.CANCELLED
                        || b.getStatus() == AppointmentStatus.COMPLETED);

                cancel.setOnAction(e -> onCancel(b));

                HBox row = new HBox(10, info, cancel);
                row.setFillHeight(true);
                setGraphic(row);
                setText(null);
            }
        });

        loadAppointments();
    }

    private void loadAppointments() {
        items.clear();

        String clientId = LoginController.getId();
        if (clientId == null || clientId.isBlank()) {
            listView.setPlaceholder(new Label("Utente non loggato."));
            return;
        }

        List<BookingBean> lista = bookingController.listCustomerAppointmentsVM(clientId);
        if (lista == null || lista.isEmpty()) {
            listView.setPlaceholder(new Label("Nessuna prenotazione trovata."));
            return;
        }
        items.addAll(lista);
    }

    private void onCancel(BookingBean b) {
        if (b.getAppointmentId() == null || b.getAppointmentId().isBlank()) {
            showInfo("Impossibile cancellare: id appuntamento mancante.");
            return;
        }

        try {
            bookingController.cancelCustomerAppointment(b.getAppointmentId());
            showInfo("Prenotazione cancellata.");
            loadAppointments();

        } catch (ValidazioneException | OggettoInvalidoException | BusinessRuleException e) {
            showError(e.getMessage());
        } catch (Exception ex) {
            showError("Errore tecnico. Riprova più tardi.");
        }
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
