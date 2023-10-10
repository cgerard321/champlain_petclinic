angular.module('ownerDetails')
    .controller('OwnerDetailsController', OwnerDetailsController);

OwnerDetailsController.$inject = ['$http', '$state', '$stateParams', '$scope', '$timeout', '$q'];

function OwnerDetailsController($http, $state, $stateParams, $scope, $timeout, $q) {
    var vm = this; // Use 'vm' (short for ViewModel) instead of 'self'

    // Initialize properties
    vm.owner = {};
    vm.pet = {};
    vm.pets = [];

    // Fetch owner data
    $http.get('api/gateway/owners/' + $stateParams.ownerId)
        .then(function (resp) {
            vm.owner = resp.data;
            console.log(vm.owner);
        })
        .catch(function (error) {
            console.error('Error fetching owner data:', error);
        });

    // Fetch associated pets and their details
    $http.get(`api/gateway/owners/${$stateParams.ownerId}/pets`)
        .then(function (response) {
            // Split the response by newline characters to get individual pet objects
            var petResponses = response.data.split('\n');

            // Parse each pet response as JSON, remove the "data:" prefix, and trim any leading/trailing whitespace
            var petObjects = petResponses.map(function (petResponse) {
                // Remove the "data:" prefix and trim any leading/trailing whitespace
                var trimmedResponse = petResponse.replace(/^data:/, '').trim();
                console.log("Trimmed results: ", trimmedResponse);

                // Check if the trimmed response is empty
                if (!trimmedResponse) {
                    return null; // Skip empty responses
                }

                try {
                    return JSON.parse(trimmedResponse);
                } catch (error) {
                    console.error('Error parsing pet response:', error);
                    return null;
                }
            });

            // Filter out any parsing errors (null values)
            petObjects = petObjects.filter(function (pet) {
                return pet !== null;
            });

            // Assuming that each pet has a 'petId' property, you can create an array of promises to fetch detailed pet data
            var petPromises = petObjects.map(function (pet) {
                return $http.get(`api/gateway/pets/${pet.petId}`);
            });

            return $q.all(petPromises);
        })
        .then(function (responses) {
            vm.pets = responses.map(function (response) {
                return response.data;
            });
            console.log("Pet Array:", vm.pets);
        })
        .catch(function (error) {
            console.error('Error fetching pet data:', error);
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
