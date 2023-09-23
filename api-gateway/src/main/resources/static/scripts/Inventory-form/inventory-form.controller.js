'use strict';

angular.module('inventoryForm')
    .controller('InventoryFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        self.submitInventoryForm = function () {
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
        };


        self.updateInventory = function () {
            var req;
            var inventoryUUID = self.inventory.uuid; // Assuming you have an inventory UUID

            var varIsConf = confirm('Want to update inventory with Inventory Id: ' + inventoryUUID + '. Are you sure?');

            if (varIsConf) {
                req = $http.put('api/gateway/inventory/' + inventoryUUID, self.inventory);

                req.then(function () {
                    // Handle success, e.g., show a success message and navigate to a new page
                    console.log('Inventory updated successfully.');
                    $state.go('inventories'); // You may need to adjust the state name
                }, function (response) {
                    // Handle error, e.g., show an error message
                    console.error('Error updating inventory:', response.data);
                });
            }
        };
    }]);
