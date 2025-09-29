'use strict';

angular.module('productUpdateForm')
    .component('productUpdateForm', {
        templateUrl: 'scripts/product-update-form/product-update-form.template.html',
        controller: 'ProductUpdateFormController'
    });

angular.module('shopProductUpdateForm')
    .component('shopProductUpdateForm', {
        templateUrl: 'scripts/product-update-form/shop-product-update-form.template.html',
        controller: 'ShopProductUpdateFormController'
    });