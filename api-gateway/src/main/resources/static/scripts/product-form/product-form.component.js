'use strict';

angular.module('productForm')
    .component('productForm', {
        templateUrl: 'scripts/product-form/product-form.template.html',
        controller: 'ProductFormController'
    });

angular.module('shopProductForm')
    .component('shopProductForm', {
        templateUrl: 'scripts/product-form/shop-product-form.template.html',
        controller: 'ShopProductFormController'
    });