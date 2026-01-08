package com.ispw.progettoispw.controller.controllerGrafico;

import com.ispw.progettoispw.bean.BarbiereBean;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.controller.controllerApplicativo.BookingController;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class OrarioGUIController extends GraphicController {

    private static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeCombo;
    @FXML private ComboBox<BarbiereBean> barberCombo;
    @FXML private Label warningLabel;

    private final BookingController bookingController = new BookingController();
    private BookingBean bookingBean;

    @FXML
    private void initialize() {
        clear(warningLabel);

        if (!loadBookingBean()) {
            return;
        }

        setupDatePicker();
        setupListeners();
        setupBarberCombo();
        setupTimeCombo();

        onDateChanged(datePicker.getValue());
    }

    private boolean loadBookingBean() {
        bookingBean = bookingController.getBookingFromSession();
        if (isBookingMissing(bookingBean)) {
            showWarn("Dati prenotazione mancanti. Torna allo step precedente.");
            return false;
        }
        return true;
    }

    private boolean isBookingMissing(BookingBean bean) {
        return bean == null
                || bean.getServizioId() == null
                || bean.getServizioId().isBlank();
    }

    private void setupDatePicker() {
        LocalDate today = LocalDate.now();
        datePicker.setValue(today);
        datePicker.setDayCellFactory(dp -> createDateCell(today));
    }

    private DateCell createDateCell(LocalDate today) {
        return new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                if (empty || d == null) return;
                setDisable(d.isBefore(today));
            }
        };
    }

    private void setupListeners() {
        datePicker.valueProperty().addListener((obs, oldD, newD) -> onDateChanged(newD));
        barberCombo.valueProperty().addListener((obs, oldB, newB) -> onBarberChanged(newB));
    }

    private void setupBarberCombo() {
        ListCell<BarbiereBean> cell = displayNameCell();
        barberCombo.setCellFactory(list -> displayNameCell());
        barberCombo.setButtonCell(cell);
    }

    private ListCell<BarbiereBean> displayNameCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(BarbiereBean vm, boolean empty) {
                super.updateItem(vm, empty);
                setText((empty || vm == null) ? null : vm.getDisplayName());
            }
        };
    }

    private void setupTimeCombo() {
        ListCell<LocalTime> cell = timeCell();
        timeCombo.setCellFactory(list -> timeCell());
        timeCombo.setButtonCell(cell);
    }

    private ListCell<LocalTime> timeCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(LocalTime t, boolean empty) {
                super.updateItem(t, empty);
                setText((empty || t == null) ? null : t.format(HHMM));
            }
        };
    }

    private void onDateChanged(LocalDate newDate) {
        clearWarning();
        clearTimeItems();

        if (newDate == null) {
            clearBarberItems();
            return;
        }

        List<BarbiereBean> disponibili = bookingController
                .availableBarbersVM(newDate, bookingBean.getServizioId());

        barberCombo.setItems(FXCollections.observableArrayList(disponibili));

        if (disponibili.isEmpty()) {
            showWarn("Nessun professionista disponibile in questa data.");
        }
    }

    private void onBarberChanged(BarbiereBean newBarber) {
        clearWarning();
        clearTimeItems();

        LocalDate day = datePicker.getValue();
        if (!isSelectionValid(day, newBarber)) {
            return;
        }

        List<LocalTime> libres = bookingController
                .listAvailableStartTimes(newBarber.getId(), day, bookingBean.getServizioId());

        timeCombo.setItems(FXCollections.observableArrayList(libres));

        if (libres.isEmpty()) {
            showWarn("Nessun orario disponibile per il professionista selezionato.");
        }
    }

    private boolean isSelectionValid(LocalDate day, BarbiereBean barber) {
        if (day == null) {
            showWarn("Seleziona prima una data.");
            return false;
        }
        return barber != null;
    }

    private void clearTimeItems() {
        if (timeCombo != null) timeCombo.getItems().clear();
    }

    private void clearBarberItems() {
        if (barberCombo != null) barberCombo.getItems().clear();
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

        if (!validateFinalSelection(day, barber, start)) {
            return;
        }

        updateBookingBean(day, barber, start);
        bookingController.saveBookingToSession(bookingBean);

        switchSafe("RiepilogoView.fxml", "Riepilogo");
    }

    private boolean validateFinalSelection(LocalDate day, BarbiereBean barber, LocalTime start) {
        if (day == null) {
            showWarn("Seleziona una data.");
            return false;
        }
        if (barber == null) {
            showWarn("Seleziona un professionista.");
            return false;
        }
        if (start == null) {
            showWarn("Seleziona un orario.");
            return false;
        }
        return true;
    }

    private void updateBookingBean(LocalDate day, BarbiereBean barber, LocalTime start) {
        bookingBean.setDay(day);
        bookingBean.setBarbiereId(barber.getId());
        bookingBean.setBarbiereDisplay(barber.getDisplayName());
        bookingBean.setStartTime(start);
        bookingBean.computeEndTime();
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
