'use strict';

angular.module('inventoryList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventories', {
                parent: 'app',
                url: '/inventory',
                template: '<inventory-list></inventory-list>'
            })


            /*.state('deleteBundle', {
                parent: 'app',
                url: '/bundles/:bundleUUID/deleteBundle',
                template: '<bundle-list></bundle-list>'
            })*/
            //delete all inventory
            .state('deleteAllInventories', {
                parent: 'app',
                url: '/inventory',
                template: '<inventory-delete-confirm></inventory-delete-confirm>'

            })
    }]);