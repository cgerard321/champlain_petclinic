package com.petclinic.inventoryservice.businesslayer;

import com.lowagie.text.DocumentException;
import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.Product;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.utils.InventoryValidator;
import com.petclinic.inventoryservice.utils.SupplyReportPdfGenerator;
import com.petclinic.inventoryservice.utils.exceptions.UnprocessableEntityException;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class SupplyReportExecutionTest {
    private final ProductRepository products = mock(ProductRepository.class);
    private final SupplyReportPdfGenerator generator = spy(new SupplyReportPdfGenerator());
    private final ProductInventoryServiceImpl service = new ProductInventoryServiceImpl(
            mock(InventoryRepository.class), products, mock(InventoryTypeRepository.class),
            mock(InventoryValidator.class), mock(SupplyAuditRepository.class), generator);

    @Test
    void pdfRenderingDoesNotRunOnReactiveRequestThread() {
        AtomicBoolean renderedOnRequestThread = new AtomicBoolean();
        Product supply = Product.builder().productName("Gauze").productQuantity(10)
                .productDescription("Sterile").productPrice(2.0).productSalePrice(3.0).build();
        doAnswer(invocation -> {
            renderedOnRequestThread.set(Schedulers.isInNonBlockingThread());
            return invocation.callRealMethod();
        }).when(generator).generate(eq("inv1"), anyList(), eq("fr"));
        when(products.findAllProductsByInventoryId("inv1")).thenReturn(Flux.just(supply));

        Mono<byte[]> report = Mono.defer(() -> service.createSupplyPdf("inv1", "fr"))
                .subscribeOn(Schedulers.parallel());

        StepVerifier.create(report)
                .assertNext(pdf -> {
                    assertEquals("%PDF-", new String(pdf, 0, 5, StandardCharsets.US_ASCII));
                    assertFalse(renderedOnRequestThread.get(),
                            "Synchronous PDF rendering must run outside the reactive request thread");
                })
                .expectComplete().verify(Duration.ofSeconds(5));
    }

    @Test
    void renderingFailureBecomesServiceErrorAndRetainsCause() {
        DocumentException failure = new DocumentException("PDF writer failed");
        doThrow(failure).when(generator).generate(eq("inv1"), anyList(), eq("fr"));
        when(products.findAllProductsByInventoryId("inv1"))
                .thenReturn(Flux.just(Product.builder().productName("Gauze").build()));

        StepVerifier.create(service.createSupplyPdf("inv1", "fr"))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(UnprocessableEntityException.class, error);
                    assertEquals("Error generating PDF", error.getMessage());
                    assertSame(failure, error.getCause());
                })
                .verify(Duration.ofSeconds(5));
    }
}
