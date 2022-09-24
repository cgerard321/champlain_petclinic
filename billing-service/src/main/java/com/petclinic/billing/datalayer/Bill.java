package com.petclinic.billing.datalayer;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

//@EnableR2dbcRepositories
@Table("billings")
public class Bill {
    @Id
    private Integer id;

    private int bill_id;

    private int customer_id;

    private String visit_type;

//    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date visit_date = new Date();

    private double amount;

    public Bill(){}

    public Bill(int billId,int customerId, String visitType, Date date, double amount){
        this.bill_id = billId;
        this.customer_id = customerId;
        this.visit_type = visitType;
        this.visit_date = date;
        this.amount = amount;
    }

    public Integer getId(){return id;}
    public int getBillId() {
        return bill_id;
    }
    public int getCustomerId(){return customer_id;}
    public Date getDate(){return visit_date;}
    public String getVisitType(){return visit_type;}
    public double getAmount(){return amount;}

    public void setId(Integer id) {
        this.id = id;
    }

    public void setBillId(int bill_id) {
        this.bill_id = bill_id;
    }

    public void setCustomerId(int customer_id){this.customer_id = customer_id;}

    public void setDate(Date visit_date) {
        this.visit_date = visit_date;
    }

    public void setVisitType(String visit_type) {
        this.visit_type = visit_type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}