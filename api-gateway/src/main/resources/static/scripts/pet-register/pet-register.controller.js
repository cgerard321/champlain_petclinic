'use strict';

angular.module('petRegister')
    .controller('PetRegisterController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var method = $stateParams.method;
        var petId = $stateParams.petId || 0;
        var owner = "";
        var myDate = new Date();

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.types = resp.data;
        });


        // Function to submit the form
        self.submitPetForm = function () {
            var petType = {
                id: self.pet.type.id,
                name: self.pet.type.name
            }

            var data = {
                ownerId: ownerId,
                petId: "12345-12345-12345",
                name: self.pet.name,
                birthDate: self.pet.birthDate,
                type: petType.id,
                isActive : "true"
            }


            var req;


            req = $http.post("api/gateway/" + "owners/" + ownerId + "/pets", data);

            req.then(function () {
                $state.go('ownerDetails', {ownerId: ownerId});
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });

        };

    }]);

