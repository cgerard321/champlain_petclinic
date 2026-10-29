package com.petclinic.cartsservice.presentationlayer;

import com.petclinic.cartsservice.dataaccesslayer.orderreport.OrderReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderReportStatusRequestModel {

    private OrderReportStatus status;

    private String resolutionNote;
}
