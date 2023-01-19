package com.ab.quiz.pojo;

public class MoneyStatusOutput {
    private int clientGameId;
    private long serverGameId;
    private long gamePlayedTime;
    private int status;
    private int amount;
    private String message;

    public int getClientGameId() {
        return clientGameId;
    }

    public void setClientGameId(int clientGameId) {
        this.clientGameId = clientGameId;
    }

    public long getServerGameId() {
        return serverGameId;
    }

    public void setServerGameId(long serverGameId) {
        this.serverGameId = serverGameId;
    }

    public long getGamePlayedTime() {
        return gamePlayedTime;
    }

    public void setGamePlayedTime(long gamePlayedTime) {
        this.gamePlayedTime = gamePlayedTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
