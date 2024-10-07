package com.abc_bank;

import java.io.Serial;
import java.io.Serializable;
import java.sql.SQLException;

public class Account implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;
    private final String Name;
    private String mobilePhoneNumber;
    private String Email;
    private final int accountNumber;
    private int monthlyTransactionCount;

    Account(String Name, String mobilePhoneNumber, String Email, int accountNumber) {
        this.Name = Name;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.Email = Email;
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return String.valueOf(accountNumber);
    }

    public String getName() {
        return this.Name;
    }

    public String getMobilePhoneNumber() {
        return this.mobilePhoneNumber;
    }

    public String getEmail() {
        return this.Email;
    }

    public String displayAccount() {

        String printStr =
                String.format("""
                        \t\t\t\t\t+-----------------------------------------------------+
                        \t\t\t\t\t> Holder : %S
                        \t\t\t\t\t> Mobile Phone Number : %s
                        \t\t\t\t\t> Email : %s
                        \t\t\t\t\t+-----------------------------------------------------+
                        """,
                        this.Name,
                        this.mobilePhoneNumber,
                        this.Email
                );
        
        System.out.printf(
                """
                        +----------------------------------------+
                        > Holder : %S
                        > Mobile Phone Number : %s
                        > Email : %s
                        +----------------------------------------+
                        %n""",
                this.Name,
                this.mobilePhoneNumber,
                this.Email
        );

        return printStr;

    }

    public void  setEmail(String newEmail) throws SQLException {
        Database_Handler db_handler = new Database_Handler();
        this.Email = newEmail;
        String query = "UPDATE holder set holderEmail = ? WHERE holderAccNumber = ?";
        String[] placeholderValues = {newEmail, this.getAccountNumber()};
        
        db_handler.prepareQuery(query, placeholderValues);
        db_handler.saveData();
    }

    public  void setMobilePhoneNumber(String newMobilePhoneNumber) throws SQLException {
        this.mobilePhoneNumber = newMobilePhoneNumber;
        String query = "UPDATE holder set holderMobileNumber = ? WHERE holderAccNumber = ?";
        String[] placeholderValues = {newMobilePhoneNumber, this.getAccountNumber()};
        Database_Handler db_handler = new Database_Handler();

        db_handler.prepareQuery(query, placeholderValues);
        db_handler.saveData();
    }

    public int getMonthlyTransactionCount() {
        return monthlyTransactionCount;
    }

    public void setMonthlyTransactionCount() {
        this.monthlyTransactionCount++;
    }

    public void setMonthlyTransactionCount(int count) {
        this.monthlyTransactionCount = count;
    }

    public void resetMonthlyTransactionCount() {
        this.monthlyTransactionCount = 0;
    }

}
