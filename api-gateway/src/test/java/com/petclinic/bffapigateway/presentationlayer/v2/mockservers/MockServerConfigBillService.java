package com.petclinic.bffapigateway.presentationlayer.v2.mockservers;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Parameter;

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
                + "{\"billId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361b\",\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"visitType\":\"general\",\"vetId\":\"2\",\"date\":\"" + java.time.LocalDate.now().plusDays(1) + "\",\"amount\":\"120\",\"taxedAmount\":\"0.0\", \"billStatus\":\"UNPAID\", \"dueDate\":\"" + java.time.LocalDate.now().plusDays(46) + "\"},"
                + "{\"billId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"visitType\":\"general\",\"vetId\":\"2\",\"date\":\"" + java.time.LocalDate.now().plusDays(1) + "\",\"amount\":\"100\",\"taxedAmount\":\"10.0\", \"billStatus\":\"UNPAID\", \"dueDate\":\"" + java.time.LocalDate.now().plusDays(46) + "\"}"
                + "]";

        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/bills")
                                .withQueryStringParameters(
                                        Parameter.param("page", "[0-9]+"), // Expecting digit characters for page
                                        Parameter.param("size", "[0-9]+")  // Expecting digit characters for size
                                ),
                        Times.unlimited()
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(response))
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerCreateBillEndpoint() {
        String response = "{\"billId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361b\",\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"visitType\":\"general\",\"vetId\":\"3\",\"date\":\"" + java.time.LocalDate.now().plusDays(1) + "\",\"amount\":\"100\",\"taxedAmount\":\"0.0\", \"billStatus\":\"UNPAID\", \"dueDate\":\"" + java.time.LocalDate.now().plusDays(46) + "\"}";

        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/bills")
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withBody(json(response))
                );
    }


    public void registerDownloadBillPdfEndpoint() {
        byte[] mockPdfContent = "Mock PDF Content".getBytes();

        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v2/gateway/customers/1/bills/1234/pdf")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(mockPdfContent)
                                .withHeader("Content-Type", "application/pdf")
                );
    }


    public void registerUpdateBillEndpoint() {
        String response = "{\"billId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"customerId\":\"e6c7398e-8ac4-4e10-9ee0-03ef33f0361a\",\"visitType\":\"operation\",\"vetId\":\"3\",\"date\":\"" + java.time.LocalDate.now().plusDays(1) + "\",\"amount\":100.00, \"taxedAmount\": 0.0, \"billStatus\":\"PAID\", \"dueDate\":\"" + java.time.LocalDate.now().plusDays(46) + "\"}";

        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("PUT")
                                .withPath("/bills/e6c7398e-8ac4-4e10-9ee0-03ef33f0361a")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json(response))
                );
    }

    public void registerGetCurrentBalanceEndpoint() {
        String response = "150.0";

        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v2/gateway/customers/{customerId}/bills/current-balance")
                                .withPathParameter("customerId", "1")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(response)
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerGetCurrentBalanceInvalidCustomerIdEndpoint() {
        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v2/gateway/customers/{customerId}/bills/current-balance")
                                .withPathParameter("customerId", "invalid-id")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withHeader("Content-Type", "application/json")
                );
    }

    public void registerPayBillEndpoint() {
        // success case
        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/api/v2/gateway/customers/1/bills/1234/pay")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"billId\":\"1234\",\"customerId\":\"1\",\"billStatus\":\"PAID\"}")
                );

        // invalid customer case
        mockServerClient_BillService
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/api/v2/gateway/customers/invalid-id/bills/1234/pay")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withHeader("Content-Type", "application/json")
                                .withBody("{\"error\":\"Customer not found\"}")
                );
    }

    public void registerDownloadStaffBillPdfEndpoint() {
        mockServerClient_BillService
                .when(
                request()
                        .withMethod("GET")
                        .withPath("/bills/staffBill-1/pdf")
        ).respond(
                response()
                        .withStatusCode(200)
                        .withHeader("Content-Type", "application/pdf")
                        .withBody("Fake Staff PDF Content".getBytes())
        );

        // Optional: simulate "not found" case
        mockServerClient_BillService
                .when(
                request()
                        .withMethod("GET")
                        .withPath("/bills/nonexistent-bill-id/pdf")
        ).respond(
                response()
                        .withStatusCode(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Bill not found\"}")
        );
    }

    public void stopMockServer() {
        if(clientAndServer != null)
            this.clientAndServer.stop();
    }
}