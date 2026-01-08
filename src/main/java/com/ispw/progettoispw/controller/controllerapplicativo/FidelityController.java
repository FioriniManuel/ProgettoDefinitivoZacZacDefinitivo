package com.ispw.progettoispw.controller.controllerapplicativo;

import com.ispw.progettoispw.dao.ReadOnlyDao;
import com.ispw.progettoispw.exception.BusinessRuleException;
import com.ispw.progettoispw.exception.OggettoInvalidoException;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.factory.DaoFactory;
import com.ispw.progettoispw.bean.PrizeBean;
import com.ispw.progettoispw.entity.PrizeOption;

import java.math.BigDecimal;
import java.util.List;

public class FidelityController {

    private final ReadOnlyDao<PrizeOption> prizeDao;
    private final LoyaltyController loyaltyController;
    private final CouponController couponController;

    public FidelityController() {
        DaoFactory factory = DaoFactory.getInstance();
        this.prizeDao = factory.getPrizeOptionDao();
        this.loyaltyController = new LoyaltyController();
        this.couponController = new CouponController();
    }

    public List<PrizeOption> listPrizes() {
        return prizeDao.readAll();
    }

    public PrizeOption getPrizeOption(String id) {
        if (id == null || id.isBlank()) return null;
        return prizeDao.read(id);
    }

    public List<PrizeBean> listPrizesVM() {
        return listPrizes().stream()
                .map(p -> new PrizeBean(p.getId(), p.getName(), p.getRequiredPoints(), p.getCouponValue()))
                .toList();
    }

    public void updatePrizeOption(String id, String name, int requiredPoints, BigDecimal couponValue)
            throws ValidazioneException {

        if (id == null || id.isBlank())
            throw new ValidazioneException("ID premio mancante");
        if (name == null || name.isBlank())
            throw new ValidazioneException("Nome premio mancante");
        if (requiredPoints <= 0)
            throw new ValidazioneException("I punti richiesti devono essere > 0");
        if (couponValue == null || couponValue.signum() < 0)
            throw new ValidazioneException("Il valore coupon deve essere >= 0");

        PrizeOption p = new PrizeOption(id, name, requiredPoints, couponValue);
        prizeDao.upsert(p);
    }

    public int getCustomerPoints(String clientId) {
        if (clientId == null || clientId.isBlank()) return 0;
        return loyaltyController.getPoints(clientId);
    }

    public boolean canRedeem(String clientId, String prizeId) {
        if (clientId == null || clientId.isBlank()) return false;
        if (prizeId == null || prizeId.isBlank()) return false;

        PrizeOption p = prizeDao.read(prizeId);
        if (p == null) return false;

        int pts = getCustomerPoints(clientId);
        return pts >= p.getRequiredPoints();
    }

    public String redeem(String clientId, String prizeId)
            throws ValidazioneException, OggettoInvalidoException, BusinessRuleException {

        if (clientId == null || clientId.isBlank())
            throw new ValidazioneException("Cliente non valido.");
        if (prizeId == null || prizeId.isBlank())
            throw new ValidazioneException("Seleziona un premio.");

        PrizeOption p = prizeDao.read(prizeId);
        if (p == null)
            throw new OggettoInvalidoException("Premio inesistente.");

        int pts = getCustomerPoints(clientId);
        if (pts < p.getRequiredPoints())
            throw new BusinessRuleException("Punti insufficienti per il premio selezionato.");

        loyaltyController.redeemPoints(clientId, p.getRequiredPoints());

        BigDecimal value = p.getCouponValue() == null ? BigDecimal.ZERO : p.getCouponValue();
        return couponController.createRewardCoupon(clientId, value, "Premio fidelity: " + p.getName());
    }

    public void updatePrize(PrizeOption updated) throws ValidazioneException {
        if (updated == null) throw new ValidazioneException("Premio nullo");
        if (updated.getId() == null || updated.getId().isBlank())
            throw new ValidazioneException("Id premio mancante");
        prizeDao.upsert(updated);
    }
}
