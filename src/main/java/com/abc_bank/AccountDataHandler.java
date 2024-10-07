package com.abc_bank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;

public class AccountDataHandler {
    HashMap<String, String> holderData = new HashMap<>();
    ArrayList<ArrayList<Account>> allAccounts = new ArrayList<>();

    AccountDataHandler(HashMap<String, String> holderData) {
        this.holderData = holderData;
    }

    AccountDataHandler(ArrayList<ArrayList<Account>> allAccounts) {
        this.allAccounts = allAccounts;
    }

    public ArrayList<ArrayList<Account>> insertUpdatedAcc(Account a) throws SQLException {
        ArrayList<Account> updatedAccList;
        int accListIndex;
        String query = "SELECT holderAccTypeId FROM Holder WHERE holderAccNumber = ?";
        Database_Handler db_handler = new Database_Handler();

        db_handler.prepareQuery(query, a.getAccountNumber());
        ResultSet results = db_handler.getData();

        assert results != null;
        if (results.next()) {
            accListIndex = switch (results.getString(1)) {
                case "CHK_ACC" -> 0;
                case "SVG_ACC" -> 1;
                case "IVM_ACC" -> 2;
                case "CD_ACC" -> 3;
                default -> -1;
            };

            if (accListIndex >= 0) {
                updatedAccList = updatedAccList(allAccounts.get(accListIndex), a);
                allAccounts.remove(accListIndex);
                allAccounts.add(updatedAccList);
            }
        }

        results.close();

        return allAccounts;
    }

    public Account exportData() throws SQLException {

        
        Database_Handler db_handler = new Database_Handler();
        ArrayList<String> placeholderValues = new ArrayList<>();
        String accountTypeId = this.holderData.get("Account Type Id");
        String accountNumber = this.holderData.get("Account Number");
        String Name = this.holderData.get("Name with Initials");
        String mobilePhoneNumber = this.holderData.get("Mobile Phone Number");
        String Email = this.holderData.get("Email");

        String[] holdersDataHeaders = {"Account Number", "Account Type Id", "Balance", "Pin Code Hash", "Full Name",
                "Name with Initials", "Title", "Profession", "Mobile Phone Number", "Telephone Number",
                "Address", "Postal Code", "Email", "National Identity Card Number", "Date of Birth", "Online Banking",
                "Debit Card", "SMS Alerts"
        };

        String query =

        """ 
        INSERT INTO Holder(holderAccNumber, holderAccTypeId, holderAccBalance, holderPinCodeHash, holderFullName,
                            holderName, holderTitle, holderProfession, holderMobileNumber,
                            holderTelephoneNumber, holderAddress, holderPostalCode, holderEmail, holderNIC, holderDOB,
                            onlineBanking, debitCard, smsAlerts)
         VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        
        for(String header : holdersDataHeaders) {
            placeholderValues.add(holderData.get(header));
        }

        db_handler.prepareQuery(query, placeholderValues);
        db_handler.saveData();

        // returning account object that equals to account type
        return switch (accountTypeId) {
            case "CHK_ACC" ->
                    new CheckingAccount(Name, mobilePhoneNumber, Email, Integer.parseInt(accountNumber));
            case "SVG_ACC" -> new SavingAccount(Name, mobilePhoneNumber, Email, Integer.parseInt(accountNumber));
            case "IVM_ACC" ->
                    new InvestmentAccount(Name, mobilePhoneNumber, Email, Integer.parseInt(accountNumber));
            case "CD_ACC" ->
                    new CertifiedDepositAccount(Name, mobilePhoneNumber, Email, Integer.parseInt(accountNumber));
            default -> null;
        };
    }

    public Account searchAccount() {

        String[] methods = {"By Account Number", "By Name", "By Mobile Phone Number", "By Email"};

        System.out.println("\n Enter a Method for Searching the required account >");
        int selectedOptionIndex = Accessories.Menu.displayMenu(methods, true);
        String Method = methods[selectedOptionIndex];

        String input = "";
        System.out.print("\nEnter : ");

        switch (Method) {
            case "By Name", "By Email" -> input = Main.read.nextLine();
            case "By Account Number" -> {
                try {
                    input = String.valueOf(Main.read.nextInt());
                    Main.read.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("\nError! Invalid Input");
                    Main.read.nextLine();
                }
            }
            case "By Mobile Phone Number" -> {
                try {
                    input = String.valueOf(Main.read.nextInt());
                    input = (input.length() == 9) ? "0" + input : input;
                    Main.read.nextLine();
                } catch (InputMismatchException e) {
                    System.out.println("\nError! Invalid Input");
                    Main.read.nextLine();
                }
            }
        }

        for (ArrayList<Account> accountList : this.allAccounts) {
            if(!accountList.isEmpty()) {
                for (Account acc : accountList) {
                    String reqMethodValue = switch (Method) {
                        case "By Account Number" -> acc.getAccountNumber();
                        case "By Name" -> acc.getName();
                        case "By Mobile Phone Number" -> acc.getMobilePhoneNumber();
                        case "By Email" -> acc.getEmail();
                        default -> "";
                    };
                    input = input.replace(" ", "");
                    reqMethodValue = reqMethodValue.replace(" ", "");
                    if (reqMethodValue.equalsIgnoreCase(input)) {
                        return acc;
                    }
                }
            }
        }
        return null;
    }


    public ArrayList<Account> updatedAccList(ArrayList<Account> accList, Account updatedAcc) {
        for(Account acc : accList) {
            if(acc.getAccountNumber().equals(updatedAcc.getAccountNumber())) {
                accList.remove(acc);
                accList.add(updatedAcc);
                break;
            }
        }
        return accList;
    }

    public static boolean checkAccountStatus(String accNo) throws SQLException {
        Database_Handler db_handler = new Database_Handler();
        String query = "SELECT status FROM Holder WHERE holderAccNumber = ?";
        db_handler.prepareQuery(query, accNo);
        ResultSet resultSet = db_handler.getData();
        assert resultSet != null;
        if(resultSet.next()) {
            return resultSet.getBoolean(1);
        }
        return false;
    }
}
