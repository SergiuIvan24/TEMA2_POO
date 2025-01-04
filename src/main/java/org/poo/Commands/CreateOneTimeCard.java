package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.Account;
import org.poo.entities.Card;
import org.poo.entities.OneTimePayCard;
import org.poo.entities.Transaction;
import org.poo.entities.User;
import org.poo.entities.UserRepo;
import org.poo.utils.Utils;

public final class CreateOneTimeCard implements Command {
    private final String account;
    private final String email;
    private final UserRepo userRepo;
    private final int timestamp;

    public CreateOneTimeCard(final String account, final String email,
                             final UserRepo userRepo, final int timestamp) {
        this.account = account;
        this.email = email;
        this.userRepo = userRepo;
        this.timestamp = timestamp;
    }

    @Override
    public void execute(final ArrayNode output) {
        User user = userRepo.getUser(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found for email: " + email);
        }

        Account account = user.getAccount(this.account);

        Card newCard = new OneTimePayCard(Utils.generateCardNumber());
        account.addCard(newCard);
        Transaction transaction = new Transaction.Builder()
                .setTimestamp(timestamp)
                .setDescription("New card created")
                .setCard(newCard.getCardNumber())
                .setCardHolder(email)
                .setAccount(this.account)
                .build();

        account.addTransaction(transaction);

    }
}
