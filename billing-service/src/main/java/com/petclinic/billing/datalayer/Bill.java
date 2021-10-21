package com.petclinic.billing.datalayer;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "billId")
    private int billId;

    @Column(name="customerId")
    private int customerId;

    @Column(name = "visitType")
    private String visitType;

    @Column(name = "visit_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date = new Date();

    @Column(name = "amount")
    private double amount;

    public Bill(){}

    public Bill(int billId,int customerId, String visitType, Date date, double amount){
        this.billId = billId;
        this.customerId = customerId;
        this.visitType = visitType;
        this.date = date;
        this.amount = amount;
    }

    public Integer getId(){return id;}
    public int getBillId() {
        return billId;
    }
    public int getCustomerId(){return  customerId;}
    public Date getDate(){return date;}
    public String getVisitType(){return visitType;}
    public double getAmount(){return amount;}

    public void setId(Integer id) {
        this.id = id;
    }

    public void setBillId(int billId) {
        this.billId = billId;
    }

    public void setCustomerId(int customerId){this.customerId = customerId;}

    public void setDate(Date date) {
        this.date = date;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}