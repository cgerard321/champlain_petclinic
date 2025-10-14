    package com.petclinic.bffapigateway.dtos.CustomerDTOs;

    import com.petclinic.bffapigateway.dtos.Files.FileDetails;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class OwnerRequestDTO {

        private String ownerId;
        private String firstName;
        private String lastName;
        private String address;
        private String city;
        private String province;
        private String telephone;
        //private List<PetResponseDTO> pets;
        private FileDetails photo;
    }
