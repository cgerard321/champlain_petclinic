'use strict';

angular.module('productForm')
    .controller('ProductFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        // post request to create a new product
        self.createProduct = function (inventoryId) {
            $http.post('/api/gateway/inventory/' + inventoryId + '/products', self.product)
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