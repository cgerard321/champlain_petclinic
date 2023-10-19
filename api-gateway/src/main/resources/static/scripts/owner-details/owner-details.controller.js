angular.module('ownerDetails')
    .controller('OwnerDetailsController', ['$http', '$state', '$stateParams', '$scope', '$timeout', '$q', function ($http, $state, $stateParams, $scope, $timeout, $q) {
        var self = this;
        self.owner = {};
        self.pet = {};

        $http.get('api/gateway/owners/' + $stateParams.ownerId, {
            headers: {
                'Cache-Control': 'no-cache'
            }
        }).then(function (resp) {
            self.owner = resp.data;
            self.owner.pets.forEach(function(pet) {
                pet.isActive = pet.isActive === "true";
            });
            console.log(self.owner);

            var petPromises = self.owner.pets.map(function (pet) {
                return $http.get('api/gateway/pets/' + pet.petid, {
                    headers: {
                        'Cache-Control': 'no-cache'
                    }

                });
            });

            $q.all(petPromises).then(function (responses) {
                self.owner.pets = responses.map(function (response) {
                    return response.data;
                });
            });
        });

        self.deletePet = function (petId) {
            var config = {
                headers: {
                    'Content-Type': 'application/json'
                }
            };
            $http.delete('api/gateway/pets/' + petId, config)
                .then(function (resp) {
                    console.log("Pet deleted successfully");

                    self.owner.pets = self.owner.pets.filter(function(pet) {
                        return pet.petId !== petId;
                    });

                    $scope.$applyAsync();
                    // Handle the success appropriately
                }).catch(function (error) {
                console.error("Error deleting pet:", error);
                // Handle the error appropriately
            });
        };

        self.toggleActiveStatus = function (petId) {
            $http.get('api/gateway/pets/' + petId, {
                headers: {
                    'Cache-Control': 'no-cache'
                }
            }).then(function (resp) {
                console.log("Pet id is " + petId);
                console.log(resp.data);
                self.pet = resp.data;
                console.log("Pet id is " + self.pet.petId);
                console.log(self.pet);
                console.log("=====================================");
                console.log(resp.data);
                console.log("Active status before is:" + self.pet.isActive);
                self.pet.isActive = self.pet.isActive === "true" ? "false" : "true";
                console.log("Active status after is:" + self.pet.isActive);

                $http.patch('api/gateway/pet/' + petId, {
                    isActive: self.pet.isActive
                }, { headers: { 'Cache-Control': 'no-cache' } }).then(function (resp) {
                    console.log("Pet active status updated successfully");
                    self.pet = resp.data;
                    $scope.$applyAsync()
                    $timeout(); // Manually trigger the $digest cycle to update the UI
                }).catch(function (error) {
                    console.error("Error updating pet active status:", error);
                    // Handle the error appropriately
                });
            });
        };
    }]);
