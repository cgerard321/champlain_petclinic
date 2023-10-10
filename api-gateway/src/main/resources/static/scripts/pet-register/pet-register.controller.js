'use strict';

angular.module('petRegister')
    .controller('PetRegisterController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        console.log("properly running on load")

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.types = resp.data;
        });


        // Function to submit the form
        self.submitPetForm = function () {
            console.log("function calls")
            var petType = {
                id: self.pet.type.id,
                name: self.pet.type.name
            }

            var data = {
                ownerId: ownerId,
                petId: randomUUID,
                name: self.pet.name,
                birthDate: self.pet.birthDate,
                type: petType.id,
                isActive : "true"
            }


           $http.post("api/gateway/" + "owners/" + ownerId + "/pets", data).then(function (){
                console.log("before if")
                $state.go('ownerDetails', {ownerId: ownerId});
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };

        function generateUUID() {
            // Generate a random hexadecimal string of length 12
            var randomHex = 'xxxxxxxxxxxx'.replace(/x/g, function () {
                return (Math.random() * 16 | 0).toString(16);
            });

            // Format the UUID
            var uuid = [
                randomHex.substr(0, 8),
                randomHex.substr(8, 4),
                '4' + randomHex.substr(13, 3), // Set the version to 4 (random)
                '89ab'[Math.floor(Math.random() * 4)] + randomHex.substr(17, 3), // Set the variant
                randomHex.substr(20, 12)
            ].join('-');

            return uuid;
        }

// Example usage:
        var randomUUID = generateUUID();
        console.log(randomUUID);


    }]);

