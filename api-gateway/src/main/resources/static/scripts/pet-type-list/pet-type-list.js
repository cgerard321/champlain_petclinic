'use strict';

angular.module('petTypeList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {

        $stateProvider
            .state('petTypes', {
                parent: 'app',
                url: '/petTypes',
                template: '<pet-type-list></pet-type-list>',
                controller: 'PetTypeListController',
                controllerAs: 'vm'
            })
    }]);