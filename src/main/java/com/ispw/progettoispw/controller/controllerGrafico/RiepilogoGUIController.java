package com.ispw.progettoispw.controller.controllerGrafico;

import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.ServizioBean;
import com.ispw.progettoispw.controller.controllerApplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerApplicativo.LoginController;
import com.ispw.progettoispw.exception.ConflittoPrenotazioneException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
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

    private static final String DASH = "-";

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
        bindCliente();
        bindBarbiere();
        bindData();
        bindOrario();

        ensureServiceAndPriceLoaded();
        bindServizioPrezzo();
    }

    private void bindCliente() {
        String clienteName = LoginController.getName();
        clienteLabel.setText(valueOrDash(clienteName));
        bean.setClienteId(LoginController.getId());
    }

    private void bindBarbiere() {
        String display = firstNonBlank(bean.getBarbiereDisplay(), bean.getBarbiereId());
        barbiereLabel.setText(valueOrDash(display));
    }

    private void bindData() {
        dataLabel.setText(bean.getDay() == null ? DASH : bean.getDay().format(DATE_FMT));
    }

    private void bindOrario() {
        if (bean.getStartTime() == null || bean.getEndTime() == null) {
            orarioLabel.setText(DASH);
            return;
        }
        orarioLabel.setText(bean.getStartTime().format(TIME_FMT) + " - " + bean.getEndTime().format(TIME_FMT));
    }

    /**
     * Recupera da catalogo servizio/prezzo solo se mancanti nel bean.
     */
    private void ensureServiceAndPriceLoaded() {
        boolean missingService = isBlank(bean.getServiceName());
        boolean missingPrice = bean.getPrezzoTotale() == null;

        if (!missingService && !missingPrice) return;

        String sid = bean.getServizioId();
        if (isBlank(sid)) return;

        ServizioBean sb = bookingController.getServizioVM(sid);
        if (sb == null) return;

        if (missingService) {
            bean.setServiceName(sb.getName());
        }
        if (missingPrice) {
            bean.setPrezzoTotale(sb.getPrice() == null ? BigDecimal.ZERO : sb.getPrice());
        }
    }

    private void bindServizioPrezzo() {
        String serviceName = bean.getServiceName();
        BigDecimal price = bean.getPrezzoTotale();

        servizioLabel.setText(valueOrDash(serviceName));
        prezzoLabel.setText("€ " + PRICE_FMT.format(price == null ? BigDecimal.ZERO : price));
    }

    private String valueOrDash(String s) {
        return (s == null) ? DASH : s;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String firstNonBlank(String a, String b) {
        return !isBlank(a) ? a : b;
    }

    @FXML
    private void onConferma() {
        if (bean == null) return;

        Toggle selected = paymentGroup.getSelectedToggle();
        if (selected == null) {
            showWarn("Seleziona una modalità di pagamento.");
            return;
        }

        boolean payInApp = (selected == payInAppRadio);

        try {
            if (payInApp) {
                bean.setOnline();
                bookingController.bookOnlineCheck(bean);

                bookingController.saveBookingToSession(bean);
                showInfo("Procedo al pagamento in app…");
                switchSafe("PagamentoView.fxml", "Pagamento");
                return;
            }

            bean.setInsede();
            bookingController.book(bean);

            showInfo("Prenotazione confermata. Pagherai in sede.");
            bookingController.sendEmail(bean);
            bookingController.clearBookingFromSession();
            switchSafe("HomeView.fxml", "Home");

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

    private void disableAll() {
        if (root != null) root.setDisable(true);
    }

    private void showWarn(String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
