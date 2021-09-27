package com.petclinic.billing.datalayer;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
public class BillDTO {

    private Date date;
    private String visitType;
    private double amount;

    public BillDTO(){
        billId = 0;
        date = null;
        visitType = null;
        amount = 0;
    }

    public BillDTO(int billId, Date date, String visitType, double amount){
        this.billId = billId;
        this.date = date;
        this.visitType = visitType;
        this.amount = amount;
    }

    public int getBillId(){
        return billId;
    }
    public Date getDate(){
        return date;
    }
    public String getVisiType(){
        return visitType;
    }
    public double getAmount(){
        return amount;
    }

    public void setBillId(int billId){
        this.billId = billId;
    }

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
