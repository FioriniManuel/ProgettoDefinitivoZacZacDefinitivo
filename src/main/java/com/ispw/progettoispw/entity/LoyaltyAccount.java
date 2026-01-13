package com.ispw.progettoispw.entity;

public class LoyaltyAccount {
    private String loyaltyaccountid;
    private String clientId;
    private int currentPoints;
    private int lifetimeEarned;
    private int lifetimeRedeemed;

    public LoyaltyAccount(){}

    public LoyaltyAccount(String loyaltyaccount,String clientId){
        this.loyaltyaccountid =loyaltyaccount;
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


    public String getLoyaltyaccountid() {
        return loyaltyaccountid;
    }

    public void setLoyaltyaccountid(String loyaltyaccountid) {
        this.loyaltyaccountid = loyaltyaccountid;
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
