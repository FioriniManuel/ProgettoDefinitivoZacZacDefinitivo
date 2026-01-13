package com.ispw.progettoispw.controller.controllergrafico;

import com.ispw.progettoispw.controller.controllerapplicativo.FidelityController;
import com.ispw.progettoispw.exception.BusinessRuleException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.session.Session;
import com.ispw.progettoispw.session.SessionManager;
import com.ispw.progettoispw.bean.FidelityBean;
import com.ispw.progettoispw.bean.PrizeBean;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.List;

public class FidelityCardGUIController extends GraphicController {

    @FXML private Label infoLabel;
    @FXML private Label puntiRimastiLabel;

    @FXML private Label puntiPrimoPremioLabel;
    @FXML private Label puntiSecondoPremioLabel;
    @FXML private Label puntiTerzoPremioLabel;

    @FXML private Label primoPremioLabel;
    @FXML private Label secondoPremioLabel;
    @FXML private Label terzoPremioLabel;

    @FXML private RadioButton prize1Radio;
    @FXML private RadioButton prize2Radio;
    @FXML private RadioButton prize3Radio;

    private final FidelityController fidelityService = new FidelityController();
    private final ToggleGroup prizeGroup = new ToggleGroup();
    private final FidelityBean bean = new FidelityBean();

    private List<PrizeBean> prizes;
    private String clientId;

    @FXML
    private void initialize() {
        hideInfo();

        Session s = SessionManager.getInstance().getCurrentSession();
        if (s == null) {
            showInfo("Effettua l'accesso per usare la fidelity.");
            disableActions(true);
            return;
        }
        clientId = s.getId();

        prize1Radio.setToggleGroup(prizeGroup);
        prize2Radio.setToggleGroup(prizeGroup);
        prize3Radio.setToggleGroup(prizeGroup);

        reloadData();

        prizeGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                bean.setSelectedPrizeId(null);
                return;
            }
            if (newT == prize1Radio) bean.setSelectedPrizeId("P1");
            else if (newT == prize2Radio) bean.setSelectedPrizeId("P2");
            else if (newT == prize3Radio) bean.setSelectedPrizeId("P3");
        });
    }

    private void reloadData() {
        int points = fidelityService.getCustomerPoints(clientId);
        bean.setTotalPoints(points);
        puntiRimastiLabel.setText(String.valueOf(points));

        prizes = fidelityService.listPrizesVM();
        bean.setPrizes(prizes);

        PrizeBean p1 = find("P1");
        PrizeBean p2 = find("P2");
        PrizeBean p3 = find("P3");
        if (p1 != null) {
            puntiPrimoPremioLabel.setText(String.valueOf(p1.getRequiredpoints()));
            primoPremioLabel.setText(descrizionePremio(p1));
        }
        if (p2 != null) {
            puntiSecondoPremioLabel.setText(String.valueOf(p2.getRequiredpoints()));
            secondoPremioLabel.setText(descrizionePremio(p2));
        }
        if (p3 != null) {
            puntiTerzoPremioLabel.setText(String.valueOf(p3.getRequiredpoints()));
            terzoPremioLabel.setText(descrizionePremio(p3));
        }

        if (p1 != null) prize1Radio.setDisable(points < p1.getRequiredpoints());
        if (p2 != null) prize2Radio.setDisable(points < p2.getRequiredpoints());
        if (p3 != null) prize3Radio.setDisable(points < p3.getRequiredpoints());
    }

    private PrizeBean find(String id) {
        if (prizes == null) return null;
        return prizes.stream().filter(p -> id.equals(p.getId())).findFirst().orElse(null);
    }

    private String descrizionePremio(PrizeBean p) {
        BigDecimal v = p.getCouponValue() == null ? BigDecimal.ZERO : p.getCouponValue();
        return p.getName() + " — Coupon € " + v.toPlainString();
    }

    private void disableActions(boolean b) {
        prize1Radio.setDisable(b);
        prize2Radio.setDisable(b);
        prize3Radio.setDisable(b);
    }

    private void hideInfo() {
        if (infoLabel != null) {
            infoLabel.setVisible(false);
            infoLabel.setText("");
        }
    }

    @FXML
    private void ritiraPremioOnAction() {
        hideInfo();

        String prizeId = bean.getSelectedPrizeId();
        if (prizeId == null) {
            showInfo("Seleziona un premio.");
            return;
        }

        try {
            String code = fidelityService.redeem(clientId, prizeId);
            showInfo("Premio riscattato! Coupon generato: " + code);

            reloadData();
            prizeGroup.selectToggle(null);
            bean.setSelectedPrizeId(null);

        } catch (ValidazioneException | OggettoInvalidoException | BusinessRuleException e) {
            showError(e.getMessage());
        } catch (Exception ex) {
            showError("Errore tecnico durante il riscatto.");
        }
    }

    @FXML
    private void homeButtonOnAction() {
        switchSafe("HomeView.fxml", "Home");
    }
}
