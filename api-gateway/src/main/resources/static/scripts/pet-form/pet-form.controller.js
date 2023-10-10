'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', '$filter', '$q', function ($http, $state, $stateParams, $filter, $q) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var petId = $stateParams.petId || 0;

        // Clear the form fields
        self.pet = {}; // Changed $ctrl.pet to self.pet

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
            var owner = ownerData.firstName + " " + ownerData.lastName; // Added "var" before owner
            self.pet.owner = owner; // Changed self.pet = { owner: owner } to self.pet.owner = owner
        });

        $q.all([
            $http.get('api/gateway/pets/' + petId),
            $http.get('api/gateway/owners/' + ownerId)
        ]).then(function (responses) {
            var petData = responses[0].data;
            petData.birthDate = new Date(petData.birthDate);

            var ownerData = responses[1].data;
            petData.owner = ownerData.firstName + " " + ownerData.lastName;

            self.pet = petData;
            self.checked = false;
        });

        // Function to submit the form
        self.submit = function () {
            var data = {
                petId: self.pet.petId,
                name: self.pet.name,
                birthDate: new Date(self.pet.birthDate).toISOString(),
                ownerId: self.pet.ownerId,
                petTypeId: self.pet.petTypeId
            };

            var req;

            req = $http.put("api/gateway/pets/" + petId, data);

            req.then(function () {
                $state.go('ownerDetails', { ownerId: ownerId });
            }).catch(function (response) { // Changed "function (response)" to ".catch(function (response))"
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);
