package com.ispw.progettoispw.entity;

public class LoyaltyAccount {
    private String LoyaltyAccountId;
    private String clientId;
    private int currentPoints;
    private int lifetimeEarned;
    private int lifetimeRedeemed;

    public LoyaltyAccount(){}

    public LoyaltyAccount(String LoyaltyAccountId,String clientId){
        this.LoyaltyAccountId=LoyaltyAccountId;
        this.clientId=clientId;
        this.currentPoints=0;
        this.lifetimeEarned=0;
        this.lifetimeRedeemed=0;
    }
    public LoyaltyAccount(String clientId,int pts){
        this.clientId=clientId;
        this.currentPoints=pts;
    }


    // GETTER / SETTER


    public String getLoyaltyAccountId() {
        return LoyaltyAccountId;
    }

    public void setLoyaltyAccountId(String loyaltyAccountId) {
        LoyaltyAccountId = loyaltyAccountId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public int getPoints() {
        return currentPoints;
    }

    public void setPoints(int currentPoints) {
        this.currentPoints = Math.max(0,currentPoints);
    }

    public int getLifetimeEarned() {
        return lifetimeEarned;
    }

    public void setLifetimeEarned(int lifetimeEarned) {
        this.lifetimeEarned = Math.max(0,lifetimeEarned);
    }

    public int getLifetimeRedeemed() {
        return lifetimeRedeemed;
    }

    public void setLifetimeRedeemed(int lifetimeRedeemed) {
        this.lifetimeRedeemed = Math.max(0,lifetimeRedeemed);
    }


    public void addPoints(int points) {
        this.currentPoints=this.currentPoints+points;
    }
}
