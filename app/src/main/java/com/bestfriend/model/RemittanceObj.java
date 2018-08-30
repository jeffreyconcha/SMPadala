package com.bestfriend.model;

public class RemittanceObj extends TransactionObj {

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
    public ReceiveObj receive;
    public TransferObj transfer;
    public int type;
}
