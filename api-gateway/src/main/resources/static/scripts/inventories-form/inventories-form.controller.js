'use strict';

angular.module('inventoriesForm')
    .controller('InventoriesFormController', ["$http", '$state', '$stateParams', '$scope', function ($http, $state, $stateParams, $scope) {
        var self = this;
        console.log("State params: " + $stateParams)
        $scope.inventoryTypeFormSearch = ""; 
        $scope.inventoryTypeOptions = ["New Type"] //get all form the inventory type repository but dont remove the New Type
        $http.get("api/gateway/inventories/types").then(function (resp) {
            //Includes all types inside the array
            resp.data.forEach(function (type) {
                $scope.inventoryTypeOptions.push(type.type);
            })});
        $scope.selectedOption = $scope.inventoryTypeOptions[0]

        self.submitInventoryForm = function () {
            var data;
            if ($scope.selectedOption === "New Type" && $scope.inventoryTypeFormSearch === "") {
                alert("Search field cannot be empty when you want to add a new type")
            }
            else if ($scope.selectedOption === "New Type") {
                $scope.selectedOption = $scope.inventoryTypeFormSearch

                data = {
                    inventoryName: self.inventory.inventoryName,
                    inventoryType: $scope.selectedOption,
                    inventoryDescription: self.inventory.inventoryDescription
                }
                $http.post("api/gateway/inventories/types", {"type":$scope.selectedOption})
                    .then(function (resp) {
                        $http.post("api/gateway/inventories", data)
                            .then(function (resp) {
                                console.log(resp)
                                $state.go('inventories');
                            }, function (response) {
                                var error = response.data;
                                error.errors = error.errors || [];
                                alert(error.error + "\r\n" + error.errors.map(function (e) {
                                    return e.field + ": " + e.defaultMessage;
                                }).join("\r\n"));
                            });
                    })
                    .catch(function (error) {
                        alert(error)
                    })
            }
            else {
                data = {
                    inventoryName: self.inventory.inventoryName,
                    inventoryType: $scope.selectedOption,
                    inventoryDescription: self.inventory.inventoryDescription
                }
                $http.post("api/gateway/inventories", data)
                    .then(function (resp) {
                        console.log(resp)
                        $state.go('inventories');
                    }, function (response) {
                        var error = response.data;
                        error.errors = error.errors || [];
                        alert(error.error + "\r\n" + error.errors.map(function (e) {
                            return e.field + ": " + e.defaultMessage;
                        }).join("\r\n"));
                    });
            }
        };
        $scope.updateOption = function() {
            var searchLowerCase = $scope.inventoryTypeFormSearch.toLowerCase();
            $scope.selectedOption = $scope.inventoryTypeOptions[0];
            for (var i = 0; i < $scope.inventoryTypeOptions.length; i++) {
                var optionLowerCase = $scope.inventoryTypeOptions[i].toLowerCase();
                if (optionLowerCase.indexOf(searchLowerCase) !== -1) {
                    $scope.selectedOption = $scope.inventoryTypeOptions[i];
                    break;
                }
            }
        };
    }]);














