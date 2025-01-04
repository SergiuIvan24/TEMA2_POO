package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.Account;
import org.poo.entities.Card;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public final class DeleteCard implements Command {
    private String email;
    private String cardNumber;
    private final int timestamp;
    private UserRepo userRepo;

    public DeleteCard(final String email, final String cardNumber,
                      final UserRepo userRepo, final int timestamp) {
        this.email = email;
        this.cardNumber = cardNumber;
        this.userRepo = userRepo;
        this.timestamp = timestamp;
    }

    @Override
    public void execute(final ArrayNode output) {
        User user = userRepo.getUser(email);
        Account account = user.getAccountByCardNumber(cardNumber);
        if (account == null) {
            return;
        }
        Card card = account.getCard(cardNumber);
        account.removeCard(card);
        Transaction transaction = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription("The card has been destroyed")
                .setCard(card.getCardNumber())
                .setCardHolder(email)
                .setAccount(account.getIban())
                .build();

        account.addTransaction(transaction);
    }
}
