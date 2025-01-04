package org.poo.entities;

public final class ClassicAccount extends Account {
    private String accountType;
    public ClassicAccount(final String iban, final String currency,
                          final double balance, final String accountType) {
        super(iban, currency, balance);
        this.accountType = accountType;
    }


    @Override
    public String getAccountType() {
        return accountType;
    }

    @Override
    public void update(boolean accepted) {
        return;
    }
}
