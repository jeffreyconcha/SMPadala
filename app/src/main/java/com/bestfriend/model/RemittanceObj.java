package com.bestfriend.model;

public class RemittanceObj extends TransactionObj {

    public String smDate;
    public String smTime;
    public String name;
    public String referenceNo;
    public String mobileNo;
    public float amount;
    public float balance;
    public float charge;
    public boolean isClaimed;
    public boolean hasBalance;
    public ReceiveObj receive;
    public int type;
}
