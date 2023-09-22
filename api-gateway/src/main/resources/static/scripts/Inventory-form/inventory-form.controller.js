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
    }]);
