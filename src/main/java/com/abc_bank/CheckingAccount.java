package com.abc_bank;

import java.io.Serial;
import java.io.Serializable;

public class CheckingAccount extends Account implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    CheckingAccount(String Name, String mobilePhoneNumber, String Email, int accountNumber) {
        super(Name, mobilePhoneNumber, Email, accountNumber);
    }

}
