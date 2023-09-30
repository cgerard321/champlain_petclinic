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
                productQuantity: self.product.productQuantity
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