'use strict';

angular.module('productUpdateForm')
    .controller('ProductUpdateFormController', ["$http", '$state', '$stateParams', '$scope', 'InventoryService', function ($http, $state, $stateParams, $scope, InventoryService) {
        var self = this;
        var product = {}
        var method = $stateParams.method;

        var inventoryId = InventoryService.getInventoryId();
        console.log("InventoryId: " + inventoryId);
        var productId = $stateParams.productId;
        console.log("ProductId: " + productId)
        // post request to create a new product



        $http.get('/api/gateway/inventory/' + inventoryId + '/products/' + productId).then(function (resp) {
            self.product = resp.data;
        });

        self.submitProductUpdateForm = function () {

            var data  = {
                productName: self.product.productName,
                productDescription: self.product.productDescription,
                productPrice: self.product.productPrice,
                productQuantity: self.product.productQuantity
            }

            $http.put('/api/gateway/inventory/' + inventoryId + '/products/' + productId, data)
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

            if (!inventoryId) {
                console.error("Inventory ID is missing");
            }
            if (!productId) {
                console.error("Product ID is missing");
            }
        }

    }]);