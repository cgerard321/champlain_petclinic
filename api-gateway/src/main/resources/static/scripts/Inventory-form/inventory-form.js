'use strict';

angular.module('inventoryForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventoryNew', {
                parent: 'app',
                url: '/inventory/new',
                template: '<inventory-form></inventory-form>'
            })



    }]);