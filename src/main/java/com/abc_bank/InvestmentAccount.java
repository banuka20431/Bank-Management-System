package com.abc_bank;

import java.io.Serial;
import java.io.Serializable;

public class InvestmentAccount extends Account implements Serializable {

    @Serial
    private final static long serialVersionUID = 1L;

    InvestmentAccount(String Name, String mobilePhoneNumber, String Email, int accountNumber) {
        super(Name, mobilePhoneNumber, Email, accountNumber);
    }
}
