package com.ispw.progettoispw.controller.controllergrafico.alternative;

import com.ispw.progettoispw.controller.controllerapplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerapplicativo.LoginController;
import com.ispw.progettoispw.controller.controllergrafico.GraphicController;
import com.ispw.progettoispw.exception.ConflittoPrenotazioneException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrarioGUIAlternativeController extends GraphicController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeCombo;

    @FXML private Label clienteValue;
    @FXML private Label professionistaValue;
    @FXML private Label dataValue;
    @FXML private Label orarioValue;
    @FXML private Label servizioValue;
    @FXML private Label prezzoValue;

    @FXML private Button backButton;
    @FXML private Button payButton;

    private final BookingController bookingController = new BookingController();
    private BookingBean bean;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    private void initialize() {
        bean = bookingController.getBookingFromSession();
        if (bean == null) {
            showError("Dati mancanti. Torna indietro e seleziona servizio e professionista.");
            onBack();
            return;
        }

        clienteValue.setText(LoginController.getName() == null ? "-" : LoginController.getName());
        bean.setClienteId(LoginController.getId());

        professionistaValue.setText(bean.getBarbiereDisplay() == null ? "-" : bean.getBarbiereDisplay());

        ServizioBean sVM = bookingController.getServizioVM(bean.getServizioId());
        servizioValue.setText(sVM == null || sVM.getName() == null ? "-" : sVM.getName());

        BigDecimal price = bean.getPrezzoTotale() == null ? BigDecimal.ZERO : bean.getPrezzoTotale();
        if (price.signum() == 0 && sVM != null && sVM.getPrice() != null) {
            price = sVM.getPrice();
            bean.setPrezzoTotale(price);
        }
        prezzoValue.setText(price.toPlainString() + " €");

        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(availableDayCellFactory());
        datePicker.valueProperty().addListener((obs, old, day) -> {
            populateTimesFor(day);
            updateSummary();
        });

        populateTimesFor(datePicker.getValue());
        timeCombo.valueProperty().addListener((obs, old, t) -> updateSummary());
    }

    private Callback<DatePicker, DateCell> availableDayCellFactory() {
        return dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                boolean disable = empty || item == null || item.isBefore(LocalDate.now());
                if (!disable) {
                    List<LocalTime> free = bookingController.listAvailableStartTimes(
                            bean.getBarbiereId(), item, bean.getServizioId()
                    );
                    disable = (free == null || free.isEmpty());
                }
                setDisable(disable);
            }
        };
    }

    private void populateTimesFor(LocalDate day) {
        timeCombo.getItems().clear();
        if (day == null) return;

        List<LocalTime> free = bookingController.listAvailableStartTimes(
                bean.getBarbiereId(), day, bean.getServizioId()
        );

        if (free == null || free.isEmpty()) {
            timeCombo.setPromptText("Nessun orario disponibile");
            dataValue.setText(day.format(dateFmt));
            orarioValue.setText("-");
            return;
        }

        timeCombo.getItems().addAll(free);
        timeCombo.setButtonCell(new TimeCell());
        timeCombo.setCellFactory(cb -> new TimeCell());
        timeCombo.getSelectionModel().selectFirst();

        bean.setDay(day);
        bean.setStartTime(timeCombo.getValue());
        ensureDurationFromVM();
        bean.computeEndTime();

        dataValue.setText(day.format(dateFmt));
        orarioValue.setText(timeCombo.getValue().format(timeFmt));
    }

    private void updateSummary() {
        LocalDate d = datePicker.getValue();
        LocalTime t = timeCombo.getValue();

        if (d != null) {
            dataValue.setText(d.format(dateFmt));
            bean.setDay(d);
        }
        if (t != null) {
            orarioValue.setText(t.format(timeFmt));
            bean.setStartTime(t);
            ensureDurationFromVM();
            bean.computeEndTime();
        }

        bookingController.saveBookingToSession(bean);
    }

    private void ensureDurationFromVM() {
        if (bean.getDurataTotaleMin() > 0) return;
        ServizioBean sVM = bookingController.getServizioVM(bean.getServizioId());
        if (sVM != null && sVM.getDurationMin() > 0) {
            bean.setDurataTotaleMin(sVM.getDurationMin());
        }
    }

    @FXML
    private void onPaga() {
        if (bean.getDay() == null || bean.getStartTime() == null) {
            showError("Seleziona giorno e orario.");
            return;
        }

        try {
            bean.setOnline();
            bookingController.bookOnlineCheck(bean);     // ✅ check slot + validazione con eccezioni
            bookingController.saveBookingToSession(bean);
            switchSafe("PagamentoViewAlternative.fxml", "Pagamento");

        } catch (ValidazioneException | OggettoInvalidoException e) {
            showError(e.getMessage());
        } catch (ConflittoPrenotazioneException e) {
            showError("Slot non più disponibile. Seleziona un altro orario.");
            populateTimesFor(bean.getDay());
        } catch (Exception ex) {
            showError("Errore tecnico. Riprova.");
        }
    }

    @FXML
    private void onBack() {
        switchSafe("PrenotazioneViewAlternative.fxml", "Scegli Servizio");
    }

    private class TimeCell extends ListCell<LocalTime> {
        @Override
        protected void updateItem(LocalTime t, boolean empty) {
            super.updateItem(t, empty);
            setText(empty || t == null ? null : t.format(timeFmt));
        }
    }
}
