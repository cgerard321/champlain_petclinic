'use strict';

angular.module('petTypeList')
    .controller('PetTypeListController', ['$http', '$stateParams', '$scope', '$state', function ($http, $stateParams, $scope, $state) {
        var self = this;
        self.petTypes = {};
        // Initialize as an empty array

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.petTypes = resp.data;
            console.log(self.petTypes);

        });

        self.deletePetType = function(petTypeId) {
            let isConfirmed = confirm('Are you sure you want to delete this pet type?');
            if (isConfirmed) {
                $http.delete('api/gateway/owners/petTypes/' + petTypeId)
                    .then(function(response) {
                        console.log('Pet type deleted successfully');
                        alert('Pet type deleted successfully!');
                
                        // Refresh the pet types list
                        $http.get('api/gateway/owners/petTypes').then(function (resp) {
                            self.petTypes = resp.data;
                        });
                    }, function(error) {
                        console.error('Error deleting pet type:', error);
                        alert('Failed to delete pet type: ' + (error.data.message || 'Unknown error'));
            });
    }
};
    }]);


