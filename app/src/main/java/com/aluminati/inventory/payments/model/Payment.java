package com.aluminati.inventory.payments.model;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Payment implements Serializable {

    private final static String TAG = Payment.class.getName();
    private String number;
    private String name;
    private String expiryDate;
    private String cardRef;


    public Payment(String number, String name, String expiryDate, String cardRef){
        this.number = number;
        this.name = name;
        this.expiryDate = expiryDate;
        this.cardRef = cardRef;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setCardRef(String cardRef) {
        this.cardRef = cardRef;
    }

    public String getCardRef() {
        return cardRef;
    }

    @Override
    public String toString(){
        return "Name=".concat(getName()).concat(";Number=").concat(getNumber())
                .concat(";ExpiryDate=").concat(getExpiryDate()).concat(";CardRef=").concat(getCardRef());
    }

    public static Map<String, List<Map<String, Object>>> addTransaction(String sum, String type, String ref, String receiptRef){
        Map<String, List<Map<String, Object>>> newTransaction = new HashMap<>();
        Map<String, Object> transactions = new HashMap<>();
        ArrayList arrayList = new ArrayList();
                  arrayList.add(transactions);
        newTransaction.put("transactions", arrayList);
        transactions.put("date", getDate());
        transactions.put("amount", sum);
        transactions.put("ref", UUID.randomUUID().toString());
        transactions.put("receipt_ref", receiptRef);
        if(ref != null){
            transactions.put("cardRef", ref);
        }
        return newTransaction;
    }

    private static String getDate(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static ArrayList<Payment> stringToList(String concat){
        ArrayList<Payment> payments = new ArrayList<>();
        String[] concatSplit = concat.split("#");
        for(String ct : concatSplit){
            String[] split = ct.split(";");
            String name = split[0].split("Name=")[1];
            String number = split[1].split("Number=")[1];
            String expiryDate = split[2].split("ExpiryDate=")[1];
            String cardRef = split[3].split("CardRef=")[1];
            payments.add(new Payment(number,name,expiryDate,cardRef));
        }
        return payments;
    }

}
