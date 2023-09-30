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
        $scope.inventoryTypeOptions = ["Test option"]; //Make foreach for the get all inventory types
        $scope.selectedOption = "";

        $scope.updateOption = function() {
            for (var i = 0; i < $scope.inventoryTypeOptions.length; i++) {
                for (var j = 0; j < $scope.inventoryTypeOptions[i].length; j++){
                    if ($scope.inventoryTypeOptions[i].charAt(j) === $scope.inventoryTypeFormSearch.charAt(j)){
                        git a
                    }
                    else {

                    }
                }
            }

            if ($scope.inventoryTypeFormSearch) {
                $scope.inventoryTypeOptions.push($scope.inventoryTypeFormSearch);
                $scope.selectedOption = $scope.inventoryTypeFormSearch;
            }
        };
    }]);














