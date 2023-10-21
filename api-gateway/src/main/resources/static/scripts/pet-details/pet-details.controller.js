angular.module('petDetails')
    .controller('PetDetailsController', PetDetailsController);

PetDetailsController.$inject = ['$http', '$state', '$stateParams', '$scope', '$timeout', '$q'];

function PetDetailsController($http, $state, $stateParams, $scope, $timeout, $q) {
    var vm = this; // Use 'vm' (short for ViewModel) instead of 'self'

    // Initialize properties
    vm.pet = {};

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
