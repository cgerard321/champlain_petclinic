'use strict';

angular.module('petForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('addPet', {
                parent: 'app',
                url: '/owners/:ownerId/pet/add-pet',
                template: '<pet-form></pet-form>'
            })
            .state('deletePet', {
                parent: 'app',
                url: '/owners/:ownerId/pet/:petId',
                template: '<pet-form></pet-form>'
            })
    }]);
