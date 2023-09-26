'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$state', '$stateParams', '$q', function ($http, $state, $stateParams, $q) {
        var self = this;


        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;

            var petPromises = self.owner.pets.map(function(pet) {
                return $http.get('api/gateway/pets/' + pet.petId);
            });

            $q.all(petPromises).then(function(responses) {
                self.owner.pets = responses.map(function(response) {
                    return response.data;
                });
            });
        });
    }]);

