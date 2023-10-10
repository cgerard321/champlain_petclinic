angular.module('ownerDetails')
    .controller('OwnerDetailsController', OwnerDetailsController);

OwnerDetailsController.$inject = ['$http', '$state', '$stateParams', '$scope', '$timeout', '$q'];

function OwnerDetailsController($http, $state, $stateParams, $scope, $timeout, $q) {
    var vm = this; // Use 'vm' (short for ViewModel) instead of 'self'

    // Initialize properties
    vm.owner = {};
    vm.pet = {};
    vm.pets = [];

    // Function to get pet type name based on petTypeId
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
    $http.get('api/gateway/owners/' + $stateParams.ownerId)
        .then(function (resp) {
            vm.owner = resp.data;
            console.log(vm.owner);

            var petPromises = vm.owner.pets.map(function (pet) {
                return $http.get('api/gateway/pets/' + pet.petId, { cache: false });
            });

            $q.all(petPromises).then(function (responses) {
                vm.owner.pets = responses.map(function (response) {
                    return response.data;
                });
            });
        })
        .catch(function (error) {
            console.error('Error fetching owner data:', error);
        });

    vm.toggleActiveStatus = function (petId) {
        $http.get('api/gateway/pets/' + petId + '?_=' + new Date().getTime(), { headers: { 'Cache-Control': 'no-cache' } })
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

                return $http.patch('api/gateway/pet/' + petId, {
                    isActive: vm.pet.isActive
                }, { headers: { 'Cache-Control': 'no-cache' } });
            })
            .then(function (resp) {
                console.log("Pet active status updated successfully");
                vm.pet = resp.data;
                $timeout(); // Manually trigger the $digest cycle to update the UI
            })
            .catch(function (error) {
                console.error("Error updating pet active status:", error);
                // Handle the error appropriately
            });
    };
}
