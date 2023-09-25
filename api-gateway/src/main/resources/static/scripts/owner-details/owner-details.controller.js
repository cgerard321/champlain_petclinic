'use strict';

angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        self.owner = {};
        self.pet = {};

        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            console.log("Owner id is " + $stateParams.ownerId)
            console.log(resp.data)
            self.owner = resp.data;

        });
        self.toggleActiveStatus = function (petId) {
            console.log(petId)

            $http.get('api/gateway/pets/' + petId).then(function (resp) {
                console.log("Pet id is " + $stateParams.petId)
                console.log(resp.data)
                self.pet = resp.data;

            });
            console.log(self.pet)
            console.log(self.pet.activeStatus)
            if (self.pet.activeStatus === "true") {
                self.pet.activeStatus = "false";
            } else {
                self.pet.activeStatus = "true";
            }
            console.log(self.pet.activeStatus)
            // Call the API to update the active status

            $http.patch('api/gateway/pet/' + petId, {
                activeStatus: self.pet.activeStatus
            }).then(function (resp) {
                console.log("Pet active status updated successfully");
            }).catch(function (error) {
                console.error("Error updating pet active status:", error);
                // Handle the error appropriately
            });
        };
    }]);
