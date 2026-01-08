package com.ispw.progettoispw.controller.controllerGrafico;

import com.ispw.progettoispw.controller.controllerApplicativo.BookingController;
import com.ispw.progettoispw.bean.BarbiereBean;
import com.ispw.progettoispw.bean.BookingBean;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrarioGUIController extends GraphicController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeCombo;
    @FXML private ComboBox<BarbiereBean> barberCombo;
    @FXML private Label warningLabel;

    private final BookingController bookingController = new BookingController();
    private BookingBean bookingBean;

    @FXML
    private void initialize() {
        clear(warningLabel);

        bookingBean = bookingController.getBookingFromSession();
        if (bookingBean == null || bookingBean.getServizioId() == null || bookingBean.getServizioId().isBlank()) {
            showWarn("Dati prenotazione mancanti. Torna allo step precedente.");
            return;
        }

        datePicker.setValue(LocalDate.now());

        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) return;
                setDisable(d.isBefore(LocalDate.now()));
            }
        });

        datePicker.valueProperty().addListener((obs, oldD, newD) -> onDateChanged(newD));
        barberCombo.valueProperty().addListener((obs, oldB, newB) -> onBarberChanged(newB));

        barberCombo.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(BarbiereBean vm, boolean empty) {
                super.updateItem(vm, empty);
                setText(empty || vm == null ? null : vm.getDisplayName());
            }
        });
        barberCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(BarbiereBean vm, boolean empty) {
                super.updateItem(vm, empty);
                setText(empty || vm == null ? null : vm.getDisplayName());
            }
        });

        DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");
        timeCombo.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.format(HHMM));
            }
        });
        timeCombo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                setText(empty || t == null ? null : t.format(HHMM));
            }
        });

        onDateChanged(datePicker.getValue());
    }

    private void onDateChanged(LocalDate newDate) {
        clearWarning();
        timeCombo.getItems().clear();

        if (newDate == null) {
            barberCombo.getItems().clear();
            return;
        }

        List<BarbiereBean> disponibili =
                bookingController.availableBarbersVM(newDate, bookingBean.getServizioId());

        barberCombo.setItems(FXCollections.observableArrayList(disponibili));

        if (disponibili.isEmpty()) {
            showWarn("Nessun professionista disponibile in questa data.");
        }
    }

    private void onBarberChanged(BarbiereBean newBarber) {
        clearWarning();
        timeCombo.getItems().clear();

        LocalDate day = datePicker.getValue();
        if (newBarber == null || day == null) {
            if (day == null) showWarn("Seleziona prima una data.");
            return;
        }

        List<LocalTime> libres = bookingController
                .listAvailableStartTimes(newBarber.getId(), day, bookingBean.getServizioId());

        timeCombo.setItems(FXCollections.observableArrayList(libres));

        if (libres.isEmpty()) {
            showWarn("Nessun orario disponibile per il professionista selezionato.");
        }
    }

    @FXML
    public void listinoButtonOnAction() {
        bookingController.clearBookingFromSession();
        switchSafe("PrenotazioneView.fxml", "Listino");
    }

    @FXML
    public void congratulazioniButtonOnAction() {
        clearWarning();

        LocalDate day = datePicker.getValue();
        BarbiereBean barber = barberCombo.getValue();
        LocalTime start = timeCombo.getValue();

        if (day == null)    { showWarn("Seleziona una data."); return; }
        if (barber == null) { showWarn("Seleziona un professionista."); return; }
        if (start == null)  { showWarn("Seleziona un orario."); return; }

        bookingBean.setDay(day);
        bookingBean.setBarbiereId(barber.getId());
        bookingBean.setBarbiereDisplay(barber.getDisplayName());
        bookingBean.setStartTime(start);
        bookingBean.computeEndTime();

        bookingController.saveBookingToSession(bookingBean);

        switchSafe("RiepilogoView.fxml", "Riepilogo");
    }

    private void showWarn(String msg) {
        if (warningLabel != null) {
            warningLabel.setText(msg);
            warningLabel.setVisible(true);
        } else {
            new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
        }
    }

    private void clearWarning() {
        if (warningLabel != null) {
            warningLabel.setVisible(false);
            warningLabel.setText("");
        }
    }
}
