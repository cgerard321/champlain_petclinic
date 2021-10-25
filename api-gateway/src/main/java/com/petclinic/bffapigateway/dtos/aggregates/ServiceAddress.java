package com.petclinic.bffapigateway.dtos.aggregates;

public class ServiceAddress {
    private String authAddress;
    private String billingAddress;
    private String customersAddress;
    private String vetsAddress;
    private String visitsAddress;

    public ServiceAddress(String authAddress, String billingAddress, String customersAddress, String vetsAddress, String visitsAddress) {
        this.authAddress = authAddress;
        this.billingAddress = billingAddress;
        this.customersAddress = customersAddress;
        this.vetsAddress = vetsAddress;
        this.visitsAddress = visitsAddress;
    }

    public ServiceAddress(){}

    public String getAuthAddress() {
        return authAddress;
    }

    public void setAuthAddress(String authAddress) {
        this.authAddress = authAddress;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getCustomersAddress() {
        return customersAddress;
    }

    public void setCustomersAddress(String customersAddress) {
        this.customersAddress = customersAddress;
    }

    public String getVetsAddress() {
        return vetsAddress;
    }

    public void setVetsAddress(String vetsAddress) {
        this.vetsAddress = vetsAddress;
    }

    public String getVisitsAddress() {
        return visitsAddress;
    }

    public void setVisitsAddress(String visitsAddress) {
        this.visitsAddress = visitsAddress;
    }
}