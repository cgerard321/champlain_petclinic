'use strict';

angular.module('inventoryUpdateForm')
    .controller('InventoryUpdateFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var inventoryId = $stateParams.inventoryId || "";
        var method = $stateParams.method;


        $http.get('api/gateway/inventory/' + inventoryId).then(function (resp) {
            self.inventory = resp.data;

        });

  

        self.submitUpdateInventoryForm = function () {
            var req;
            var data = {
                inventoryName: self.inventory.inventoryName,
                inventoryType: self.inventory.inventoryType,
                inventoryDescription: self.inventory.inventoryDescription
            }

            if (method === 'edit') {
                req = $http.put('/api/gateway/inventory/' + inventoryId, data);
                req.then(function (response) {
                    console.log(response);
                    $state.go('inventories');
                }, function (response) {
                    var error = response.data;
                    error.errors = error.errors || [];
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });
            } else {

                console.error("Invalid method:", method);

            }

            if (!inventoryId) {

                console.error("Inventory ID is missing");

            }
        };
    }]);
