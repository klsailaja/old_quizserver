package com.ab.quiz.pojo;

public class LoadMoney {
    private long uid;
    private int amount;
    private int coinCount;
    private boolean moneyMoney;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getCoinCount() {
        return coinCount;
    }

    public void setCoinCount(int coinCount) {
        this.coinCount = coinCount;
    }

    public boolean isMoneyMoney() {
        return moneyMoney;
    }

    public void setMoneyMoney(boolean moneyMoney) {
        this.moneyMoney = moneyMoney;
    }
}