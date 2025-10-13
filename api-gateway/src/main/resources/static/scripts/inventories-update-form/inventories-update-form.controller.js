'use strict';

angular.module('inventoriesUpdateForm')
    .controller('InventoriesUpdateFormController', ["$http", '$state', '$stateParams', '$scope', function ($http, $state, $stateParams, $scope) {
        var self = this;
        var inventoryId = $stateParams.inventoryId || "";
        var method = 'edit';
        $scope.inventoryTypeFormUpdateSearch = "";
        $scope.inventoryTypeUpdateOptions = ["New Type"];
        $http.get('api/gateway/inventories/' + inventoryId).then(function (resp) {
            self.inventory = resp.data;

            $http.get("api/gateway/inventories/types").then(function (typesResp) {

                // Includes all types inside the array
                typesResp.data.forEach(function (type) {
                    $scope.inventoryTypeUpdateOptions.push(type.type);
                });
                var inventoryType = self.inventory.inventoryType;
                if ($scope.inventoryTypeUpdateOptions.includes(inventoryType)) {
                    $scope.selectedUpdateOption = inventoryType;
                } else {
                    $scope.inventoryTypeFormUpdateSearch = $scope.inventoryTypeUpdateOptions[0];
                }
            });
        });

        self.submitUpdateInventoryForm = function () {
            var data;

            if ($scope.selectedUpdateOption === "New Type" && $scope.inventoryTypeFormUpdateSearch === "") {
                alert("Search field cannot be empty when you want to add a new type");
            } else if ($scope.selectedUpdateOption === "New Type") {
                $scope.selectedUpdateOption = $scope.inventoryTypeFormUpdateSearch;
                data = {
                    inventoryName: self.inventory.inventoryName,
                    inventoryType: $scope.selectedUpdateOption,
                    inventoryDescription: self.inventory.inventoryDescription
                };

                $http.post("api/gateway/inventories/types", { "type": $scope.selectedUpdateOption })
                    .then(function (resp) {
                        if (method === 'edit') {
                            $http.put('/api/gateway/inventories/' + inventoryId, data)
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
                        } else {
                            console.error("Invalid method:", method);
                        }
                        if (!inventoryId) {
                            console.error("Inventory ID is missing");
                        }
                    });
            }
            else {
                data = {
                    inventoryName: self.inventory.inventoryName,
                    inventoryType: $scope.selectedUpdateOption,
                    inventoryDescription: self.inventory.inventoryDescription
                }
                if (method === 'edit') {
                    $http.put('/api/gateway/inventories/' + inventoryId, data)
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
                } else {
                    console.error("Invalid method:", method);
                }
                if (!inventoryId) {
                    console.error("Inventory ID is missing");
                }
            }
        };

        $scope.updateOptionUpdate = function() {
            var searchLowerCase = $scope.inventoryTypeFormUpdateSearch.toLowerCase();
            $scope.selectedUpdateOption = $scope.inventoryTypeUpdateOptions[0];
            for (var i = 0; i < $scope.inventoryTypeUpdateOptions.length; i++) {
                var optionLowerCase = $scope.inventoryTypeUpdateOptions[i].toLowerCase();
                if (optionLowerCase.indexOf(searchLowerCase) !== -1) {
                    $scope.selectedUpdateOption = $scope.inventoryTypeUpdateOptions[i];
                    break;
                }
            }
        };
    }]);
