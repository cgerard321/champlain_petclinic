package com.petclinic.bffapigateway.dtos.aggregates;

import com.petclinic.bffapigateway.dtos.OwnerDetails;
import com.petclinic.bffapigateway.dtos.VetDetails;
import com.petclinic.bffapigateway.dtos.VisitDetails;
import com.petclinic.bffapigateway.dtos.aggregates.ServiceAddress;

import java.util.List;

public class CustomerVisitsVetsAggregate {

    private List<OwnerDetails> ownerInfo;
    private List<VisitDetails> visitsInfo;
    private List<VetDetails> vetsInfo;

    public CustomerVisitsVetsAggregate(List<OwnerDetails> ownerInfo, List<VisitDetails> visitsInfo, List<VetDetails> vetsInfo) {
        this.ownerInfo = ownerInfo;
        this.visitsInfo = visitsInfo;
        this.vetsInfo = vetsInfo;
    }

    public CustomerVisitsVetsAggregate(){}


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

}
