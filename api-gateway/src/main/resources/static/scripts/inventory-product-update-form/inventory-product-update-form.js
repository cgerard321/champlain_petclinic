'use strict';

angular.module('inventoryProductUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider

            .state('updateProductInventory', {
                parent: 'app',
                url: '/inventories/:inventoryId/products/:productId',
                template: '<product-update-form></product-update-form>'
            })

    }]);