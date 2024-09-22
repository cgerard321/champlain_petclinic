package com.petclinic.inventoryservice.businesslayer;

import com.petclinic.inventoryservice.datalayer.Inventory.InventoryNameRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryRepository;
import com.petclinic.inventoryservice.datalayer.Inventory.InventoryTypeRepository;
import com.petclinic.inventoryservice.datalayer.Supply.Supply;
import com.petclinic.inventoryservice.datalayer.Supply.SupplyRepository;
import com.petclinic.inventoryservice.datalayer.Supply.Status;
import com.petclinic.inventoryservice.presentationlayer.*;
import com.petclinic.inventoryservice.utils.EntityDTOUtil;
import com.petclinic.inventoryservice.utils.exceptions.InvalidInputException;
import com.petclinic.inventoryservice.utils.exceptions.InventoryNotFoundException;
import com.petclinic.inventoryservice.utils.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SupplyInventoryServiceImpl implements SupplyInventoryService {

    private final InventoryRepository inventoryRepository;
    private final SupplyRepository supplyRepository;
    private final InventoryTypeRepository inventoryTypeRepository;
    private final InventoryNameRepository inventoryNameRepository;


    @Override
    public Mono<SupplyResponseDTO> addSupplyToInventory(Mono<SupplyRequestDTO> supplyRequestDTOMono, String inventoryId) {
        return supplyRequestDTOMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(requestDTO -> inventoryRepository.findInventoryByInventoryId(inventoryId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                        .flatMap(inventory -> {
                            if (requestDTO.getSupplyName() == null || requestDTO.getSupplyPrice() == null || requestDTO.getSupplyQuantity() == null || requestDTO.getSupplySalePrice() == null) {
                                return Mono.error(new InvalidInputException("Supply must have an inventory id, supply name, supply price, and supply quantity."));
                            } else if (requestDTO.getSupplyPrice() < 0 || requestDTO.getSupplyQuantity() < 0 || requestDTO.getSupplySalePrice() < 0) {
                                return Mono.error(new InvalidInputException("Supply price and quantity must be greater than 0."));
                            } else {
                                Supply supply = EntityDTOUtil.toSupplyEntity(requestDTO);
                                supply.setInventoryId(inventoryId);
                                supply.setSupplyId(EntityDTOUtil.generateUUID());
                                // Set Status based on the supply quantity
                                if (supply.getSupplyQuantity() == 0) {
                                    supply.setStatus(Status.OUT_OF_STOCK);
                                } else if (supply.getSupplyQuantity() < 20) {
                                    supply.setStatus(Status.RE_ORDER);
                                } else {
                                    supply.setStatus(Status.AVAILABLE);
                                }
                                return supplyRepository.save(supply)
                                        .map(EntityDTOUtil::toSupplyResponseDTO);
                            }
                        }))
                .switchIfEmpty(Mono.error(new InvalidInputException("Unable to save supply to the repository, an error occurred.")));
    }

    @Override

    public Mono<InventoryResponseDTO> addInventory(Mono<InventoryRequestDTO> inventoryRequestDTO) {
        return inventoryRequestDTO
                .map(EntityDTOUtil::toInventoryEntity)
                .doOnNext(e -> {
                    if (e.getInventoryType() == null) {
                        throw new InvalidInputException("Invalid input data: inventory type cannot be blank.");
                    }
                    e.setInventoryId(EntityDTOUtil.generateUUID());
                })
                .flatMap(inventoryRepository::insert)
                .map(EntityDTOUtil::toInventoryResponseDTO);

    }


    @Override
    public Mono<InventoryResponseDTO> updateInventory(Mono<InventoryRequestDTO> inventoryRequestDTO, String inventoryId) {

        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .flatMap(existingInventory -> inventoryRequestDTO.map(requestDTO -> {

                    existingInventory.setInventoryName(requestDTO.getInventoryName());



                    existingInventory.setInventoryType(requestDTO.getInventoryType());
                    existingInventory.setInventoryDescription(requestDTO.getInventoryDescription());
                    return existingInventory;

                }))
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                .flatMap(inventoryRepository::save)
                .map(EntityDTOUtil::toInventoryResponseDTO);

    }

    @Override
    public Mono<SupplyResponseDTO> updateSupplyInInventory(Mono<SupplyRequestDTO> supplyRequestDTOMono, String inventoryId, String supplyId) {

        return supplyRequestDTOMono
                .publishOn(Schedulers.boundedElastic())
                .flatMap(requestDTO -> inventoryRepository.findInventoryByInventoryId(inventoryId)
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with id: " + inventoryId)))
                        .flatMap(inventory -> {
                            if (requestDTO.getSupplyName() == null || requestDTO.getSupplyPrice() == null || requestDTO.getSupplyQuantity() == null || requestDTO.getSupplySalePrice() == null) {
                                return Mono.error(new InvalidInputException("Supply must have an inventory id, supply name, supply price, and supply quantity."));
                            } else if (requestDTO.getSupplyPrice() < 0 || requestDTO.getSupplyQuantity() < 0 || requestDTO.getSupplySalePrice() < 0) {
                                return Mono.error(new InvalidInputException("Supply price and quantity must be greater than 0."));
                            } else {
                                return supplyRepository.findSupplyBySupplyId(supplyId)
                                        .flatMap(existingSupply -> {
                                            existingSupply.setSupplyName(requestDTO.getSupplyName());
                                            existingSupply.setSupplyDescription(requestDTO.getSupplyDescription());
                                            existingSupply.setSupplyPrice(requestDTO.getSupplyPrice());
                                            existingSupply.setSupplyQuantity(requestDTO.getSupplyQuantity());
                                            existingSupply.setSupplySalePrice(requestDTO.getSupplySalePrice());

                                            // Set Status based on the supply quantity
                                            if (existingSupply.getSupplyQuantity() == 0) {
                                                existingSupply.setStatus(Status.OUT_OF_STOCK);
                                            } else if (existingSupply.getSupplyQuantity() < 20) {
                                                existingSupply.setStatus(Status.RE_ORDER);
                                            } else {
                                                existingSupply.setStatus(Status.AVAILABLE);
                                            }

                                            return supplyRepository.save(existingSupply)
                                                    .map(EntityDTOUtil::toSupplyResponseDTO);
                                        })
                                        .switchIfEmpty(Mono.error(new NotFoundException("Supply not found with id: " + supplyId)));
                            }
                        }))
                .switchIfEmpty(Mono.error(new InvalidInputException("Unable to update supply in the repository, an error occurred.")));
    }








    @Override
    public Mono<Void> deleteSupplyInInventory(String inventoryId, String supplyId) {
        return inventoryRepository.existsByInventoryId(inventoryId)
                .flatMap(invExist -> {
                    if (!invExist) {
                        return Mono.error(new NotFoundException("Inventory not found, make sure it exists, inventoryId: " + inventoryId));
                    } else {
                        return supplyRepository.existsBySupplyId(supplyId)
                                .flatMap(prodExist -> {
                                    if (!prodExist) {
                                        return Mono.error(new NotFoundException("Supply not found, make sure it exists, supplyId: " + supplyId));
                                    } else {
                                        return supplyRepository.deleteBySupplyId(supplyId);
                                    }
                                });
                    }
                });


    }

    @Override
    public Flux<SupplyResponseDTO> getSuppliesInInventoryByInventoryIdAndSuppliesField(String inventoryId, String supplyName, Double supplyPrice, Integer supplyQuantity, Double supplySalePrice) {

        if (supplyName != null && supplyPrice != null && supplyQuantity != null && supplySalePrice != null){
            return  supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyNameAndSupplyPriceAndSupplyQuantityAndSupplySalePrice(inventoryId,
                            supplyName, supplyPrice, supplyQuantity, supplySalePrice)
                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyName: " + supplyName + "\nOr SupplyPrice: " + supplyPrice + "\nOr SupplyQuantity: " + supplyQuantity + "\nOr SupplySalePrice: " + supplySalePrice)));
        }
        if (supplyPrice != null && supplyQuantity != null){
            return supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyPriceAndSupplyQuantity(inventoryId, supplyPrice, supplyQuantity)
                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyPrice: " + supplyPrice + "\nOr SupplyQuantity: " + supplyQuantity)));
        }
        if (supplyPrice != null){
            return supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyPrice(inventoryId, supplyPrice)
                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyPrice: " + supplyPrice)));
        }
        if (supplyQuantity != null){
            return supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyQuantity(inventoryId, supplyQuantity )

                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyQuantity: " + supplyQuantity)));
        }
        if (supplySalePrice != null){
            return supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplySalePrice(inventoryId, supplySalePrice)

                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplySalePrice: " + supplySalePrice)));
        }


        if (supplyName != null) {
            String escapedInventoryName = Pattern.quote(supplyName);

            String regexPattern = "(?i)^" + escapedInventoryName + ".*";

            if (supplyName.length() == 1) {
                return supplyRepository
                        .findAllSuppliesByInventoryIdAndSupplyNameRegex(inventoryId, regexPattern)
                        .map(EntityDTOUtil::toSupplyResponseDTO)
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                                "\nOr SupplyName: " + supplyName)));
            } else {
                return supplyRepository
                        .findAllSuppliesByInventoryIdAndSupplyNameRegex(inventoryId,regexPattern)
                        .map(EntityDTOUtil::toSupplyResponseDTO)
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Name starting with or matching: " + supplyName)));
            }

        }
        return supplyRepository
                .findAllSuppliesByInventoryId(inventoryId)
                .map(EntityDTOUtil::toSupplyResponseDTO);
        //where the 404 not found issue lies if we switchIfEmpty

    }

    @Override
    public Flux<SupplyResponseDTO> getSuppliesInInventoryByInventoryIdAndSuppliesFieldsPagination(String inventoryId, String supplyName, Double supplyPrice, Integer supplyQuantity, Pageable pageable) {

        if (supplyName != null && supplyPrice != null && supplyQuantity != null){
            return  supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyNameAndSupplyPriceAndSupplyQuantity(inventoryId,
                            supplyName, supplyPrice, supplyQuantity)
                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyName: " + supplyName + "\nOr SupplyPrice: " + supplyPrice + "\nOr SupplyQuantity: " + supplyQuantity)));
        }
        if (supplyPrice != null && supplyQuantity != null){
            return supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyPriceAndSupplyQuantity(inventoryId, supplyPrice, supplyQuantity)
                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyPrice: " + supplyPrice + "\nOr SupplyQuantity: " + supplyQuantity)));
        }
        if (supplyPrice != null){
            return supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyPrice(inventoryId, supplyPrice)
                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyPrice: " + supplyPrice)));
        }
        if (supplyQuantity != null){
            return supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyQuantity(inventoryId, supplyQuantity)
                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyQuantity: " + supplyQuantity)));
        }
        if (supplyName != null){
            return supplyRepository
                    .findAllSuppliesByInventoryIdAndSupplyName(inventoryId, supplyName)
                    .map(EntityDTOUtil::toSupplyResponseDTO)
                    .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                    .take(pageable.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with InventoryId: " + inventoryId +
                            "\nOr SupplyName: " + supplyName)));
        }


        return supplyRepository
                .findAllSuppliesByInventoryId(inventoryId)
                .map(EntityDTOUtil::toSupplyResponseDTO)
                .skip((long) pageable.getPageNumber() * pageable.getPageSize())
                .take(pageable.getPageSize());
        //where the 404 not found issue lies if we switchIfEmpty

    }




    @Override
    public Mono<InventoryResponseDTO> getInventoryById(String inventoryId){
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("No inventory with this id was found" + inventoryId)))
                .map(EntityDTOUtil::toInventoryResponseDTO);

    }
    @Override
    public Mono<Void> deleteInventoryByInventoryId(String inventoryId) {
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new NotFoundException("The Inventory with the inventoryId: " + inventoryId + " is invalid. Please enter a valid inventory id.")))
                .flatMapMany(inventoryRepository::delete)
                .then();
    }

    @Override
    public Flux<InventoryResponseDTO>searchInventories(Pageable page, String inventoryName, String inventoryType, String inventoryDescription) {

        if (inventoryName != null && inventoryType != null && inventoryDescription != null){
            return inventoryRepository
                    .findAllByInventoryNameAndInventoryTypeAndInventoryDescription(inventoryName, inventoryType, inventoryDescription)
                    .map(EntityDTOUtil::toInventoryResponseDTO)
                    .skip(page.getPageNumber() * page.getPageSize())
                    .take(page.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Name: " + inventoryName +
                            ", Type: " + inventoryType + ", Description: " + inventoryDescription)));
        }

        if (inventoryType != null && inventoryDescription != null){
            return inventoryRepository
                    .findAllByInventoryTypeAndInventoryDescription(inventoryType, inventoryDescription)
                    .map(EntityDTOUtil::toInventoryResponseDTO)
                    .skip(page.getPageNumber() * page.getPageSize())
                    .take(page.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Type: " + inventoryType +
                            " and Description: " + inventoryDescription)));
        }

        if (inventoryName != null){
            String escapedInventoryName = Pattern.quote(inventoryName); // escape any special characters

            // Regex pattern for partial or full name match
            String regexPattern = "(?i)^" + escapedInventoryName + ".*";

            // If only one character is provided, match all that starts with that character
            if (inventoryName.length() == 1) {
                return inventoryRepository
                        .findByInventoryNameRegex(regexPattern)
                        .map(EntityDTOUtil::toInventoryResponseDTO)
                        .skip(page.getPageNumber() * page.getPageSize())
                        .take(page.getPageSize())
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found starting with: " + inventoryName)));
            }
            // For any other input, match starting characters or the exact name
            else {
                return inventoryRepository
                        .findByInventoryNameRegex(regexPattern)
                        .map(EntityDTOUtil::toInventoryResponseDTO)
                        .skip(page.getPageNumber() * page.getPageSize())
                        .take(page.getPageSize())
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Name starting with or matching: " + inventoryName)));
            }
        }


        if (inventoryType != null){
            return inventoryRepository
                    .findAllByInventoryType(inventoryType)
                    .map(EntityDTOUtil::toInventoryResponseDTO)
                    .skip(page.getPageNumber() * page.getPageSize())
                    .take(page.getPageSize())
                    .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Type: " + inventoryType)));
        }

        if (inventoryDescription != null) {

            String escapedInventoryDescription = Pattern.quote(inventoryDescription);
            String regexPattern = "(?i)^" + escapedInventoryDescription + ".*";

            if (inventoryDescription.length() == 1) {
                return inventoryRepository
                        .findByInventoryDescriptionRegex(regexPattern)
                        .map(EntityDTOUtil::toInventoryResponseDTO)
                        .skip(page.getPageNumber() * page.getPageSize())
                        .take(page.getPageSize())
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Description: " + inventoryDescription)));
            } else {
                return inventoryRepository
                        .findByInventoryDescriptionRegex(regexPattern)
                        .map(EntityDTOUtil::toInventoryResponseDTO)
                        .skip(page.getPageNumber() * page.getPageSize())
                        .take(page.getPageSize())
                        .switchIfEmpty(Mono.error(new NotFoundException("Inventory not found with Name starting with or matching: " + inventoryName)));
            }
        }

        // Default - fetch all if no criteria provided.
        return inventoryRepository
                .findAll()
                .skip(page.getPageNumber() * page.getPageSize())
                .take(page.getPageSize())
                .map(EntityDTOUtil::toInventoryResponseDTO);
    }


    @Override
    public Mono<SupplyResponseDTO> getSupplyBySupplyIdInInventory(String inventoryId, String supplyId) {
        return supplyRepository
                .findSupplyByInventoryIdAndSupplyId(inventoryId, supplyId)
                .map(EntityDTOUtil::toSupplyResponseDTO)
                .switchIfEmpty(Mono.error(new NotFoundException("Inventory id:" + inventoryId + "and supply:" + supplyId + "are not found")));
    }

    @Override
    public Mono<InventoryResponseDTO> addSupplyToInventoryByInventoryName(String inventoryName, Mono<SupplyRequestDTO> supplyRequestDTOMono) {

        return supplyRequestDTOMono
                .flatMap(supplyRequestDTO ->
                        inventoryRepository.findByInventoryName(inventoryName)
                                .switchIfEmpty(Mono.error(new InventoryNotFoundException("No inventory found for name: " + inventoryName)))
                                .flatMap(inventory -> {
                                    Supply supply = new Supply(
                                            UUID.randomUUID().toString(),
                                            inventory.getInventoryId(),
                                            supplyRequestDTO.getSupplyName(),
                                            supplyRequestDTO.getSupplyDescription(),
                                            supplyRequestDTO.getSupplyQuantity(),
                                            supplyRequestDTO.getSupplyPrice(),
                                            supplyRequestDTO.getSupplySalePrice(),
                                            Status.AVAILABLE
                                    );

                                    inventory.addSupply(supply);
                                    return inventoryRepository.save(inventory);
                                })
                                .map(updatedInventory -> {
                                    List<SupplyResponseDTO> supplyResponseDTOs = updatedInventory.getSupplies().stream()
                                            .map(supply -> new SupplyResponseDTO(
                                                    supply.getSupplyId(),
                                                    supply.getInventoryId(),
                                                    supply.getSupplyName(),
                                                    supply.getSupplyDescription(),
                                                    supply.getSupplyPrice(),
                                                    supply.getSupplyQuantity(),
                                                    supply.getSupplySalePrice(),
                                                    supply.getStatus()
                                            ))
                                            .collect(Collectors.toList());

                                    return new InventoryResponseDTO(
                                            updatedInventory.getInventoryId(),
                                            updatedInventory.getInventoryName(),
                                            updatedInventory.getInventoryType(),
                                            updatedInventory.getInventoryDescription(),
                                            supplyResponseDTOs
                                    );
                                })
                );
    }

    //delete all supplies and delete all inventory
    @Override
    public Mono<Void> deleteAllSupplyInventory (String inventoryId){
        return inventoryRepository.findInventoryByInventoryId(inventoryId)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid Inventory Id")))
                .flatMapMany(inv -> supplyRepository.deleteByInventoryId(inventoryId))
                .then();
    }

    @Override
    public Mono<Void> deleteAllInventory () {
        return inventoryRepository.deleteAll();

    }

    @Override
    public Mono<InventoryTypeResponseDTO> addInventoryType(Mono<InventoryTypeRequestDTO> inventoryTypeRequestDTO) {
        return inventoryTypeRequestDTO
                .map(EntityDTOUtil::toInventoryTypeEntity)
                .doOnNext(e -> {
                    e.setTypeId(EntityDTOUtil.generateUUID());
                })
                .flatMap(inventoryTypeRepository::insert)
                .map(EntityDTOUtil::toInventoryTypeResponseDTO);
    }

    @Override
    public Flux<InventoryTypeResponseDTO> getAllInventoryTypes() {
        return inventoryTypeRepository.findAll()
                .map(EntityDTOUtil::toInventoryTypeResponseDTO);
    }

    @Override
    public Flux<InventoryNameResponseDTO> getAllInventoryNames() {
        return inventoryNameRepository.findAll()
                .map(EntityDTOUtil::toInventoryNameResponseDTO);
    }
}

