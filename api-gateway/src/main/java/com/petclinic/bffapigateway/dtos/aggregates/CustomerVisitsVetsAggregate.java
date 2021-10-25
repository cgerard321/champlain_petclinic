package com.petclinic.bffapigateway.dtos.aggregates;

import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.VetDetails;
import com.petclinic.bffapigateway.dtos.VisitDetails;
import com.petclinic.bffapigateway.dtos.aggregates.ServiceAddress;

import java.util.List;

public class CustomerVisitsVetsAggregate {
    private int customerId;
    private String name;
    private String petName;
    private List<OwnerDetails> ownerInfo;
    private List<VisitDetails> visitsInfo;
    private List<VetDetails> vetsInfo;
    private ServiceAddress serviceAddress;

    public CustomerVisitsVetsAggregate(int customerId, String name, String petName, List<OwnerDetails> ownerInfo, List<VisitDetails> visitsInfo, List<VetDetails> vetsInfo, ServiceAddress serviceAddress) {
        this.customerId = customerId;
        this.name = name;
        this.petName = petName;
        this.ownerInfo = ownerInfo;
        this.visitsInfo = visitsInfo;
        this.vetsInfo = vetsInfo;
        this.serviceAddress = serviceAddress;
    }

    public CustomerVisitsVetsAggregate(){}

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public List<OwnerDetails> getOwnerInfo() {
        return ownerInfo;
    }

    public void setOwnerInfo(List<OwnerDetails> ownerInfo) {
        this.ownerInfo = ownerInfo;
    }

    public List<VisitDetails> getVisitsInfo() {
        return visitsInfo;
    }

    public void setVisitsInfo(List<VisitDetails> visitsInfo) {
        this.visitsInfo = visitsInfo;
    }

    public List<VetDetails> getVetsInfo() {
        return vetsInfo;
    }

    public void setVetsInfo(List<VetDetails> vetsInfo) {
        this.vetsInfo = vetsInfo;
    }

    public ServiceAddress getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(ServiceAddress serviceAddress) {
        this.serviceAddress = serviceAddress;
    }
}
