package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import com.petclinic.bffapigateway.dtos.Bills.BillResponseDTO;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.time.LocalDate;

import static com.petclinic.bffapigateway.dtos.Bills.BillStatus.PAID;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

public class MockServerConfigBillService {

    private static final Integer BILL_SERVICE_SERVER_PORT = 7004;
    private final ClientAndServer clientAndServer;
    private final MockServerClient mockServerClient_BillService = new MockServerClient("localhost", BILL_SERVICE_SERVER_PORT);
    public MockServerConfigBillService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(BILL_SERVICE_SERVER_PORT);
    }

    public void registerGetAllBillsEndpoint() {

        String response = "["
                + "{\"billId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361b\",\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"visitType\":\"general\",\"vetId\":\"2\",\"date\":\"2024-10-11\",\"amount\":\"120\",\"taxedAmount\":\"0.0\", \"billStatus\":\"UNPAID\", \"dueDate\":\"2024-10-13\"},"
                + "{\"billId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"visitType\":\"general\",\"vetId\":\"2\",\"date\":\"2024-10-11\",\"amount\":\"100\",\"taxedAmount\":\"10.0\", \"billStatus\":\"PAID\", \"dueDate\":\"2024-10-13\"},"
                + "{\"billId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361c\",\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"visitType\":\"general\",\"vetId\":\"2\",\"date\":\"2024-10-11\",\"amount\":\"100\",\"taxedAmount\":\"10.0\", \"billStatus\":\"OVERDUE\", \"dueDate\":\"2024-10-13\"}"
                + "]";

        mockServerClient_BillService
                .when(
                    request()
                            .withMethod("GET")
                            .withPath("/bills")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(response))
                );
    }

    public void registerDeleteBillEndpoint() {
        BillResponseDTO billResponse = new BillResponseDTO();
        billResponse.setBillId("e6c7398e-8ac4-4e10-9ee0-03ef33f0361a");
        billResponse.setCustomerId("Betty");
        billResponse.setVisitType("general");
        billResponse.setVetId("3");
        billResponse.setDate(LocalDate.parse("2024-10-11"));
        billResponse.setAmount(100.0);
        billResponse.setTaxedAmount(0.0);
        billResponse.setBillStatus(PAID);
        billResponse.setDueDate(LocalDate.parse("2024-10-13"));

        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("DELETE")
                                .withPath("/bills/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(billResponse))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}
