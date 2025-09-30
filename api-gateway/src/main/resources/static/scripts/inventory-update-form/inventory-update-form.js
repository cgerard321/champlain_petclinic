'use strict';

angular.module('inventoryUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider

            .state('updateInventory', {
                    parent: 'app',
                    url: '/inventories/:inventoryId/edit',
                    template: '<inventory-update-form></inventory-update-form>'
                }
            )

    }]);