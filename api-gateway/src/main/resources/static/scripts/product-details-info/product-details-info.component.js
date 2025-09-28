'use strict';

angular.module('productDetailsInfo')
    .component('productDetailsInfo', {
        templateUrl: 'scripts/product-details-info/product-details-info.template.html',
        controller: 'ProductDetailsInfoController'
    });

angular.module('shopProductDetailsInfo')
    .component('shopProductDetailsInfo', {
        templateUrl: 'scripts/product-details-info/shop-product-details-info.template.html',
        controller: 'ShopProductDetailsInfoController'
    });

