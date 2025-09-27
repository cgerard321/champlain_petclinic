'use strict';

angular.module('petTypeList')
    .controller('PetTypeListController', ['$http', function ($http) {
        var self = this;
        self.petTypes = {};
        self.showAddForm = false;
        self.newPetType = { name: '', petTypeDescription: '' };

        self.toggleAddForm = function () {
            self.showAddForm = !self.showAddForm;
            if (!self.showAddForm) {
                self.newPetType = { name: '', petTypeDescription: '' };
            }
        };

        self.addPetType = function () {
            // Extra check to look for numbers or special characters
            var lettersOnly = /^[a-zA-Z\s]+$/;

            if (!lettersOnly.test(self.newPetType.name) || !lettersOnly.test(self.newPetType.petTypeDescription)) {
                alert('Name and Description must contain letters and spaces only.');
                return;
            }

            if (!self.newPetType.name || !self.newPetType.petTypeDescription) {
                alert('Name and Description are required.');
                return;
            }

            $http.post('api/gateway/owners/petTypes', self.newPetType)
                .then(function () {
                    alert('Pet type added successfully!');
                    self.showAddForm = false;
                    self.newPetType = { name: '', petTypeDescription: '' };
                    $http.get('api/gateway/owners/petTypes').then(function (resp) {
                        self.petTypes = resp.data;
                    });
                }, function (error) {
                    alert('Failed to add pet type: ' + (error.data.message || 'Unknown error'));
                });
        };

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.petTypes = resp.data;
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


