package com.petclinic.billing.datalayer;

import lombok.*;
import org.springframework.lang.Nullable;

import java.util.Date;


public class BillDTO {

    private int bill_id;
    private int customer_id;
    private String visit_type;
    private Date date;
    @Nullable
    private double amount;

    public BillDTO(){
        bill_id = 0;
        customer_id = 0;
        visit_type = null;
        date = null;
        amount = 0;

    }


    public BillDTO(int billId,int customerId, String visitType, Date date, double amount){
        this.bill_id = billId;
        this.customer_id = customerId;
        this.visit_type = visitType;
        this.date = date;
        this.amount = amount;
    }

    public BillDTO(int billId,int customerId, Date date, String visitType){
        this.bill_id = billId;
        this.customer_id = customerId;
        this.date = date;
        this.visit_type = visitType;
    }



    public int getBillId(){
        return bill_id;
    }
    public int getCustomerId(){return customer_id;}
    public Date getDate(){
        return date;
    }
    public String getVisitType(){
        return visit_type;
    }
    public double getAmount(){
        return amount;
    }

    public void setBillId(int billId){
        this.bill_id = billId;
    }

    public void setCustomerId(int customerId){this.customer_id = customerId;}

    public void setDate(Date date){
        this.date = date;
    }

    public void setVisitType(String visitType){
        this.visit_type = visitType;
    }

    public void setAmount(double amount){
        this.amount = amount;
    }
}