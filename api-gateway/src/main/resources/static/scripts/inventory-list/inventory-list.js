'use strict';

angular.module('inventoryList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventories', {
                parent: 'app',
                url: '/inventory',
                template: '<inventory-list></inventory-list>'
            })


            //delete all inventory
            .state('deleteAllInventories', {
                parent: 'app',
                url: '/inventory',
                template: '<inventory-delete-confirm></inventory-delete-confirm>'

            })

            .state('deleteInventory', {
                parent: 'app',
                url: '/inventory/:inventoryId',
                template: '<inventory-item></inventory-item>'
            })
    }]);