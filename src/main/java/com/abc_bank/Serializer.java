package com.abc_bank;

import java.io.*;
import java.util.ArrayList;

public class Serializer {
    public static void serialize(ArrayList<Account> accounts, File file) {
        try {
            FileOutputStream FOUT = new FileOutputStream(file);
            ObjectOutputStream OOS = new ObjectOutputStream(FOUT);
            OOS.writeObject(accounts);
            OOS.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Account> deserialize(File file) {
        ArrayList<Account> accountList = new ArrayList<>();
        try {
            FileInputStream FIN = new FileInputStream(file);
            ObjectInputStream OIS = new ObjectInputStream(FIN);
            accountList = (ArrayList<Account>) OIS.readObject();
            OIS.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountList;
    }
}