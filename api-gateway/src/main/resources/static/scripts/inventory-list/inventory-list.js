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
    }]);