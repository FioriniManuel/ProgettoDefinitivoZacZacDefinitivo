package com.ispw.progettoispw.controller.controllerapplicativo;

import com.ispw.progettoispw.dao.GenericDao;
import com.ispw.progettoispw.enu.CouponStatus;
import com.ispw.progettoispw.exception.ValidazioneException;
import com.ispw.progettoispw.factory.DaoFactory;
import com.ispw.progettoispw.bean.CouponBean;
import com.ispw.progettoispw.entity.LoyaltyAccount;
import com.ispw.progettoispw.entity.PersonalCoupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

public class CouponController {

    private static final int EURO_PER_POINT = 10;

    private final GenericDao<PersonalCoupon> couponDao;
    private final GenericDao<LoyaltyAccount> loyaltyDao;

    public CouponController() {
        DaoFactory f = DaoFactory.getInstance();
        this.couponDao = f.getPersonalCouponDao();
        this.loyaltyDao = f.getLoyaltyAccountDao();
    }

    public BigDecimal previewTotalWithCoupon(CouponBean bean, BigDecimal base) throws ValidazioneException {
        if (bean == null) throw new ValidazioneException("Dati coupon mancanti.");
        if (base == null || base.signum() < 0) throw new ValidazioneException("Importo non valido.");

        BigDecimal discount = BigDecimal.ZERO;
        String code = trim(bean.getCouponCode());

        if (!code.isBlank()) {
            PersonalCoupon c = findActiveCouponByCodeForClient(code, bean.getClienteId());
            if (c != null) {
                discount = c.getValue();
                if (discount == null || discount.signum() <= 0) discount = BigDecimal.ZERO;
                if (discount.compareTo(base) > 0) discount = base;
            }
        }

        BigDecimal total = base.subtract(discount);
        if (total.signum() < 0) total = BigDecimal.ZERO;

        bean.setDiscountApplied(discount);
        bean.setTotalToPay(total);

        return total;
    }

    public int computePointsToAward(BigDecimal totalToPay) {
        BigDecimal tot = safeNonNegative(totalToPay).setScale(0, RoundingMode.DOWN);
        return tot.divide(BigDecimal.valueOf(EURO_PER_POINT), RoundingMode.DOWN).intValue();
    }

    public void markCouponUsed(String couponCode, String clienteId) {
        if (couponCode == null || couponCode.isBlank()) return;
        PersonalCoupon c = findActiveCouponByCodeForClient(couponCode.trim(), clienteId);
        if (c == null) return;
        c.setStatus(CouponStatus.USED);
        couponDao.update(c);
    }

    public String createRewardCoupon(String clientId, BigDecimal value, String note) {
        if (clientId == null || value == null) return null;

        String code = "REWARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        PersonalCoupon c = new PersonalCoupon();
        c.setCouponId(UUID.randomUUID().toString());
        c.setClientId(clientId);
        c.setCode(code);
        c.setValue(value);
        c.setStatus(CouponStatus.ACTIVE);
        c.setNote(note);

        couponDao.create(c);
        return code;
    }

    public boolean reactivateCoupon(String code, String clienteId) {
        if (code == null || code.isBlank() || clienteId == null) {
            return false;
        }

        Optional<PersonalCoupon> opt = couponDao.readAll().stream()
                .filter(c -> code.equalsIgnoreCase(c.getCode()))
                .filter(c -> clienteId.equals(c.getClientId()))
                .findFirst();

        if (opt.isEmpty()) return false;

        PersonalCoupon c = opt.get();
        if (c.getStatus() == CouponStatus.USED) {
            c.setStatus(CouponStatus.ACTIVE);
            couponDao.update(c);
            return true;
        }

        return false;
    }

    private PersonalCoupon findActiveCouponByCodeForClient(String code, String clienteId) {
        for (PersonalCoupon c : couponDao.readAll()) {
            if (c == null) continue;
            boolean codeMatch = code.equalsIgnoreCase(trim(c.getCode()));
            boolean isOwner   = clienteId == null || clienteId.equals(c.getClientId());
            boolean active    = c.getStatus() == CouponStatus.ACTIVE;
            if (codeMatch && isOwner && active) return c;
        }
        return null;
    }

    private static BigDecimal safeNonNegative(BigDecimal v) {
        if (v == null || v.signum() < 0) return BigDecimal.ZERO;
        return v;
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
