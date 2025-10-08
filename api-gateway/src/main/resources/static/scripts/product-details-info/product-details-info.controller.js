

angular.module('shopProductDetailsInfo')
    .controller('ShopProductDetailsInfoController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        self.product = {}; // Initialize self.product
        var productId = $stateParams.productId;

        $http.get('/api/v2/gateway/products/' + productId)
            .then(function (resp) {
                // Handle the response data for the specific product
                var product = resp.data;
                //console.log("Product found:", product);
                self.product = product; // Update the product data in your controller
            })
            .catch(function (error) {
                // Handle errors if the product is not found or other issues
                console.error("Error fetching product:", error);
            });

    }]);