'use strict';

angular.module('inventoryList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventories', {
                parent: 'app',
                url: '/inventories',
                template: '<inventory-list></inventory-list>'
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