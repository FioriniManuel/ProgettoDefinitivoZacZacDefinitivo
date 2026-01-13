package com.ispw.progettoispw.bean;

import java.math.BigDecimal;

public class PrizeBean {
    private String pid;              // es. "P1"
    private String pname;            // es. "Taglio + Shampoo"
    private int requiredpoints;     // punti necessari
    private BigDecimal couponvalue; // valore coupon

    public PrizeBean() {}

    public PrizeBean(String pid, String name, int requiredpoints, BigDecimal couponValue) {
        this.pid = pid;
        this.pname = name;
        this.requiredpoints = requiredpoints;
        this.couponvalue = couponValue;
    }

    public String getId() { return pid; }
    public String getName() { return pname; }
    public int getRequiredpoints() { return requiredpoints; }
    public BigDecimal getCouponValue() { return couponvalue; }

    public void setId(String id) { this.pid = id; }
    public void setName(String name) { this.pname = name; }
    public void setRequiredpoints(int requiredpoints) { this.requiredpoints = requiredpoints; }
    public void setCouponValue(BigDecimal couponValue) { this.couponvalue = couponValue; }
}
