package org.poo.Commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.entities.Account;
import org.poo.entities.BusinessAccount;
import org.poo.entities.User;
import org.poo.entities.UserRepo;

public class AddNewBusinessAssociate implements Command {
   private String accountIban;
   private String role;
   private String email;
   private final int timestamp;
   private UserRepo userRepo;

   public AddNewBusinessAssociate(final String accountIban, final String role, final String email, final int timestamp, final UserRepo userRepo) {
      this.accountIban = accountIban;
      this.role = role;
      this.email = email;
      this.timestamp = timestamp;
      this.userRepo = userRepo;
   }

   @Override
    public void execute(ArrayNode output) {
       BusinessAccount account = (BusinessAccount)userRepo.getAccountByIBAN(accountIban);
       User userToBeAdded = userRepo.getUser(this.email);
       if (userToBeAdded == null) {
            return;
       }
        switch (role) {
            case "employee":
                account.addEmployee(userToBeAdded);
                userToBeAdded.addAccount(account);
                break;
            case "manager":
                account.addManager(userToBeAdded);
                userToBeAdded.addAccount(account);
                break;
            default:
                break;
        }
    }
}
