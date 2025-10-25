'use strict';

angular.module('inventoriesList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventories', {
                parent: 'app',
                url: '/inventories',
                template: '<inventories-list></inventories-list>'
            })

            .state('deleteInventory', {
                parent: 'app',
                url: '/inventories/:inventoryId',
                template: '<inventory-item></inventory-item>'
            })
    }]);