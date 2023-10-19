'use strict';

angular.module('productUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider

            .state('updateProductInventory', {
                parent: 'app',
                url: '/inventory/:inventoryId/products/:productId',
                template: '<product-update-form></product-update-form>'
            })

    }]);

//url: '/inventory/:inventoryId/products/:productId/:method',
