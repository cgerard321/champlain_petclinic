'use strict';

angular.module('productForm')
    .controller('ProductFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var inventoryId = $stateParams.inventoryId;
        inventoryId = 1 //temporarily hardcoding inventoryId to 1

        // post request to create a new product
        self.submitProductForm = function () {
            console.log(self.product.productName)
            console.log(self.product.productDescription)
            console.log(self.product.productPrice)
            console.log(self.product.productQuantity)
            $http.post('/api/gateway/inventory/' + inventoryId + '/products', {
                productName: self.product.productName,
                productDescription: self.product.productDescription,
                productPrice: self.product.productPrice,
                productQuantity: self.product.productQuantity

                }
                // productName: $scope.product.productName,
                // productDescription: $scope.product.productDescription,
                // productPrice: $scope.product.productPrice,
                // productQuantity: $scope.product.productQuantity
            )
                .then(function (response) {
                    $state.go('productList');
                }, function (response) {
                    var error = response.data;
                    error.errors = error.errors || [];
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });
        }

    }]);