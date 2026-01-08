package com.ispw.progettoispw.controller.controllergrafico;

import com.ispw.progettoispw.controller.controllerapplicativo.BookingController;
import com.ispw.progettoispw.controller.controllerapplicativo.CouponController;
import com.ispw.progettoispw.exception.ConflittoPrenotazioneException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.bean.BookingBean;
import com.ispw.progettoispw.bean.CouponBean;
import com.ispw.progettoispw.bean.PaymentBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.math.BigDecimal;
import java.util.List;

public class PagamentoGUIController extends GraphicController {

    @FXML private Pane root;

    @FXML private TextField cardHolderField;
    @FXML private TextField cardNumberField;
    @FXML private TextField expiryField;
    @FXML private PasswordField cvvField;

    @FXML private TextField couponField;
    @FXML private Label totalLabel;
    @FXML private Label errorLabel;

    private final BookingController bookingController = new BookingController();
    private final CouponController couponController = new CouponController();

    private BookingBean booking;
    private BigDecimal baseTotal = BigDecimal.ZERO;
    private BigDecimal currentTotal = BigDecimal.ZERO;

    private final CouponBean cb = new CouponBean();

    @FXML
    private void initialize() {
        hideError();

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
    }

    @FXML
    private void onValidateCoupon() {
        hideError();

        String code = safeText(couponField);
        if (code.isEmpty()) {
            showErrorOnLabel("Inserisci un codice coupon.");
            return;
        }

        try {
            cb.setClienteId(booking.getClienteId());
            cb.setCouponCode(code);

            BigDecimal newTotal = couponController.previewTotalWithCoupon(cb, baseTotal);

            if (newTotal == null || newTotal.compareTo(baseTotal) > 0) {
                showErrorOnLabel("Coupon non valido.");
                return;
            }

            currentTotal = newTotal;
            booking.setCouponCode(code);
            refreshTotalLabel();

        } catch (ValidazioneException e) {
            showErrorOnLabel(e.getMessage());
        } catch (Exception ex) {
            showErrorOnLabel("Coupon non valido.");
        }
    }

    @FXML
    private void onPay() {
        hideError();

        PaymentBean pb = new PaymentBean();
        pb.setCardHolderName(safeText(cardHolderField));
        pb.setCardNumber(safeText(cardNumberField));
        pb.setExpiry(safeText(expiryField));
        pb.setCvv(safeText(cvvField));
        pb.setCouponCode(safeText(couponField));
        pb.setAmount(currentTotal);

        List<String> errs = pb.validate();
        if (!errs.isEmpty()) {
            showErrorOnLabel(String.join("\n", errs));
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

            if (!"success".equals(result)) {
                showErrorOnLabel(mapPaymentError(result));
                return;
            }

            booking.setOnline();
            booking.setPrezzoTotale(currentTotal);
            booking.setCouponCode(pb.getCouponCode());

            bookingController.book(booking);

            if (cb.getCouponCode() != null && !cb.getCouponCode().isBlank()) {
                couponController.markCouponUsed(cb.getCouponCode(), cb.getClienteId());
            }

            bookingController.sendEmail(booking);
            bookingController.clearBookingFromSession();

            showInfo("Pagamento completato con successo!");
            switchSafe("HomeView.fxml", "Home");

        } catch (ValidazioneException | OggettoInvalidoException e) {
            showErrorOnLabel(e.getMessage());
        } catch (ConflittoPrenotazioneException e) {
            showErrorOnLabel("L'orario selezionato non è più disponibile.");
        } catch (Exception ex) {
            showErrorOnLabel("Errore tecnico. Riprova più tardi.");
        }
    }

    @FXML
    private void onBack() {
        switchSafe("RiepilogoView.fxml", "Riepilogo");
    }

    private String mapPaymentError(String result) {
        return switch (result) {
            case "error:card_declined" -> "Carta rifiutata. Controlla i dati o usa un altro metodo.";
            case "error:coupon_invalid" -> "Coupon non valido al momento del pagamento.";
            case "error:slot_taken" -> "L'orario selezionato non è più disponibile.";
            default -> "Errore durante il pagamento. Riprova.";
        };
    }

    private void refreshTotalLabel() {
        if (totalLabel != null) totalLabel.setText("€ " + currentTotal.toPlainString());
    }

    private void showErrorOnLabel(String msg) {
        if (errorLabel != null) {
            errorLabel.setVisible(true);
            errorLabel.setText(msg);
        } else {
            showError(msg);
        }
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

    private static String safeText(TextInputControl c) {
        String s = (c == null) ? null : c.getText();
        return s == null ? "" : s.trim();
    }
}
