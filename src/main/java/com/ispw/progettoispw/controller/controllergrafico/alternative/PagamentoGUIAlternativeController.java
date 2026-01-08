package com.ispw.progettoispw.controller.controllergrafico.alternative;

import com.ispw.progettoispw.controller.controllerapplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerapplicativo.CouponController;
import com.ispw.progettoispw.controller.controllergrafico.GraphicController;
import com.ispw.progettoispw.exception.ConflittoPrenotazioneException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.CouponBean;
import com.ispw.progettoispw.bean.PaymentBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.math.BigDecimal;
import java.util.List;

public class PagamentoGUIAlternativeController extends GraphicController {

    @FXML private Pane root;

    @FXML private RadioButton pagaOnline;
    @FXML private RadioButton pagaInSede;

    @FXML private Pane cardPane;
    @FXML private GridPane couponGrid;

    @FXML private TextField cardHolderField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private PasswordField cvvField;

    @FXML private TextField couponField;

    @FXML private Label totalLabel;
    @FXML private Label errorLabel;

    @FXML private Button backButton;
    @FXML private Button payButton;

    private final BookingController bookingController = new BookingController();
    private final CouponController couponController = new CouponController();

    private BookingBean booking;
    private BigDecimal baseTotal = BigDecimal.ZERO;
    private BigDecimal currentTotal = BigDecimal.ZERO;

    private final ToggleGroup payGroup = new ToggleGroup();
    private final CouponBean couponBean = new CouponBean();

    @FXML
    private void initialize() {
        hideError();

        pagaOnline.setToggleGroup(payGroup);
        pagaInSede.setToggleGroup(payGroup);

        booking = bookingController.getBookingFromSession();
        if (booking == null || booking.getServizioId() == null || booking.getServizioId().isBlank()) {
            showError("Dati prenotazione mancanti. Torna allo step precedente.");
            disableAll();
            return;
        }

        if (booking.getPrezzoTotale() != null) baseTotal = booking.getPrezzoTotale();
        else {
            var s = bookingController.getServizio(booking.getServizioId());
            baseTotal = (s != null && s.getPrice() != null) ? s.getPrice() : BigDecimal.ZERO;
        }

        currentTotal = baseTotal;
        refreshTotalLabel();

        pagaInSede.setSelected(true);
        showInSedeMode(true);

        payGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            boolean inSede = (newT == pagaInSede);
            showInSedeMode(inSede);
        });
    }

    private void showInSedeMode(boolean inSede) {
        boolean showOnlineFields = !inSede;

        if (couponGrid != null) couponGrid.setVisible(showOnlineFields);
        if (cardPane != null) cardPane.setVisible(showOnlineFields);

        hideError();

        if (inSede) currentTotal = baseTotal;
        refreshTotalLabel();

        if (payButton != null) payButton.setText(inSede ? "Conferma Prenotazione" : "Paga");
    }

    @FXML
    private void onValidateCoupon() {
        hideError();

        if (!pagaOnline.isSelected()) {
            showError("La validazione coupon è disponibile solo per il pagamento online.");
            return;
        }

        String code = safeText(couponField);
        if (code.isBlank()) {
            showError("Inserisci un codice coupon.");
            return;
        }

        try {
            couponBean.setClienteId(booking.getClienteId());
            couponBean.setCouponCode(code);

            BigDecimal newTotal = couponController.previewTotalWithCoupon(couponBean, baseTotal);
            currentTotal = (newTotal == null) ? baseTotal : newTotal;
            booking.setCouponCode(code);

            refreshTotalLabel();

        } catch (ValidazioneException e) {
            showError(e.getMessage());
        } catch (Exception ex) {
            showError("Coupon non valido.");
        }
    }

    @FXML
    private void onPay() {
        hideError();

        // ===== IN SEDE =====
        if (pagaInSede.isSelected()) {
            try {
                booking.setInsede();
                booking.setPrezzoTotale(baseTotal);
                booking.setCouponCode(null);

                bookingController.book(booking);
                bookingController.sendEmail(booking);
                bookingController.clearBookingFromSession();

                showInfo("Prenotazione registrata! Pagherai in sede.");
                switchSafe("HomeViewAlternative.fxml", "Home");
                return;

            } catch (ValidazioneException | OggettoInvalidoException e) {
                showError(e.getMessage());
            } catch (ConflittoPrenotazioneException e) {
                showError("Lo slot selezionato non è più disponibile. Torna indietro e scegli un altro orario.");
            } catch (Exception ex) {
                showError("Errore tecnico. Riprova.");
            }
            return;
        }

        // ===== ONLINE =====
        PaymentBean pb = new PaymentBean();
        pb.setCardHolderName(safeText(cardHolderField));
        pb.setCardNumber(safeText(cardNumberField));
        pb.setExpiry(safeText(expiryField));
        pb.setCvv(safeText(cvvField));
        pb.setCouponCode(safeText(couponField));
        pb.setAmount(currentTotal);

        List<String> errs = pb.validate();
        if (!errs.isEmpty()) {
            showError(String.join("\n", errs));
            return;
        }

        try {
            String result = bookingController.paga(
                    pb.getCardHolderName(),
                    pb.getCardNumber(),
                    pb.getExpiry(),
                    pb.getCvv(),
                    pb.getAmount()
            );

            if (!"success".equalsIgnoreCase(result)) {
                showError(switch (result) {
                    case "error:card_declined" -> "Carta rifiutata.";
                    case "error:coupon_invalid" -> "Coupon non valido al pagamento.";
                    case "error:slot_taken" -> "Orario non più disponibile.";
                    default -> "Errore durante il pagamento.";
                });
                return;
            }

            booking.setOnline();
            booking.setPrezzoTotale(currentTotal);
            booking.setCouponCode(pb.getCouponCode());

            bookingController.book(booking);

            if (couponBean.getCouponCode() != null && !couponBean.getCouponCode().isBlank()) {
                couponController.markCouponUsed(couponBean.getCouponCode(), booking.getClienteId());
            }

            bookingController.sendEmail(booking);
            bookingController.clearBookingFromSession();

            showInfo("Pagamento completato e prenotazione confermata!");
            switchSafe("HomeViewAlternative.fxml", "Home");

        } catch (ValidazioneException | OggettoInvalidoException e) {
            showError(e.getMessage());
        } catch (ConflittoPrenotazioneException e) {
            showError("Orario non più disponibile.");
        } catch (Exception ex) {
            showError("Errore tecnico. Riprova.");
        }
    }

    @FXML
    private void onBack() {
        switchSafe("RiepilogoViewAlternative.fxml", "Riepilogo");
    }

    private void refreshTotalLabel() {
        if (totalLabel != null) totalLabel.setText("€ " + currentTotal.toPlainString());
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
    }

    private void disableAll() {
        if (root != null) root.setDisable(true);
    }

    private static String safeText(TextField tf) {
        return tf == null ? "" : (tf.getText() == null ? "" : tf.getText().trim());
    }

    private static String safeText(PasswordField pf) {
        return pf == null ? "" : (pf.getText() == null ? "" : pf.getText().trim());
    }
}
