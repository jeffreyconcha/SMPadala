package com.bestfriend.model;

public class RemittanceData extends TransactionData {

    public String smDate;
    public String smTime;
    public String referenceNo;
    public String mobileNo;
    public float amount;
    public float balance;
    public float charge;
    public boolean isClaimed;
    public boolean hasBalance;
    public boolean isMarked;
    public ReceiveData receive;
    public TransferData transfer;
    public int type;
}
