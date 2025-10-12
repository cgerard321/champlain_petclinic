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
* [React Frontend Usage](#react-frontend-usage)
    * [Frontend Setup](#react-frontend-setup)
    * [Display Image](#react-display-image)
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


3. Use the `Rethrower` utility for error propagation.
    - Never build custom error messages for Files Service responses.
    - Always pass back the error received from the Files Service through the `Rethrower`.
    - This guarantees consistent error handling and uniform API responses across all services.


4. Files are owned and managed by the services that reference them.
    - Each service is responsible for creating, updating, and deleting its associated files via the Files Service.
    - When deleting an entity that has a file, always delete the associated file (unless it’s a shared or default file).
    - Never let orphaned files remain in the Files Service.


5. Entities should only store the File ID, not the file content.
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

#### 2. Add Rethrower
We are adding a rethrower because the errors you will get as Response Entity will already tell you what is wrong.
As such the rethrower handles the error received by the files Service Client and sends back a new one to the api-gateway with the original message and technically the same error code.

```java
@RequiredArgsConstructor
@Component
public class Rethrower {

    private static final Logger log = LoggerFactory.getLogger(Rethrower.class);
    private final ObjectMapper objectMapper;

    public Mono<? extends Throwable> rethrow(ClientResponse clientResponse, Function<Map, ? extends Throwable> exceptionProvider) {
        return clientResponse.createException().flatMap(n ->
        {
            try {
                final Map map =
                        objectMapper.readValue(n.getResponseBodyAsString(), Map.class);
                return Mono.error(exceptionProvider.apply(map));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                return Mono.error(e);
            }
        });
    }

}
```

#### 3. Handle the Files Service Client Errors
The Files Service can currently return 4 different types of error when a request is made, not found, bad request, unprocessable entity or internal server error.

This is an example of how errors are handled in the Files Service Client:
```
    .onStatus(HttpStatus.NOT_FOUND::equals, resp -> rethrower.rethrow(resp, ex -> new NotFoundException(ex.get("message").toString())))
    .onStatus(HttpStatus.UNPROCESSABLE_ENTITY::equals, resp -> rethrower.rethrow(resp, ex -> new InvalidInputException(ex.get("message").toString())))
    .onStatus(HttpStatus.BAD_REQUEST::equals, resp -> rethrower.rethrow(resp, ex -> new BadRequestException(ex.get("message").toString())))
    .onStatus(HttpStatus.INTERNAL_SERVER_ERROR::equals, resp -> rethrower.rethrow(resp, ex -> new RuntimeException(ex.get("message").toString())))
```

As you can see, we are using 1 exception class per error code. These exception class can be anything, but it is probably simpler if you name them the following:

- NotFoundException
- UnprocessableEntityException
- BadRequestException
- InternalServerException

These can be named anything as long as you handle them the right way in your global exception handler.

#### 4. Update Global Exception Handler

Each exception class added for handling the Files Service Client errors will need to be handle so that they return the right error code.

Here is the example to handle one, you need to handle them all.
```java
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public HttpErrorInfo handleBadRequestException(ServerHttpRequest request, Exception ex){
        return createHttpErrorInfo(BAD_REQUEST, request, ex);
    }
```

In this case, this error will always be thrown when a bad request is sent to the Files Service Client so we want the error being sent back to the gateway to also be 400 Bad Request.

#### 2. Add Host and Port to application.yaml
You will need to add the host and port in the application.yaml of your service for the client to work.

```yaml
app:
  files-service:
    host: files-service
    port: 8000
```

#### 3. Update Service Implement
You will need to add the following variable to your service implement

```java
private final FilesServiceClient filesServiceClient;
```

#### 4. Add the File Details DTO
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

### Get File

#### 1. Update Service Controller to Support File Inclusion

Since files can be heavy and won't always be used, we add a request parameter to say if we want or not the file.

We are not making a new endpoint in this case to get a file because the owner photo's will never be needed without the other details about him.
This should be the general rule, do not make a new endpoint as you would simply make it so that you have to make 2 https get calls to get all the information you need instead of one.

Good Example from Customer-Service's Controller:

```java
    @GetMapping("/{ownerId}")
    public Mono<ResponseEntity<OwnerResponseDTO>> getOwnerByOwnerId(@PathVariable String ownerId, @RequestParam(required = true) boolean includePhoto) {
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
                .switchIfEmpty(Mono.error(new NotFoundException("Owner not found with id : " + ownerId)))
                .flatMap(owner -> {
                    OwnerResponseDTO dto = EntityDTOUtil.toOwnerResponseDTO(owner);
                    String fileId = (includePhoto && owner.getPhoto().getFileId() != null)
                            ? owner.getPhoto().getFileId()
                            : defaultPhotoId;
                    return filesServiceClient.getFileById(fileId)
                            .map(fileDetails -> {
                                dto.setPhoto(fileDetails);
                                return dto;
                            });
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
* If the file should be uploaded after creating the entity, follow [Add File after Creation](#add-new-patch-endpoint)

#### Update Service Implement to Support addFile
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

#### Add New Patch Endpoint

This is the harder method and requires that a new endpoint be created in the service and in the api-gateway.
The Steps to add a new patch endpoint won't be explained here.

Here is the logic to get a FileId after adding it to the Files Service.

```
filesServiceClient.addFile(ownerRequest.getPhoto()).map(FileDetails::getFileId);
```

### Update File

There are two possible ways to update the file associated with your entity, depending on your use case:
* If only the file is changing, follow [Add New Patch Endpoint](#add-new-patch-endpoint)
* If the file should be uploaded after creating the entity, follow [Update Service Implement to Support UpdateFile](#update-service-implement-to-support-updatefile)

#### Update Service Implement to Support UpdateFile

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

### Delete File

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