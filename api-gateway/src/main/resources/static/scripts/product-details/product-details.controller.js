'use strict';

angular.module('productDetails')
    .controller('ProductDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;
        var inventoryId = $stateParams.inventoryId || "";
        var productId = $stateParams.productId || "";

        $http.get('api/gateway/inventory' + inventoryId + 'products' + productId).then(function (resp) {
            self.product = resp.data;
        });


    }]);
