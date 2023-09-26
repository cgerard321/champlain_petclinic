'use strict';

angular.module('inventoryForm')
    .controller('InventoryFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        console.log("State params: " + $stateParams)


        self.submitInventoryForm = function (value = true) {

            var data  = {
                inventoryName: self.inventory.inventoryName,
                inventoryType: self.inventory.inventoryType,
                inventoryDescription: self.inventory.inventoryDescription
            }

            if (value) {
                var req;
                req = $http.post("api/gateway/inventory", self.inventory);
                req.then(function () {
                    $state.go('inventories');
                }, function (response) {
                    var error = response.data;
                    error.errors = error.errors || [];
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });

            } else {
                $http.put('/api/gateway/inventory/' + self.inventory.inventoryId, data)
                    .then(function (response) {
                        console.log(response);
                        $state.go('updateInventories');
                    }, function (response) {
                        var error = response.data;
                        error.errors = error.errors || [];
                        alert(error.error + "\r\n" + error.errors.map(function (e) {
                            return e.field + ": " + e.defaultMessage;
                        }).join("\r\n"));
                    });
            }

        }
    }]);














