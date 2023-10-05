'use strict';

angular.module('productDetails')
    .controller('ProductDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var inventoryId = $stateParams.inventoryId || "";

        $http.get('api/gateway/inventory/' + inventoryId + '/products/' + $stateParams.productId)
            .then(function (resp) {
                self.product = resp.data;
            });


    }]);
