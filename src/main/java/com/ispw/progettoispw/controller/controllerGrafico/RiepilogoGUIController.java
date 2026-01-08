package com.ispw.progettoispw.controller.controllerGrafico;

import com.ispw.progettoispw.controller.controllerApplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerApplicativo.LoginController;
import com.ispw.progettoispw.exception.ConflittoPrenotazioneException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class RiepilogoGUIController extends GraphicController {

    @FXML private StackPane root;

    @FXML private Label clienteLabel;
    @FXML private Label barbiereLabel;
    @FXML private Label dataLabel;
    @FXML private Label orarioLabel;
    @FXML private Label servizioLabel;
    @FXML private Label prezzoLabel;

    @FXML private ToggleGroup paymentGroup;
    @FXML private RadioButton payInShopRadio;
    @FXML private RadioButton payInAppRadio;

    private final BookingController bookingController = new BookingController();
    private BookingBean bean;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DecimalFormat PRICE_FMT = new DecimalFormat("#,##0.00");

    @FXML
    private void initialize() {
        bean = bookingController.getBookingFromSession();
        if (bean == null) {
            showWarn("Dati prenotazione mancanti. Torna allo step precedente.");
            disableAll();
            return;
        }
        refreshView();
    }

    private void refreshView() {
        String clienteName = LoginController.getName();
        clienteLabel.setText(clienteName == null ? "-" : clienteName);
        bean.setClienteId(LoginController.getId());

        String barberDisplay = bean.getBarbiereDisplay();
        if (barberDisplay == null || barberDisplay.isBlank()) barberDisplay = bean.getBarbiereId();
        barbiereLabel.setText(barberDisplay == null ? "-" : barberDisplay);

        dataLabel.setText(bean.getDay() == null ? "-" : bean.getDay().format(DATE_FMT));
        if (bean.getStartTime() != null && bean.getEndTime() != null) {
            orarioLabel.setText(bean.getStartTime().format(TIME_FMT) + " - " + bean.getEndTime().format(TIME_FMT));
        } else {
            orarioLabel.setText("-");
        }

        String serviceName = bean.getServiceName();
        BigDecimal price = bean.getPrezzoTotale();

        if ((serviceName == null || serviceName.isBlank()) || price == null) {
            String sid = bean.getServizioId();
            if (sid != null && !sid.isBlank()) {
                ServizioBean sb = bookingController.getServizioVM(sid);
                if (sb != null) {
                    if (serviceName == null || serviceName.isBlank()) {
                        serviceName = sb.getName();
                        bean.setServiceName(serviceName);
                    }
                    if (price == null) {
                        price = (sb.getPrice() == null ? BigDecimal.ZERO : sb.getPrice());
                        bean.setPrezzoTotale(price);
                    }
                }
            }
        }

        servizioLabel.setText(serviceName == null ? "-" : serviceName);
        prezzoLabel.setText("€ " + PRICE_FMT.format(price == null ? BigDecimal.ZERO : price));
    }

    @FXML
    private void onConferma() {
        if (bean == null) return;
        if (paymentGroup.getSelectedToggle() == null) {
            showWarn("Seleziona una modalità di pagamento.");
            return;
        }

        boolean payInApp = (paymentGroup.getSelectedToggle() == payInAppRadio);

        try {
            if (payInApp) {
                bean.setOnline();
                bookingController.bookOnlineCheck(bean);

                bookingController.saveBookingToSession(bean);
                showInfo("Procedo al pagamento in app…");
                switchSafe("PagamentoView.fxml", "Pagamento");

            } else {
                bean.setInsede();
                bookingController.book(bean);

                showInfo("Prenotazione confermata. Pagherai in sede.");
                bookingController.sendEmail(bean);
                bookingController.clearBookingFromSession();
                switchSafe("HomeView.fxml", "Home");
            }

        } catch (ValidazioneException | OggettoInvalidoException e) {
            showWarn(e.getMessage());
        } catch (ConflittoPrenotazioneException e) {
            showWarn("L'orario scelto non è più disponibile. Seleziona un nuovo orario.");
        } catch (Exception ex) {
            showWarn("Errore tecnico. Riprova più tardi.");
        }
    }

    @FXML
    private void onIndietro() {
        switchSafe("OrarioView.fxml", "Scegli orario");
    }

    private void disableAll() { if (root != null) root.setDisable(true); }

    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
