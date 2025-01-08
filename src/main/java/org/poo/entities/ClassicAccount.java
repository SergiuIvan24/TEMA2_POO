package org.poo.entities;

public final class ClassicAccount extends Account {
    private String accountType;
    public ClassicAccount(final String iban, final String currency,
                          final double balance, final String accountType, final String email, UserRepo userRepo) {
        super(iban, currency, balance, email, userRepo);
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
