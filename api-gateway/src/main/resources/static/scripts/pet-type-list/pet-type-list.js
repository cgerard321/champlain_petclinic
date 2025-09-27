'use strict';

angular.module('petTypeList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {

        $stateProvider
            .state('petTypes', {
                parent: 'app',
                url: '/petTypes?page&size&petTypeId&name&description',
                template: '<pet-type-list></pet-type-list>',
                controller: 'PetTypeListController',
                controllerAs: '$ctrl'
            })
    }]);