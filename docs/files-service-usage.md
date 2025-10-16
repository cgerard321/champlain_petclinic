# File Service Usage Standards

Back to [Main page](../README.md)

<!-- TOC -->
* [General Rules](#general-rules)
* [Backend Usage](#backend-usage)
    * [Backend Setup](#backend-setup)
    * [Get File](#get-file)
    * [Add File](#add-file)
    * [Update File](#update-file)
    * [Delete File](#delete-file)
    * [Patch File](#patch-file)
* [React Frontend Usage](#react-frontend-usage)
    * [React Frontend Setup](#react-frontend-setup)
    * [React Display Image](#react-display-image)
<!-- TOC -->

## General Rules
1. Never communicate with the Files Service directly through the API Gateway.
    - All communication with the Files Service must go through your own domain service (e.g., `customer-service`, `vet-service`, etc.).
    - The API Gateway must never call the Files Service directly.
    - Do not register or reference a `FilesServiceClient` or `FilesController` inside the API Gateway.


2. Always use your service’s existing logic and endpoints when accessing files.
    - Do not create new “get file” endpoints just to retrieve files.
    - If your entity already has a standard “get by ID” endpoint, use it and include a query parameter (e.g., `includePhoto=true`) to control file inclusion.
    - This ensures a single API call returns both entity and file data when needed.


3. Files are owned and managed by the services that reference them.
    - Each service is responsible for creating, updating, and deleting its associated files via the Files Service.
    - When deleting an entity that has a file, always delete the associated file (unless it’s a shared or default file).
    - Never let orphaned files remain in the Files Service.


4. Entities should only store the File ID, not the file content.
    - File data should only exist in memory when being sent to or received from the Files Service.
    - Database entities must store only the file’s `fileId` field.
    - The actual file data (e.g., `byte[] fileData`) belongs exclusively in the Files Service.

## Backend Usage

### Backend Setup

These are the steps that need to be followed to integrate your service with the **Files Service**.
All of these are already implemented properly in the customer-service.

#### 1. Add the Files Service Client
You will first need to add the FilesServiceClient to your service. This file should be almost identical across all services that use the files service.
For an example of what the file should look like please take a look at the one from the customer-service.

#### 2. Handle the Files Service Client Errors
When handling errors returned by the Files Service, other services should not simply mirror the HTTP status code or expose the raw error message from the Files Service. Doing so may leak sensitive implementation details and create inconsistencies in how errors are reported to clients.

Instead, each service should interpret the error in context and return an HTTP status code that accurately reflects what happened from its own perspective.

For example, if the Files Service returns a 404 Not Found, the calling service should not return 404 to its client. From the calling service’s perspective, the issue lies in a downstream dependency, so a more appropriate response would be 424 Failed Dependency.

This approach ensures consistent, meaningful, and secure error reporting across all services.

#### 3. Add Host and Port to application.yaml
You will need to add the host and port in the application.yaml of your service for the client to work.

```yaml
app:
  files-service:
    host: files-service
    port: 8000
```

#### 4. Update Service Implement
You will need to add the following variable to your service implement

```java
private final FilesServiceClient filesServiceClient;
```

#### 7. Add the File Details DTO
You will also need to create a new DTO in your service with the following structure:

```java
@Getter
@Setter
@AllArgsConstructor
public class FileResponseDTO {

    private String fileId;
    private String fileName;
    private String fileType;
    private byte[] fileData;
}
```

```java
@Getter
@Setter
@AllArgsConstructor
public class FileRequestDTO {
    
    private String fileName;
    private String fileType;
    private byte[] fileData;
}
```

#### 5. Include FileDetails in the Models

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
    private FileResponseDTO photo;
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
    private FileRequestDTO photo;
}
```

#### 6. Add FileId Field to the Entity

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

#### 7. Update EntityMapper
You will also need to update your EntityModelMapper to handle the file fields correctly:

Request Model → Entity <br>
Do not map the file field automatically. The fileId should be set manually, as it is not part of the request model.

Entity → Response Model <br>
Do not map the file field automatically. The fileResponseDTO should be set manually, since the entity does not contain all the necessary file information.

### Get File

#### 1. Update Service Controller to Support File Inclusion

Since files can be heavy and won't always be used, we add a request parameter to say if we want or not the file.

We are not making a new endpoint in this case to get a file because the owner's photo will never be needed without the other details about him.
This should be the general rule, do not make a new endpoint as you would simply make it so that you have to make 2 HTTP GET calls to get all the information you need instead of one.
Using a request parameter with a default value to false makes it backwards compatible.

Good Example from Customer-Service's Controller:

```java
    @GetMapping("/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerByOwnerId(@PathVariable String ownerId, @RequestParam(required = false, defaultValue = "false") boolean includePhoto) {
        return ownerService.getOwnerByOwnerId(ownerId, includePhoto)
                .map(ownerResponseDTO -> ResponseEntity.status(HttpStatus.OK).body(ownerResponseDTO))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
```

#### 2. Update Service Implement to getFile

The service should not map the fileDetails field and instead should be manually done inside the service implement.

In the following example, we are using a default image so that if none is set it doesn't throw an error.

Good Example from Customer-Service's ServiceImplement:

```java
    @Override
    public Mono<OwnerResponseDTO> getOwnerByOwnerId(String ownerId, boolean includePhoto) {
        return ownerRepo.findOwnerByOwnerId(ownerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Owner not found with id: " + ownerId)))
                .flatMap(owner -> {
                    OwnerResponseDTO dto = EntityDTOUtil.toOwnerResponseDTO(owner);
    
                    if (includePhoto && owner.getPhotoId() != null) {
                        return filesServiceClient.getFileById(owner.getPhotoId())
                                .map(fileDetails -> {
                                    dto.setPhoto(fileDetails);
                                    return dto;
                                })
                                .onErrorReturn(dto);
                    } else {
                        dto.setPhoto(null);
                        return Mono.just(dto);
                    }
                });
    }
```

#### 3. Update API Gateway Response Model

You should also update your response model in the api-gateway to include the fileDetails field.
It should look identical to the one in your service.

Do not create a duplicate DTO.
Instead, import and reuse the existing FileDetails class from the files package.

#### 4. Update API Gateway Service Client to Pass File Parameter

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

#### 5. Update API Gateway Controller to Support File Inclusion

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

### Add File

There are two possible ways to associate a file with your entity, depending on your use case:
* If the file should be uploaded when creating the entity, follow [Add File on Creation](#update-service-implement-to-addfile)
* If the file should be uploaded after creating the entity, follow [Add File after Creation](#patch-file)

#### Update Service Implement to Support addFile
This approach is the simplest and only requires to update the addEntity of your service.

Good Example from Customer Service Implement:
```java
    @Override
    public Mono<OwnerResponseDTO> addOwner(Mono<OwnerRequestDTO> ownerRequestDTO) {
        return ownerRequestDTO
                .flatMap(this::validateRequestDTO)
                .flatMap(ownerRequest -> {
                    Owner owner = EntityDTOUtil.toOwner(ownerRequest);
                    Mono<FileDetails> photoMono;
    
                    if (ownerRequest.getPhoto() != null) {
                        photoMono = filesServiceClient.addFile(ownerRequest.getPhoto());
                    } else {
                        photoMono = Mono.empty();
                    }
    
                    return photoMono
                            .defaultIfEmpty(null)
                            .flatMap(photo -> {
                                if (photo != null) {
                                    owner.setPhotoId(photo.getFileId());
                                } else {
                                    owner.setPhotoId(null);
                                }
    
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

### Patch File

This is the harder method, but the most efficient. It requires that a new endpoint be created in the service and in the api-gateway.
Here are the steps for adding the endpoint to your service, to add it to the api-gateway, you can just do the same things as other endpoints.

#### 1. Add Service Implement Patch
When using a PATCH endpoint, you need to make that you update only if the entity already has a fileId.
Do not use addFile when a file already exists because it will not remove the old file.

Good Example from Customer Service Implement:

```java
    @Override
    public Mono<OwnerResponseDTO> updateOwnerPhoto(String ownerId, FileRequestDTO photo) {
        return ownerRepo.findOwnerByOwnerId(ownerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Owner not found with id: " + ownerId)))
                .flatMap(existingOwner -> {
                    Mono<FileResponseDTO> fileOperation;

                    if (existingOwner.getPhotoId() != null && !existingOwner.getPhotoId().isEmpty()) {
                        fileOperation = filesServiceClient.updateFile(existingOwner.getPhotoId(), photo)
                                .onErrorResume(e -> {
                                    log.warn("Photo file {} not found or error updating, creating new file instead: {}", 
                                            existingOwner.getPhotoId(), e.getMessage());
                                    return filesServiceClient.addFile(photo);
                                });
                    } else {
                        fileOperation = filesServiceClient.addFile(photo);
                    }

                    return fileOperation
                            .flatMap(fileResp -> {
                                existingOwner.setPhotoId(fileResp.getFileId());
                                return ownerRepo.save(existingOwner)
                                        .map(savedOwner -> {
                                            OwnerResponseDTO dto = EntityDTOUtil.toOwnerResponseDTO(savedOwner);
                                            dto.setPhoto(fileResp);
                                            return dto;
                                        });
                            });
                });
    }
```

#### 2. Add Controller Patch 
The PATCH endpoint doesn't need the full entity dto, it only needs the FileDTO.

Good Example from Customer Service Implement:

```java
    @PatchMapping("/{ownerId}/photo")
    public Mono<ResponseEntity<OwnerResponseDTO>> updateOwnerPhoto(@PathVariable String ownerId, @RequestBody Mono<FileRequestDTO> photoMono) {
        return photoMono
                .flatMap(photo -> ownerService.updateOwnerPhoto(ownerId, photo))
                .map(updatedOwner -> ResponseEntity.ok().body(updatedOwner))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
```

### Update File

There are two possible ways to update the file associated with your entity, depending on your use case:

The best solution is to use a patch endpoint instead of a put endpoint when updating, [Add New Patch Endpoint](#patch-file). This makes it so that only when the file has been changed will we send an update request to the Files Service.

You can also use the put method, but it will be more costly since it send an update request to the Files Service everytime the entity is updated, [Update Service Implement to Support UpdateFile](#update-service-implement-to-support-updatefile).

#### Update Service Implement to Support UpdateFile

This approach is the simplest and only requires to update the updateEntity of your service.

Good Example from Customer Service Implement:

```java
    @Override
    public Mono<OwnerResponseDTO> updateOwner(Mono<OwnerRequestDTO> ownerRequestDTO, String ownerId) {
        return ownerRepo.findOwnerByOwnerId(ownerId)
                .switchIfEmpty(Mono.error(new NotFoundException("Owner not found with id: " + ownerId)))
                .flatMap(existingOwner ->
                        ownerRequestDTO.flatMap(requestDTO -> {
                            Mono<String> fileIdMono;
    
                            if (existingOwner.getPhotoId() != null && requestDTO.getPhoto() != null) {
                                fileIdMono = filesServiceClient.updateFile(existingOwner.getPhotoId(), requestDTO.getPhoto()).thenReturn(existingOwner.getPhotoId());
                            } else if (requestDTO.getPhoto() != null) {
                                fileIdMono = filesServiceClient.addFile(requestDTO.getPhoto()).map(FileResponseDTO::getFileId);
                            } else if (existingOwner.getPhotoId() != null) {
                                fileIdMono = filesServiceClient.deleteFile(existingOwner.getPhotoId()).thenReturn(null);
                            } else {
                                fileIdMono = Mono.justOrEmpty(existingOwner.getPhotoId());
                            }
    
                            return fileIdMono
                                    .defaultIfEmpty(null)
                                    .map(fileId -> {
                                        existingOwner.setFirstName(requestDTO.getFirstName());
                                        existingOwner.setLastName(requestDTO.getLastName());
                                        existingOwner.setAddress(requestDTO.getAddress());
                                        existingOwner.setCity(requestDTO.getCity());
                                        existingOwner.setProvince(requestDTO.getProvince());
                                        existingOwner.setTelephone(requestDTO.getTelephone());
                                        existingOwner.setPhotoId(fileId);
                                        return existingOwner;
                                    });
                        })
                )
                .flatMap(ownerRepo::save)
                .map(EntityDTOUtil::toOwnerResponseDTO);
    }
```

The downside is that even if the photo is not changed it will still update in the files Service, a patch approach would be more optimised.

### Delete File

Every File associated with an entity should always be deleted when that entity is deleted.

Good Example from Customer Service Implement:

```java
    @Override
    public Mono<OwnerResponseDTO> deleteOwnerByOwnerId(String ownerId) {
        return ownerRepo.findOwnerByOwnerId(ownerId)
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("OwnerId not found: " + ownerId))))
                .flatMap(found -> {
                    Mono<Void> deletePhotoMono = Mono.justOrEmpty(found.getPhotoId())
                            .flatMap(filesServiceClient::deleteFileById);
                    Mono<Void> deleteOwnerMono = ownerRepo.delete(found);
    
                    return Mono.when(deleteOwnerMono, deletePhotoMono)
                            .thenReturn(found);
                })
                .map(EntityDTOUtil::toOwnerResponseDTO);
    }
```

If the file needs to be deleted without the entire entity, follow [Add New Patch Endpoint](#patch-file)

## React Frontend Usage

### React Frontend Setup

The Response Model of your entity on the frontend should be modified to include the FileDetailsDTO.
Make sure that you are using the shared component, do not make a new one.

```ts
import {FileDetails} from "@/shared/models/FileDetails.ts"

export interface OwnerResponseModel {
    ownerId: string;
    firstName: string;
    lastName: string;
    address: string;
    city: string;
    province: string;
    telephone: string;
    pets: PetResponseModel[];
    photo: FileDetails;
}
```

### React Display Image

```html
<img
        src={`data:${fileDetails.fileType};base64,${fileDetails.fileData}`}
        alt={`${fileDetails.fileName}`}
/>
```