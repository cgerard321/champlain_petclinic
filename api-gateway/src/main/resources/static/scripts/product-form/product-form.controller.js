'use strict';

angular.module('productForm')
    .controller('ProductFormController', ["$http", '$state', '$stateParams', function ($http, $state , $scope,  $stateParams) {
        var self = this;
      //  var inventoryId = $stateParams.inventoryId;
        var inventoryId = 1 //temporarily hardcoding inventoryId to
        var product = {}
        // post request to create a new product
        self.submitProductForm = function () {
            var data  = {
                productName: self.product.productName,
                productDescription: self.product.productDescription,
                productPrice: self.product.productPrice,
                productQuantity: self.product.productQuantity
            }

            $http.post('/api/gateway/inventory/' + inventoryId + '/products', data
                // productName: $scope.product.productName,
                // productDescription: $scope.product.productDescription,
                // productPrice: $scope.product.productPrice,
                // productQuantity: $scope.product.productQuantity
            )
                .then(function (response) {
                    console.log(response);
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