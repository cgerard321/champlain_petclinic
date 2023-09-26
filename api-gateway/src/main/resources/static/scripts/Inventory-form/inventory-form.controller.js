'use strict';

angular.module('inventoryForm')
    .controller('InventoryFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var inventoryId = $stateParams.inventoryId;
        var method = $stateParams.method;
        console.log("State params: " + $stateParams)


        self.submitInventoryForm = function () {

            var data = {
                inventoryName: self.inventory.inventoryName,
                inventoryType: self.inventory.inventoryType,
                inventoryDescription: self.inventory.inventoryDescription
            }

            if (method == 'edit') {
                $http.put('/api/gateway/inventory/' + inventoryId, data)
                    .then(function (response) {
                        console.log(response);
                        $state.go('inventories');

                    }, function (response) {
                        var error = response.data;
                        error.errors = error.errors || [];
                        alert(error.error + "\r\n" + error.errors.map(function (e) {
                            return e.field + ": " + e.defaultMessage;
                        }).join("\r\n"));
                    });
            }
            else {
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

            }
        }
    }]);














