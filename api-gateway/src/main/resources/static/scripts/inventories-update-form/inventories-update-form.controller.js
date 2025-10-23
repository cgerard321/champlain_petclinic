'use strict';

angular.module('inventoriesUpdateForm')
    .controller('InventoriesUpdateFormController', ["$http", '$state', '$stateParams', '$scope', function ($http, $state, $stateParams, $scope) {
        var self = this;
        var inventoryId = $stateParams.inventoryId || "";
        var method = 'edit';
        $scope.saving = false;
        $scope.inventoryTypeFormUpdateSearch = "";
        $scope.inventoryTypeUpdateOptions = ["New Type"];
        $http.get('/api/gateway/inventories/' + inventoryId).then(function (resp) {
            self.inventory = resp.data;

            $http.get("/api/gateway/inventories/types").then(function (typesResp) {

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
            }, handleHttpError);
        },handleHttpError);

        self.submitUpdateInventoryForm = function () {
            if ($scope.inventoryUpdateForm && $scope.inventoryUpdateForm.$invalid) { 
        angular.forEach($scope.inventoryUpdateForm.$error, function (fields) { 
          (fields || []).forEach(function (f) { f.$setTouched(); });           
        });                                                                     
        return;                                                                
      }
            var data;

            if ($scope.selectedUpdateOption === "New Type" && $scope.inventoryTypeFormUpdateSearch === "") {
                alert("Search field cannot be empty when you want to add a new type");
                $scope.saving = true;
            } else if ($scope.selectedUpdateOption === "New Type") {
                $scope.selectedUpdateOption = $scope.inventoryTypeFormUpdateSearch;
                data = {
                    inventoryName: self.inventory.inventoryName,
                    inventoryType: $scope.selectedUpdateOption,
                    inventoryDescription: self.inventory.inventoryDescription
                };

                $http.post("/api/gateway/inventories/types", { "type": $scope.selectedUpdateOption })
                    .then(function (resp) {
                        if (method === 'edit') {
                            $http.put('/api/gateway/inventories/' + inventoryId, data)
                                .then(function (response) {
                                    console.log(response);
                                    $state.go('inventories');
                                }, handleHttpError); }
                        else {
                                console.error("Invalid method:", method);

                        }
                        if (!inventoryId) {
                            console.error("Inventory ID is missing");
                        }
                    },handleHttpError)
                    .finally(function () { $scope.saving = false; });
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
                        }, handleHttpError)
                        .finally(function () { $scope.saving = false; });
                } else {
                    console.error("Invalid method:", method);
                }
                if (!inventoryId) {
                    console.error("Inventory ID is missing");
                }
                $scope.saving = false;
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

        function handleHttpError(response) {
            // Always log the raw thing for debugging
            try { console.error('HTTP error:', response); } catch (e) {}

            var data = (response && response.data);
            var status = (response && response.status);
            var statusText = (response && response.statusText) || '';

            // Try to normalize data if it's a string JSON
            if (typeof data === 'string') {
                // if it's JSON text, try parse; else treat as plain message
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    // plain text string response
                    var plain = data.trim();
                    if (plain) {
                        alert(plain);
                        return;
                    }
                    data = {}; // continue with object path
                }
            }
            data = data || {};

            // Common server shapes we might see
            var violations = data.violations || data.constraintViolations || [];
            var detailsArr = Array.isArray(data.details) ? data.details : [];
            var errorsArr  = Array.isArray(data.errors)  ? data.errors  : [];

            // Build fieldText from any array-ish error shapes
            function mapErr(e) {
                if (typeof e === 'string') return e;
                var field = e.field || e.path || e.parameter || e.property || '';
                var msg   = e.defaultMessage || e.message || e.reason || e.detail || e.title || '';
                var asStr = msg || JSON.stringify(e);
                return field ? (field + ': ' + asStr) : asStr;
            }
            var fieldText = []
                .concat(errorsArr.map(mapErr))
                .concat(detailsArr.map(mapErr))
                .concat(Array.isArray(violations) ? violations.map(mapErr) : [])
                .filter(Boolean)
                .join('\r\n');

            // Choose a base message with lots of fallbacks
            var baseMsg =
                data.message ||
                data.error_description ||
                data.errorMessage ||
                data.error ||
                data.title ||
                data.detail ||
                (typeof data === 'object' && Object.keys(data).length === 0 ? '' : JSON.stringify(data)) ||
                (status ? ('HTTP ' + status + (statusText ? (' ' + statusText) : '')) : 'Request failed');

            alert(fieldText ? (baseMsg + '\r\n' + fieldText) : baseMsg);
        }
    }]);
