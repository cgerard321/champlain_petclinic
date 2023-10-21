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
            console.log(vm.pet);
        })
        .catch(function (error) {
            console.error('Error fetching pet data:', error);
        });

}
