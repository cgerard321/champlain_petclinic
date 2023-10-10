'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', '$filter', '$q', function ($http, $state, $stateParams, $filter, $q) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var petId = $stateParams.petId || 0;

        $http.get('api/gateway/owners/petTypes')
            .then(function (resp) {
                self.types = resp.data;
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

        self.submit = function () {
            var data = {
                petId: self.pet.petId,
                name: self.pet.name,
                birthDate: new Date(self.pet.birthDate).toISOString(),
                ownerId: self.pet.ownerId,
                petTypeId: self.pet.petTypeId
            };

            $http.put("api/gateway/pets/" + petId, data)
                .then(function () {
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

