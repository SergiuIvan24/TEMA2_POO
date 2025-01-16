package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.BusinessAccount;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public final class AddNewBusinessAssociate implements Command {
   private String accountIban;
   private String role;
   private String email;
   private final int timestamp;
   private UserRepo userRepo;

   public AddNewBusinessAssociate(final String accountIban,
                                  final String role, final String email,
                                  final int timestamp, final UserRepo userRepo) {
      this.accountIban = accountIban;
      this.role = role;
      this.email = email;
      this.timestamp = timestamp;
      this.userRepo = userRepo;
   }

   @Override
    public void execute(final ArrayNode output) {
       BusinessAccount account = (BusinessAccount) userRepo.getAccountByIBAN(accountIban);
       User user = userRepo.getUser(this.email);
       if (user == null) {
            return;
       }
       if (account == null) {
           return;
       }
       if (account.getOwner() == user || account.getManagers().contains(user)
               || account.getEmployees().contains(user)) {
           return;
       }
        switch (role) {
            case "employee":
                account.addEmployee(user);
                user.addAccount(account);
                account.addTimestampWhenBecameAssociate(user, timestamp);
                break;
            case "manager":
                account.addManager(user);
                user.addAccount(account);
                account.addTimestampWhenBecameAssociate(user, timestamp);
                break;
            default:
                break;
        }
    }
}
