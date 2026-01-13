package com.ispw.progettoispw.entity;

import com.ispw.progettoispw.enu.CouponStatus;

import java.math.BigDecimal;

public class PersonalCoupon {
    private String couponid;
    private String clientid;
    private String code;
    private BigDecimal value;
    private CouponStatus status;
    private String note;

    public PersonalCoupon() {
        /**
         * Costruttore vuoto intenzionale.
         * <p>
         * Necessario per permettere l'istanziazione dell'entità
         * tramite framework di serializzazione/deserializzazione
         * (ad es. Gson) e meccanismi basati su riflessione.
         */
    }




    public String getClientId() {
        return clientid;
    }

    public void setClientId(String clientId) {
        clientid = clientId;
    }

    public void setStatus(CouponStatus status) {
        this.status = status;
    }

    public CouponStatus getStatus() {
        return status;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getCouponId() {
        return couponid;
    }

    public void setCouponId(String couponId) {
        couponid = couponId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNote(String note) {
        this.note=note;
    }
    public String getNote(){
        return note;
    }
}