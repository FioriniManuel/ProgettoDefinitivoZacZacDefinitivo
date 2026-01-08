package com.ispw.progettoispw.bean;


import java.util.List;

public class FidelityBean {
    private int totalPoints;            // punti attuali del cliente
    private String selectedPrizeId;     // "P1" | "P2" | "P3"
    private List<PrizeBean> prizes;   // lista premi da mostrare

    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }

    public String getSelectedPrizeId() { return selectedPrizeId; }
    public void setSelectedPrizeId(String selectedPrizeId) { this.selectedPrizeId = selectedPrizeId; }

    public void setPrizes(List<PrizeBean> prizes) { this.prizes = prizes; }

}

