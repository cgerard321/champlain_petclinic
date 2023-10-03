'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var method = $stateParams.method;
        var petId = $stateParams.petId || 0;
        var owner = "";
        var myDate = new Date();

        // Clear the form fields
        $ctrl.pet = {};

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.types = resp.data;
        });

        $http.get('api/gateway/pets/' + petId).then(function (resp) {
            self.pet = resp.data;
        }).catch(function (error) {
            console.error('Error loading pet details:', error);
        });

        $http.get('api/gateway/owners/' + ownerId).then(function (resp) {
            var ownerData = resp.data;
            owner = ownerData.firstName + " " + ownerData.lastName;
            self.pet = {
                owner: owner
            }

            self.checked = false;
        });

        // Function to submit the form
        self.submit = function () {
            var petType = {
                id: self.pet.type.id,
                name: self.pet.type.name
            }

            var data = {
                petId: self.pet.petId,
                name: self.pet.name,
                birthDate: self.pet.birthDate,
                ownerId: self.pet.ownerId,
                type: petType
            }

            var req;

            req = $http.put("api/gateway/" + "pets/" + petId, data);

            req.then(function () {
                $state.go('ownerDetails', { ownerId: ownerId });
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);
