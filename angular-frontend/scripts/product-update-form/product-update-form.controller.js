'use strict';



angular.module('shopProductUpdateForm')
    .controller('ShopProductUpdateFormController', [
        "$http", "$state", "$stateParams",
        function ($http, $state, $stateParams) {
            var self = this;
            var productId = $stateParams.productId;

            console.log("ProductId: " + productId);

            // Load product by ID
            $http.get('/api/v2/gateway/products/' + productId).then(function (resp) {
                self.product = resp.data;
            });

            // update
            self.submitProductUpdateForm = function () {
                var data = {
                    productName: self.product.productName,
                    productDescription: self.product.productDescription,
                    productPrice: self.product.productPrice,
                    productQuantity: self.product.productQuantity,
                    productSalePrice: self.product.productSalePrice
                };

                $http.put('/api/v2/gateway/products/' + productId, data)
                    .then(function (response) {
                        console.log(response);
                        $state.go('shopProductList');
                    }, function (response) {
                        var error = response.data;
                        error.errors = error.errors || [];
                        alert(error.error + "\r\n" + error.errors.map(function (e) {
                            return e.field + ": " + e.defaultMessage;
                        }).join("\r\n"));
                    });

                if (!productId) {
                    console.error("Product ID is missing");
                }
            };
        }
    ]);