package com.ispw.progettoispw.controller.controllergrafico.alternative;

import com.ispw.progettoispw.controller.controllerapplicativo.FidelityController;
import com.ispw.progettoispw.controller.controllergrafico.GraphicController;
import com.ispw.progettoispw.exception.BusinessRuleException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.session.Session;
import com.ispw.progettoispw.session.SessionManager;
import com.ispw.progettoispw.bean.PrizeBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.List;

public class FidelityCardGUIAlternativeController extends GraphicController {

    @FXML private Label puntiRimastiLabel;

    @FXML private Label puntiNec1Label;
    @FXML private Label puntiNec2Label;
    @FXML private Label puntiNec3Label;

    @FXML private Label premio1Label;
    @FXML private Label premio2Label;
    @FXML private Label premio3Label;

    @FXML private Button redeemBtn1;
    @FXML private Button redeemBtn2;
    @FXML private Button redeemBtn3;

    @FXML private Button backButton;
    @FXML private Button homeButton;

    private final FidelityController fidelityService = new FidelityController();
    private List<PrizeBean> prizes;
    private String clientId;

    @FXML
    private void initialize() {
        Session s = SessionManager.getInstance().getCurrentSession();
        if (s == null) {
            setAllDisabled(true);
            if (puntiRimastiLabel != null) puntiRimastiLabel.setText("0");
            showInfo("Effettua l'accesso per usare la Fidelity Card.");
            return;
        }
        clientId = s.getId();
        reloadData();
    }

    private void reloadData() {
        int points = fidelityService.getCustomerPoints(clientId);
        puntiRimastiLabel.setText(String.valueOf(points));

        prizes = fidelityService.listPrizesVM();

        PrizeBean p1 = find("P1");
        PrizeBean p2 = find("P2");
        PrizeBean p3 = find("P3");

        bindPrize(points, p1, puntiNec1Label, premio1Label, redeemBtn1);
        bindPrize(points, p2, puntiNec2Label, premio2Label, redeemBtn2);
        bindPrize(points, p3, puntiNec3Label, premio3Label, redeemBtn3);
    }

    private void bindPrize(int points, PrizeBean p,
                           Label ptsLabel, Label descLabel, Button redeemBtn) {

        if (p == null) {
            if (ptsLabel != null)  ptsLabel.setText("-");
            if (descLabel != null) descLabel.setText("-");
            if (redeemBtn != null) redeemBtn.setDisable(true);
            return;
        }

        if (ptsLabel != null)  ptsLabel.setText(String.valueOf(p.getRequiredPoints()));
        if (descLabel != null) descLabel.setText(descrizionePremio(p));
        if (redeemBtn != null) redeemBtn.setDisable(points < p.getRequiredPoints());
    }

    private PrizeBean find(String id) {
        if (prizes == null) return null;
        return prizes.stream().filter(p -> id.equals(p.getId())).findFirst().orElse(null);
    }

    private String descrizionePremio(PrizeBean p) {
        BigDecimal v = p.getCouponValue() == null ? BigDecimal.ZERO : p.getCouponValue();
        return p.getName() + " — Coupon € " + v.toPlainString();
    }

    private void setAllDisabled(boolean disabled) {
        if (redeemBtn1 != null) redeemBtn1.setDisable(disabled);
        if (redeemBtn2 != null) redeemBtn2.setDisable(disabled);
        if (redeemBtn3 != null) redeemBtn3.setDisable(disabled);
        if (backButton != null) backButton.setDisable(disabled);
        if (homeButton != null) homeButton.setDisable(disabled);
    }

    @FXML private void onRedeemP1() { redeem("P1"); }
    @FXML private void onRedeemP2() { redeem("P2"); }
    @FXML private void onRedeemP3() { redeem("P3"); }

    private void redeem(String prizeId) {
        if (clientId == null || clientId.isBlank()) {
            showInfo("Utente non loggato.");
            return;
        }

        try {
            String code = fidelityService.redeem(clientId, prizeId);
            showInfo("Premio riscattato! Coupon generato: " + code);
            reloadData();

        } catch (ValidazioneException | OggettoInvalidoException | BusinessRuleException e) {
            showError(e.getMessage());
        } catch (Exception ex) {
            showError("Errore tecnico durante il riscatto.");
        }
    }

    @FXML
    private void onBack() {
        switchSafe("AreaPersonaleViewAlternative.fxml", "Area Personale");
    }

    @FXML
    private void onHome() {
        switchSafe("HomeViewAlternative.fxml", "Home");
    }
}
