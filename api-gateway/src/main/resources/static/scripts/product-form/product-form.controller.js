'use strict';

angular.module('productForm')
    .controller('ProductFormController', ["$http", '$state', '$stateParams', '$scope', 'InventoryService', function ($http, $state , $scope,  $stateParams, InventoryService) {
        var self = this;
        var product = {}
        // post request to create a new product
        self.submitProductForm = function (product) {
            var data  = {
                productName: self.product.productName,
                productDescription: self.product.productDescription,
                productPrice: self.product.productPrice,
                productQuantity: self.product.productQuantity,
                productSalePrice: self.product.productSalePrice
            }
            var inventoryId = InventoryService.getInventoryId();
            console.log("InventoryId: " + inventoryId);
            $http.post('/api/gateway/inventory/' + inventoryId + '/products', data
            )
                .then(function (response) {
                    console.log(response);
                    $state.go('productList', {inventoryId: inventoryId});
                }, function (response) {
                    var error = response.data;
                    error.errors = error.errors || [];
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });
        }


    }]);


angular.module('shopProductForm')
    .controller('ShopProductFormController', ["$http", '$state', '$stateParams', function ($http, $state , $stateParams) {
        var self = this;
        // post request to create a new product
        self.submitProductForm = function () {
            var data  = {
                productName: self.product.productName,
                productDescription: self.product.productDescription,
                productPrice: self.product.productPrice,
                productQuantity: self.product.productQuantity,
                productSalePrice: self.product.productSalePrice
            }
            $http.post('/api/v2/gateway/products', data
            )
                .then(function (response) {
                    //console.log(response);
                    $state.go('shopProductList');

                }, function (response) {
                    var error = response.data;
                    error.errors = error.errors || [];
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });
        }
    }]);