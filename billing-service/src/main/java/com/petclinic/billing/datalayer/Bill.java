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

    @Column(name = "visit_date")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date = new Date();

    @Column(name = "visitType")
    private String visitType;

    @Column(name = "amount")
    private double amount;

    public Bill(){}

    public Bill(Date date, String visitType, double amount){
        this.date = date;
        this.visitType = visitType;
        this.amount = amount;
    }

    public Integer getId(){return id;}
    public Date getDate(){return date;}
    public String getVisitType(){return visitType;}
    public double getAmount(){return amount;}


}
