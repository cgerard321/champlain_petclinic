'use strict';

angular.module('inventoryForm')
    .controller('InventoryFormController', ["$http", '$state', '$stateParams', '$scope', function ($http, $state, $stateParams, $scope) {
        var self = this;
        console.log("State params: " + $stateParams)


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
        $scope.inventoryTypeFormSearch = "";
        $scope.inventoryTypeOptions = ["Internal", "Sales"];
        $scope.selectedOption = "";

        $scope.updateOption = function() {
            var searchLowerCase = $scope.inventoryTypeFormSearch.toLowerCase();
            $scope.selectedOption = "";
            for (var i = 0; i < $scope.inventoryTypeOptions.length; i++) {
                var optionLowerCase = $scope.inventoryTypeOptions[i].toLowerCase();
                if (optionLowerCase.indexOf(searchLowerCase) !== -1) {
                    $scope.selectedOption = $scope.inventoryTypeOptions[i];
                    break;
                }
            }
        };
    }]);














