package org.poo.entities;

public class Commerciant {
    private String commerciant;
    private int id;
    private String account;
    private String type;
    private String cashbackStrategy;

    public Commerciant(String commerciant, int id, String account, String type, String cashbackStrategy) {
        this.commerciant = commerciant;
        this.id = id;
        this.account = account;
        this.type = type;
        this.cashbackStrategy = cashbackStrategy;
    }

    public String getCommerciant() {
        return commerciant;
    }

    public void setCommerciant(String commerciant) {
        this.commerciant = commerciant;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCashbackStrategy() {
        return cashbackStrategy;
    }

    public void setCashbackStrategy(String cashbackStrategy) {
        this.cashbackStrategy = cashbackStrategy;
    }
}
