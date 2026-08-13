angular.module('petDetails')
    .controller('PetDetailsController', PetDetailsController);

PetDetailsController.$inject = ['$http', '$state', '$stateParams', '$scope', '$timeout', '$q'];

function PetDetailsController($http, $state, $stateParams, $scope, $timeout, $q) {
    var vm = this; // ViewModel

    // Initialize properties
    vm.pet = {};
    vm.showDeleteModal = false; // Delete modal
    vm.showModal = false;       // Update modal (if needed)

    //Should not be hard coded anymore
    vm.getPetTypeName = function (petTypeId) {
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

    // Fetch pet data
    $http.get('api/gateway/pets/' + $stateParams.petId)
        .then(function (resp) {
            vm.pet = resp.data;
            console.log(vm.pet);
        })
        .catch(function (error) {
            console.error('Error fetching pet data:', error);
        });

    // Format birthday
    vm.getBirthday = function(birthday) {
        if (!birthday) return '';
        var date = new Date(birthday);
        var timezoneOffset = date.getTimezoneOffset() * 60000;
        date = new Date(date.getTime() - timezoneOffset);
        var year = date.getFullYear();
        var month = (date.getMonth() + 1).toString().padStart(2, '0');
        var day = date.getDate().toString().padStart(2, '0');
        return year + ' / ' + month + ' / ' + day;
    };

    // Toggle pet's active status
    vm.toggleActiveStatus = function (petId) {
        return $http.get('api/gateway/pets/' + $stateParams.petId + '?_=' + new Date().getTime(), {headers: { 'Cache-Control': 'no-cache' }})
            .then(function (resp) {
                vm.pet = resp.data;
                vm.pet.isActive = vm.pet.isActive === "true" ? "false" : "true";

                return $http.patch('api/gateway/pets/' + $stateParams.petId + "/active" + '?isActive=' + vm.pet.isActive, null, {headers: { 'Cache-Control': 'no-cache' }});
            })
            .then(function (resp) {
                vm.pet = resp.data;
                $scope.$evalAsync();
            })
            .catch(function (error) {
                console.error("Error updating pet active status:", error);
            });
    };

    // Open delete confirmation modal
    vm.confirmDeletePet = function(petId) {
        vm.showDeleteModal = true;
    };

    // Cancel delete
    vm.cancelDelete = function() {
        vm.showDeleteModal = false;
    };

    // Confirm delete (actually delete)
    vm.deletePet = function() {
        vm.showDeleteModal = false;

        var config = { headers: { 'Content-Type': 'application/json' } };

        $http.delete('api/gateway/pets/' + $stateParams.petId, config)
            .then(function(resp) {
                console.log("Pet deleted successfully");
                $state.go('ownerDetails', { ownerId: vm.pet.ownerId });
            })
            .catch(function(error) {
                console.error("Error deleting pet:", error);
                alert('Error deleting pet: ' + (error.data.error || 'Unknown error'));
            });
    };

}
