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
                isActive : "true",
                weight: self.pet.weight
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
            // Generate a random hexadecimal string of length 32
            var randomHex = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = Math.random() * 16 | 0,
                    v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });

            return randomHex;
        }

// Example usage:
        var randomUUID = generateUUID();
        console.log(randomUUID);


    }]);

