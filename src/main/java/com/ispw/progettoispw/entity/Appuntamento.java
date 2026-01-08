package com.ispw.progettoispw.entity;

import com.ispw.progettoispw.enu.AppointmentStatus;
import com.ispw.progettoispw.enu.CouponStatus;
import com.ispw.progettoispw.enu.PaymentChannel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Appuntamento con pagamento "leggero":
 * - Sconto SOLO tramite coupon personale (nessun consumo punti in checkout)
 * - Accreditamento punti al pagamento
 * - Rimborso: riattiva il coupon e storna i punti accreditati
 */
public class Appuntamento {

    /* ===== Identità e riferimenti ===== */
    private String id;
    private String clientId;
    private String barberId;

    /* ===== Data/slot ===== */
    private LocalDate date;
    private LocalTime slotInit;
    private  LocalTime slotFin;//

    /* ===== Stato ===== */
    private AppointmentStatus status = AppointmentStatus.PENDING;

    /* ===== Importi ===== */
    private BigDecimal baseAmount = BigDecimal.ZERO;     // somma servizi (snapshot al momento della prenotazione)
    private BigDecimal discountAmount = BigDecimal.ZERO; // SOLO da coupon
    private BigDecimal total = BigDecimal.ZERO;          // base - discount (>=0)

    /* ===== Coupon (snapshot) ===== */
    private String appliedCouponId;
    private String appliedCouponCode;

    /* ===== Loyalty (snapshot) ===== */
    private Integer loyaltyPointsEarned; // punti accreditati al pagamento

    /* ===== Pagamento ===== */
    private PaymentChannel paymentChannel; // ONLINE / IN_SHOP

    private String servizio;

    /* ===== Costruttori / factory ===== */
    public Appuntamento() {}

    public static Appuntamento newWithId() {
        Appuntamento a = new Appuntamento();
        a.id = UUID.randomUUID().toString();
        return a;
    }

    public Appuntamento(String clientId, String barberId, LocalDate date, LocalTime slotInit,LocalTime slotFin ,BigDecimal baseAmount) {
        this.id = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.barberId = Objects.requireNonNull(barberId, "barberId");
        this.date = Objects.requireNonNull(date, "date");
        this.slotFin = slotFin;
        this.slotInit=slotInit;
        setBaseAmount(baseAmount);
        recomputeTotal();
    }

    /* ===== Getter/Setter essenziali ===== */
    public String getId() { return id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId;}

    public String getBarberId() { return barberId; }
    public void setBarberId(String barberId) { this.barberId = barberId;}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date;  }

    public LocalTime getSlotIndex() { return slotInit; }
    public void setSlotIndex(LocalTime slotInit) { this.slotInit= slotInit; }
    public LocalTime getSlotFin(){ return slotFin;}

    public void setSlotFin(LocalTime slotFin) {
        this.slotFin = slotFin;
    }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status;  }

    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) {
        if (baseAmount == null || baseAmount.signum() < 0) throw new IllegalArgumentException("baseAmount >= 0");
        this.baseAmount = baseAmount;
        recomputeTotal();
    }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotal() { return total; }
    public void setInsede() { this.paymentChannel = PaymentChannel.IN_SHOP; }
    public void setOnline() { this.paymentChannel = PaymentChannel.ONLINE; }
    public String getAppliedCouponId() { return appliedCouponId; }
    public String getAppliedCouponCode() { return appliedCouponCode; }

    public void setAppliedCouponCode(String appliedCouponCode) {
        this.appliedCouponCode = appliedCouponCode;
    }

    public Integer getLoyaltyPointsEarned() { return loyaltyPointsEarned; }

    public PaymentChannel getPaymentChannel() { return paymentChannel; }


    /* ===== Sconto: SOLO coupon ===== */

    /**
     * Applica un coupon personale all'appuntamento.
     * Non cambia lo stato del coupon qui; verrà marcato USED solo in markPaid.
     * @return true se applicato, false se non valido
     */
    public boolean applyPersonalCoupon(PersonalCoupon coupon, String currentClientId) {
        if (coupon == null) return false;
        if (coupon.getStatus() != CouponStatus.ACTIVE) return false;
        if (coupon.getValue() == null || coupon.getValue().signum() <= 0) return false;
        if (coupon.getClientId() == null || !coupon.getClientId().equals(currentClientId)) return false;

        this.appliedCouponId = coupon.getCouponId();
        this.appliedCouponCode = coupon.getCode();

        BigDecimal newDiscount = nonNegative(this.discountAmount.add(coupon.getValue()));
        if (newDiscount.compareTo(baseAmount) > 0) newDiscount = baseAmount; // cap per non andare sotto zero
        this.discountAmount = newDiscount;

        recomputeTotal();
        return true;
    }


    /* ===== Totali e helper ===== */
    public void recomputeTotal() {
        BigDecimal d = (discountAmount == null) ? BigDecimal.ZERO : discountAmount;
        total = baseAmount.subtract(d);
        if (total.signum() < 0) total = BigDecimal.ZERO;
    }

    private BigDecimal nonNegative(BigDecimal v) { return (v == null || v.signum() < 0) ? BigDecimal.ZERO : v; }


    public void setId(String id) {
        this.id = id;
    }

    public String getServizio() {
         return servizio;
    }

    public void setServizio(String servizioId) {
        this.servizio=servizioId;
    }
}
