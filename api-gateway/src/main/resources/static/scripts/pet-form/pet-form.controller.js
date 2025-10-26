'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', '$filter', '$q', function ($http, $state, $stateParams, $filter, $q) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var petId = $stateParams.petId || 0;

        // Helper to get pet type name
        self.getPetTypeName = function (petTypeId) {
            switch (petTypeId) {
                case '4283c9b8-4ffd-4866-a5ed-287117c60a40': return 'Cat';
                case '1233c9b8-4ffd-4866-4h36-287117c60a35': return 'Dog';
                case '9783c9b8-4ffd-4866-a5ed-287117c60a10': return 'Lizard';
                case '9133c9b8-4ffd-4866-a5ed-287117c60a19': return 'Snake';
                case '2093c9b8-4ffd-4866-a5ed-287117c60a11': return 'Bird';
                case '1103c9b8-4ffd-4866-a5ed-287117c60a89': return 'Hamster';
                case '9993c9b8-4ffd-4866-a5ed-287117c60a99': return 'Others';
                default: return 'Unknown';
            }
        };

        // Initialize
        self.pet = {};
        self.showModal = false;

        // Load types
        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.types = resp.data;
        });

        // Load pet & owner info
        $q.all([
            $http.get('api/gateway/pets/' + petId),
            $http.get('api/gateway/owners/' + ownerId)
        ]).then(function (responses) {
            var petData = responses[0].data;
            petData.birthDate = new Date(petData.birthDate);

            var ownerData = responses[1].data;
            petData.owner = ownerData.firstName + " " + ownerData.lastName;

            self.pet = petData;
            self.pet.weight = Number(self.pet.weight)
            self.checked = false;
        }).catch(function (error) {
            console.error('Error loading pet/owner details:', error);
        });

        // Open modal
        self.submit = function () {
            self.showModal = true;
        };

        // Cancel modal
        self.cancelModal = function () {
            self.showModal = false;
        };

        // Confirm modal (submit form)
        self.confirmModal = function () {
            self.showModal = false;

            var data = {
                petId: self.pet.petId,
                name: self.pet.name,
                birthDate: new Date(self.pet.birthDate).toISOString(),
                ownerId: self.pet.ownerId,
                petTypeId: self.pet.petTypeId,
                weight: self.pet.weight,
                isActive: self.pet.isActive
            };




            $http.put("api/gateway/pets/" + self.pet.petId, data)
                .then(function () {
                    $state.go('petDetails', { petId: self.pet.petId });
                })
                .catch(function (response) {
                    var error = response.data;
                    error.errors = error.errors || [];
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });
        };
    }]);
