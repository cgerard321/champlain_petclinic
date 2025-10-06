# File Service Usage Standards

Back to [Main page](../README.md)

<!-- TOC -->
* [Setup](#setup)
* [Get File](#get-file)
* [Add File](#add-file)
* [Update File](#update-file)
* [Delete File](#delete-file)
<!-- TOC -->

## Communication With Files Service
Direct communication with the Files Service is not permitted from the API Gateway or from any external client.

Do not add a FilesServiceClient or a FilesController to the api-gateway.

Any service that requires access to files must handle it within its own domain service and controller, rather than communicating with the Files Service directly.

For example, if the Customer Service needs to retrieve a customer’s image, it should parse as an argument includePhoto = true to the normal get endpoint for owners.

## Setup

These are the steps that needs to be followed to integrate your service with the **Files Service**.

### 1. Add the Files Service Client
You will first need to add the FilesServiceClient to your service. This file should be almost identical across all services that use the files service.
For an example of what the file should look like please take a look at the one from the customer-service.

### 2. Add Host and Port to application.yaml
You will need to add the host and port in the application.yaml of your service for the client to work.

```yaml
app:
  files-service:
    host: files-service
    port: 8000
```

### 3. Update Service Implement
You will need to add the following variable to your service implement

```java
private final FilesServiceClient filesServiceClient;
```

### 4. Add the File Details DTO
You will also need to create a new DTO in your service with the following structure:

```java
@Getter
@Setter
@AllArgsConstructor
public class FileDetails {

    private String fileId;
    private String fileName;
    private String fileType;
    private byte[] fileData;
}
```

### 5. Include FileDetails in the Models

Your Response and Request Model should be modified to include a FileDetails field.

Good Examples from Customer-Service:

```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OwnerResponseDTO {
    
    private String ownerId;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String province;
    private String telephone;
    private List<PetResponseDTO> pets;
    private FileDetails photo;
}
```

```java
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OwnerRequestDTO {

    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String province;
    private String telephone;
    private FileDetails photo;
}
```

### 6. Add FileId Field to the Entity

Your Entity should only store the id of the file that it wishes to access later.

Good Example from Customer-Service:
```java
@Data
@NoArgsConstructor
@Builder
@Getter
@AllArgsConstructor
public class Owner {

    @Id
    private String id;
    private String ownerId;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String province;
    private String telephone;
    private List<PetResponseDTO> pets;
    private String photoId;
}
```

## Get File

### 1. Update Service Controller to Support File Inclusion

Since files can be heavy and that in most cases won't be used, an endpoint that return the entire DTO with the file should be added to the controller.

This parameter must always be explicitly provided for clarity. It should be set to false by default and only set to true when the file data is actually required.

Good Example from Customer-Service's Controller:

```java
@GetMapping("/{ownerId}")
public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerByOwnerId(@PathVariable String ownerId, @RequestParam(required = true) boolean includePhoto) {
    return ownerService.getOwnerByOwnerId(ownerId, includePhoto)
            .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(ownerResponseDTO))
            .defaultIfEmpty(ResponseEntity.notFound().build());
}
```

### 2. Update Service Implement to getFile

The service should not map the fileDetails field and insteand should be manually done inside the service implement.

In the following example, we are using a default image so that if none is set it doesn't throw an error.

Good Example from Customer-Service's ServiceImplement:

```java
@Override
public Mono<OwnerResponseDTO> getOwnerByOwnerId(String ownerId, boolean includePhoto) {
    return ownerRepo.findOwnerByOwnerId(ownerId)
            .switchIfEmpty(Mono.error(new NotFoundException("Owner not found with id : " + ownerId)))
            .flatMap(owner -> {
                OwnerResponseDTO dto = EntityDTOUtil.toOwnerResponseDTO(owner);

                if (includePhoto && owner.getPhoto().getFileId() != null) {
                    filesServiceClient.getFileById(owner.getPhoto().getFileId()).map(fileDetails -> dto.setPhoto(fileDetails));
                } else {
                    filesServiceClient.getFileById(defaultPhotoId).map(fileDetails -> dto.setPhoto(fileDetails));
                }
                return Mono.just(dto);
            });
    }
```

### 3. Update API Gateway Response Model

You should also update your response model in the api-gateway to include the fileDetails field.
It should look identical to the one in your service. 

Do not create a duplicate DTO.
Instead, import and reuse the existing FileDetails class from the files package.

### 4. Update API Gateway Service Client to Pass File Parameter

Your Service Client may not currently support query parameters.
You’ll need to modify the GET request to include the includeFile parameter when calling the service.

Good Example from Customer Service Client:

```java
public Mono<OwnerResponseDTO> getOwner(final String ownerId, boolean includePhoto) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(customersServiceUrl + "/owners/" + ownerId);
    builder.queryParam("includePhoto", includePhoto);
        
    return webClientBuilder.build().get()
        .uri(builder.build().toUri())
        .retrieve()
        .bodyToMono(OwnerResponseDTO.class);
}
```

### 5. Update API Gateway Controller to Support File Inclusion

Add the includeFile query parameter to your API Gateway controller endpoint.
This allows clients to control whether file data should be included in the response, maintaining consistency with the service-level endpoint.

Good Example from Customer Api-gateway Controller:
```java
@IsUserSpecific(idToMatch = {"ownerId"}, bypassRoles = {Roles.ADMIN})
@GetMapping(value = "/{ownerId}")
public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerDetails(final @PathVariable String ownerId, @RequestParam(required = false) boolean includeImage) {
    return customersServiceClient.getOwner(ownerId, includeImage)
        .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(ownerResponseDTO))
        .defaultIfEmpty(ResponseEntity.notFound().build());
}
```

## Add File

There are two possible ways to associate a file with your entity, depending on your use case:
* If the file should be uploaded when creating the entity, follow [Add File on Creation](#update-service-implement-to-addfile)
* If the file should be uploaded after creating the entity, follow [Add File after Creation](#add-new-patch-endpoint)

### Update Service Implement to Support addFile

This approach is the simplest and only requires to update the addEntity of your service.

Good Example from Customer Service Implement:
```java
@Override
public Mono<Owner> addOwner(Mono<OwnerRequestDTO> ownerRequestDTO) {
    return ownerRequestDTO
            .flatMap(this::validateRequestDTO)
            .flatMap(ownerRequest -> {
                Owner owner = EntityDTOUtil.toOwner(ownerRequest);

                Mono<FileDetails> photoMono;

                if (ownerRequest.getPhoto() != null) {
                    photoMono = filesServiceClient.addFile(ownerRequest.getPhoto());
                } else
                    photoMono = filesServiceClient.getFileById(defaultPhotoId);

                return photoMono
                        .flatMap(photo -> {
                            owner.setPhotoId(photo.getFileId());
                            return ownerRepo.save(owner)
                                    .map(savedOwner -> {
                                        OwnerResponseDTO dto = EntityDTOUtil.toOwnerResponseDTO(savedOwner);
                                        dto.setPhoto(photo);
                                        return dto;
                                    });
                        });
            });
}
```

### Add New Patch Endpoint

This is the harder method and requires that a new endpoint be created in the service and in the api-gateway.
The Steps to add a new patch endpoint won't be explained here.

Here is the logic to get a FileId after adding it to the files-service.

```
filesServiceClient.addFile(ownerRequest.getPhoto()).map(FileDetails::getFileId);
```

## Update File

There are two possible ways to update the file associated with your entity, depending on your use case:
* If only the file is changing, follow [Add New Patch Endpoint](#add-new-patch-endpoint)
* If the file should be uploaded after creating the entity, follow [Update Service Implement to Support UpdateFile](#update-service-implement-to-support-Updatefile)

### Update Service Implement to Support UpdateFile

This approach is the simplest and only requires to update the updateEntity of your service.

Good Example from Customer Service Implement:

```java
@Override
public Mono<OwnerResponseDTO> updateOwner(Mono<OwnerRequestDTO> ownerRequestDTO, String ownerId) {

    return ownerRepo.findOwnerByOwnerId(ownerId)
            .flatMap(existingOwner -> ownerRequestDTO.map(requestDTO -> {
                filesServiceClient.updateFile(existingOwner.getPhotoId(), requestDTO.getPhoto());
                        
                existingOwner.setFirstName(requestDTO.getFirstName());
                existingOwner.setLastName(requestDTO.getLastName());
                existingOwner.setAddress(requestDTO.getAddress());
                existingOwner.setCity(requestDTO.getCity());
                existingOwner.setProvince(requestDTO.getProvince());
                existingOwner.setTelephone(requestDTO.getTelephone());
                return existingOwner;
            } ))
            .flatMap(ownerRepo::save)
            .map(EntityDTOUtil::toOwnerResponseDTO);
}
```
The id doesn't need to be saved again because it does not change.

## Delete File

Every File associated with an entity should always be deleted when that entity is deleted.

Good Example from Customer Service Implement:

```java
@Override
public Mono<OwnerResponseDTO> deleteOwnerByOwnerId(String ownerId) {
    return ownerRepo.findOwnerByOwnerId(ownerId)
            .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Course id not found: " + ownerId))))
            .flatMap(found -> {
                Mono<Void> deleteOwnerMono = ownerRepo.delete(found);

                Mono<Void> deletePhotoMono = Mono.empty();
                if (!found.getPhotoId().equals(defaultPhotoId)) {
                    deletePhotoMono = filesServiceClient.deleteFileById(found.getPhotoId());
                }

                return Mono.when(deleteOwnerMono, deletePhotoMono)
                        .thenReturn(found);
            })
            .map(EntityDTOUtil::toOwnerResponseDTO);
}
```

If the file needs to be deleted without the entire entity, follow [Add New Patch Endpoint](#add-new-patch-endpoint)
