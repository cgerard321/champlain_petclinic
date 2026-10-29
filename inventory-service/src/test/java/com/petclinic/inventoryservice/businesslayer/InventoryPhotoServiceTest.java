package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Audit.SupplyAuditRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.Inventory;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Product.ProductRepository;
import com.petclinic.inventoryservice.presentationlayer.InventoryRequestDTO;
import com.petclinic.inventoryservice.utils.InventoryValidator;
import com.petclinic.inventoryservice.utils.SupplyReportPdfGenerator;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventoryPhotoServiceTest {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final Map<String, String> NAMES = Map.of("en", "Bandages", "fr", "Pansements");
    private static final Map<String, String> DESCRIPTIONS = Map.of("en", "Wound care", "fr", "Soins des plaies");
    private static final String LINK = "https://example.com/bandages.jpg";
    private static final String BACKUP_LINK = "https://example.com/backup.jpg";
    private static final byte[] OLD_PHOTO = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x01};
    private static final byte[] NEW_PHOTO = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    private final InventoryRepository inventories = mock(InventoryRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final InventoryTypeRepository types = mock(InventoryTypeRepository.class);
    private final ProductInventoryServiceImpl service = new ProductInventoryServiceImpl(
            inventories, products, types, new InventoryValidator(inventories, types, products),
            mock(org.springframework.data.mongodb.core.ReactiveMongoTemplate.class),
            mock(SupplyAuditRepository.class), new SupplyReportPdfGenerator());

    private Inventory current;

    @BeforeEach
    void setUp() {
        current = Inventory.builder().inventoryId("current").inventoryName("Bandages").inventoryType("Equipment")
                .inventoryDescription("Wound care").name(NAMES).description(DESCRIPTIONS)
                .inventoryImage(LINK).inventoryBackupImage(BACKUP_LINK).imageUploaded(OLD_PHOTO.clone())
                .build();
        when(inventories.findInventoryByInventoryId("current")).thenReturn(Mono.just(current));
        when(inventories.findAll()).thenReturn(Flux.just(current));
        when(inventories.existsByInventoryNameAndInventoryIdNot("Bandages", "current")).thenReturn(Mono.just(false));
        when(inventories.save(any(Inventory.class))).thenAnswer(call -> Mono.just(call.getArgument(0)));
    }

    @Test
    void editingDetailsWithoutAPhotoKeepsTheCurrentPhoto() {
        StepVerifier.create(service.updateInventory(Mono.just(request().inventoryType("Medications").build()), "current"))
                .assertNext(response -> {
                    assertEquals("Medications", response.getInventoryType());
                    assertArrayEquals(OLD_PHOTO, response.getImageUploaded());
                    assertEquals(LINK, response.getInventoryImage());
                    assertEquals(BACKUP_LINK, response.getInventoryBackupImage());
                })
                .expectComplete().verify(TIMEOUT);
    }

    @Test
    void uploadingAPhotoReplacesTheOldPhotoAndImageLinks() {
        StepVerifier.create(service.updateInventory(Mono.just(request().imageUploaded(NEW_PHOTO).build()), "current"))
                .assertNext(response -> {
                    assertArrayEquals(NEW_PHOTO, response.getImageUploaded());
                    assertNull(response.getInventoryImage());
                    assertNull(response.getInventoryBackupImage());
                    assertEquals(NAMES, response.getName());
                })
                .expectComplete().verify(TIMEOUT);
    }

    @Test
    void anEmptyUploadRemovesEveryPhoto() {
        StepVerifier.create(service.updateInventory(Mono.just(request().imageUploaded(new byte[0]).build()), "current"))
                .assertNext(response -> {
                    assertNull(response.getImageUploaded());
                    assertNull(response.getInventoryImage());
                    assertNull(response.getInventoryBackupImage());
                })
                .expectComplete().verify(TIMEOUT);
    }

    @Test
    void aPhotoOverTheSizeLimitIsRejectedAndNothingIsSaved() {
        byte[] tooBig = new byte[160 * 1024 + 1];

        StepVerifier.create(service.updateInventory(Mono.just(request().imageUploaded(tooBig).build()), "current"))
                .expectError(InvalidInputException.class).verify(TIMEOUT);
        verify(inventories, never()).save(any(Inventory.class));
    }

    @Test
    void creatingAnInventoryWithAPhotoStoresIt() {
        when(inventories.count()).thenReturn(Mono.just(1L));
        when(inventories.findAll()).thenReturn(Flux.empty());
        when(inventories.existsByInventoryName("Bandages")).thenReturn(Mono.just(false));
        when(inventories.insert(any(Inventory.class))).thenAnswer(call -> Mono.just(call.getArgument(0)));

        StepVerifier.create(service.addInventory(Mono.just(request().imageUploaded(NEW_PHOTO).build())))
                .assertNext(response -> assertArrayEquals(NEW_PHOTO, response.getImageUploaded()))
                .expectComplete().verify(TIMEOUT);
    }

    private InventoryRequestDTO.InventoryRequestDTOBuilder request() {
        return InventoryRequestDTO.builder().inventoryType("Equipment").name(NAMES).description(DESCRIPTIONS);
    }
}
