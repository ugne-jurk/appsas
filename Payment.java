package com.example.skaiciuotuvas;

import java.util.Date;

public class Payment {
    int month;
    double monthlyPayment;
    double principal;
    double interest;
    double balance;
    Date paymentDate;
    boolean isDeferred;
    double deferredInterest;

    public Payment(int month, double monthlyPayment, double principal, double interest, double balance) {
        this.month = month;
        this.monthlyPayment = monthlyPayment;
        this.principal = principal;
        this.interest = interest;
        this.balance = balance;
        this.isDeferred = false;
        this.deferredInterest = 0.0;
    }

    public Payment(int month, double monthlyPayment, double principal, double interest,
                   double balance, Date paymentDate) {
        this(month, monthlyPayment, principal, interest, balance);
        this.paymentDate = paymentDate;
    }

    public void applyDeferment(double deferredAmount) {
        this.isDeferred = true;
        this.deferredInterest = deferredAmount;
        this.interest += deferredAmount;
        this.monthlyPayment += deferredAmount;
    }

    public double getTotalInterest() {
        return interest + deferredInterest;
    }

    public String getPaymentDateString() {
        if (paymentDate != null) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(paymentDate);
        }
        return "";
    }
}