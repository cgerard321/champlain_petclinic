package com.petclinic.auth.presentationlayer.Signup;

import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserRepo;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RequestMapping("/users")
@RestController
@Timed("petclinic.user")
@Slf4j
class SignupResource {

        //private static final Logger log = LoggerFactory.getLogger(OwnerResource.class);

        private final OwnerRepository ownerRepository;

        OwnerResource(OwnerRepository ownerRepository) {
            this.ownerRepository = ownerRepository;
        }

        /**
         * Create Owner
         */
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public Owner createOwner(@Valid @RequestBody Owner owner) {
            return ownerRepository.save(owner);
        }

        /**
         * Read single Owner
         */
        @GetMapping(value = "/{ownerId}")
        public Optional<Owner> findOwner(@PathVariable("ownerId") int ownerId) {
            return ownerRepository.findById(ownerId);
        }

        /**
         * Read List of Owners
         */
        @GetMapping
        public List<Owner> findAll() {
            return ownerRepository.findAll();
        }

        /**
         * Update Owner
         */
        @PutMapping(value = "/{ownerId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void updateOwner(@PathVariable("ownerId") int ownerId, @Valid @RequestBody Owner ownerRequest) {
            final Optional<Owner> owner = ownerRepository.findById(ownerId);

            final Owner ownerModel = owner.orElseThrow(() -> new ResourceNotFoundException("Owner "+ownerId+" not found"));
            // This is done by hand for simplicity purpose. In a real life use-case we should consider using MapStruct.
            ownerModel.setFirstName(ownerRequest.getFirstName());
            ownerModel.setLastName(ownerRequest.getLastName());
            ownerModel.setCity(ownerRequest.getCity());
            ownerModel.setAddress(ownerRequest.getAddress());
            ownerModel.setTelephone(ownerRequest.getTelephone());
            log.info("Saving owner {}", ownerModel);
            ownerRepository.save(ownerModel);
        }
    }


}
