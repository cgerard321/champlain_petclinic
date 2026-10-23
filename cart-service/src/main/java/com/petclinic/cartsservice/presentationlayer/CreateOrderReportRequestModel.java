package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderReportRequestModel {

    private OrderReport.Reason reason;

    @Builder.Default
    private List<String> productIds = List.of();

    private String description;
}
