package com.petclinic.billing.datalayer;

import lombok.*;
import org.springframework.lang.Nullable;

import java.util.Date;


public class BillDTO {

    private int billId;
    private int customerId;
    private String visitType;
    private Date date;
    @Nullable
    private double amount;

    public BillDTO(){
        billId = 0;
        customerId = 0;
        visitType = null;
        date = null;
        amount = 0;

    }


    public BillDTO(int billId,int customerId, String visitType, Date date, double amount){
        this.billId = billId;
        this.customerId = customerId;
        this.visitType = visitType;
        this.date = date;
        this.amount = amount;
    }

    public BillDTO(int billId,int customerId, Date date, String visitType){
        this.billId = billId;
        this.customerId = customerId;
        this.date = date;
        this.visitType = visitType;
    }



    public int getBillId(){
        return billId;
    }
    public int getCustomerId(){return customerId;}
    public Date getDate(){
        return date;
    }
    public String getVisitType(){
        return visitType;
    }
    public double getAmount(){
        return amount;
    }

    public void setBillId(int billId){
        this.billId = billId;
    }

    public void setCustomerId(int customerId){this.customerId = customerId;}

    public void setDate(Date date){
        this.date = date;
    }

    public void setVisitType(String visitType){
        this.visitType = visitType;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }
}