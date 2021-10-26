'use strict';

angular.module('petForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('addPet', {
                parent: 'app',
                url: '/owners/:ownerId/pets/add-pet',
                template: '<pet-form></pet-form>'
            })
            .state('deletePet', {
                parent: 'app',
                url: '/owners/:ownerId/pets/:petId/:method',
                template: '<pet-form></pet-form>'
            })
    }]);
