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

    vm.getBirthday = function(birthday) {
        if (birthday) {
            var date = new Date(birthday);
            var year = date.getUTCFullYear();
            var month = (date.getUTCMonth() + 1).toString().padStart(2, '0'); // Months are zero-based, so we add 1
            var day = date.getUTCDate().toString().padStart(2, '0');
            return year + ' / ' + month + ' / ' + day;
        } else {
            return '';
        }
    };

}
