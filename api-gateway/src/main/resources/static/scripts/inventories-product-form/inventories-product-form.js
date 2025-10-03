'use strict';

angular.module('inventoriesProductForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productNew', {
                parent: 'app',
                url: '/inventories/:inventoryId/product/new',
                params : {inventoryId: null},
                // url: '/product/new',
                template: '<product-form></product-form>'
            })

    }]);