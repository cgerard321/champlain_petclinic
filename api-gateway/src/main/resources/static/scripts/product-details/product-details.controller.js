'use strict';

angular.module('productDetails')
    .controller('ProductDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;
        self.product = {}; // Initialize self.product
        var inventoryId = $stateParams.inventoryId || "";
        var productId = $stateParams.productId || "";

            $http.get('api/gateway/inventory/' + $stateParams.inventoryId + '/products/' + productId)
                .then(function (resp) {
                    // Handle the response data for the specific product
                    var product = resp.data;
                    console.log("Product found:", product);
                    self.product = product; // Update the product data in your controller
                })
                .catch(function (error) {
                    // Handle errors if the product is not found or other issues
                    console.error("Error fetching product:", error);
                });

    }]);
