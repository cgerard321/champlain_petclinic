'use strict';

angular.module('productForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productAdd', {
                parent: 'app',
                url: '/product/new',
                template: '<product-form></product-form>'
            })

    }]);
