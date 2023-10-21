angular.module('petDetails')
    .controller('PetDetailsController', PetDetailsController);

PetDetailsController.$inject = ['$http', '$state', '$stateParams', '$scope', '$timeout', '$q'];

function PetDetailsController($http, $state, $stateParams, $scope, $timeout, $q) {
    var vm = this; // Use 'vm' (short for ViewModel) instead of 'self'

    // Initialize properties
    vm.pet = {};

    vm.getPetTypeName = function (petTypeId) {
        switch (petTypeId) {
            case '1':
                return 'Cat';
            case '2':
                return 'Dog';
            case '3':
                return 'Lizard';
            case '4':
                return 'Snake';
            case '5':
                return 'Bird';
            case '6':
                return 'Hamster';
            default:
                return 'Unknown';
        }
    };
    // Fetch owner data
    $http.get('api/gateway/pets/' + $stateParams.petId)
        .then(function (resp) {
            vm.pet = resp.data;
            vm.pets.forEach(function(pet) {
                pet.isActive = pet.isActive === "true";
            });

            console.log(vm.pet);
        })
        .catch(function (error) {
            console.error('Error fetching pet data:', error);
        });


    // Toggle pet's active status
    vm.toggleActiveStatus = function (petId) {
        return $http.get('api/gateway/pets/' +$stateParams.petId + '?_=' + new Date().getTime(), { headers: { 'Cache-Control': 'no-cache' } })
            .then(function (resp) {
                console.log("Pet id is " + petId);
                console.log(resp.data);
                vm.pet = resp.data;
                console.log("Pet id is " + vm.pet.petId);
                console.log(vm.pet);
                console.log("=====================================");
                console.log(resp.data);
                console.log("Active status before is:" + vm.pet.isActive);
                vm.pet.isActive = vm.pet.isActive === "true" ? "false" : "true";
                console.log("Active status after is:" + vm.pet.isActive);

                return $http.patch('api/gateway/pet/' + $stateParams.petId, {
                    isActive: vm.pet.isActive
                }, { headers: { 'Cache-Control': 'no-cache' } });
            })
            .then(function (resp) {
                console.log("Pet active status updated successfully");
                vm.pet = resp.data;
                // Schedule a function to be executed during the next digest cycle
                $scope.$evalAsync();
            })
            .catch(function (error) {
                console.error("Error updating pet active status:", error);
                // Handle the error appropriately
            });
    };

    vm.deletePet = function (petId) {
        var config = {
            headers: {
                'Content-Type': 'application/json'
            }
        };

        $http.delete('api/gateway/pets/' + petId, config)
            .then(function (resp) {
                console.log("Pet deleted successfully");

                /*  $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
                      self.owner = resp.data;
                  });
                 */

                $scope.$applyAsync();
                $state.go(ownerDetails)
                // Handle the success appropriately
            }).catch(function (error) {
            console.error("Error deleting pet:", error);
            // Handle the error appropriately
        });
    };


}
