'use strict';

angular.module('inventoriesList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventories', {
                parent: 'app',
                url: '/inventories',
                template: '<inventories-list></inventories-list>'
            })


            //delete all inventory
            .state('deleteAllInventories', {
                parent: 'app',
                url: '/inventories',
                template: '<inventory-delete-confirm></inventory-delete-confirm>'

            })

            .state('deleteInventory', {
                parent: 'app',
                url: '/inventories/:inventoryId',
                template: '<inventory-item></inventory-item>'
            })
    }]);