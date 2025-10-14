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
            });
        if (!$scope.selectedOption) {
            $scope.selectedOption = $scope.inventoryTypeOptions[0];
        }
    }, handleHttpError);

      $scope.selectedOption = $scope.inventoryTypeOptions[0];


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
                $http.post("api/gateway/inventories/types", {"type": $scope.selectedOption})
                    .then(function () {
                        return $http.post("api/gateway/inventories", data);
                    }, handleHttpError)
                    .then(function (resp) {
                        console.log(resp)
                        $state.go('inventories');
                    }, handleHttpError);
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
                    }, handleHttpError);
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

        function handleHttpError(response) {
            try { console.error('HTTP error:', response); } catch (e) {}

            var data = response && response.data;
            var status = response && response.status;
            var statusText = (response && response.statusText) || '';

            // Normalize string bodies (plain text or JSON-as-string)
            if (typeof data === 'string') {
                try {
                    data = JSON.parse(data);
                } catch (e) {
                    var plain = data.trim();
                    if (plain) {
                        alert(plain);
                        return;
                    }
                    data = {};
                }
            }
            data = data || {};

            // Arrays the backend might use
            var errorsArr  = Array.isArray(data.errors)  ? data.errors  : [];
            var detailsArr = Array.isArray(data.details) ? data.details : [];
            var violations = Array.isArray(data.violations || data.constraintViolations)
                ? (data.violations || data.constraintViolations) : [];

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
                .concat(violations.map(mapErr))
                .filter(Boolean)
                .join('\r\n');

            var baseMsg =
                data.message ||
                data.error_description ||
                data.errorMessage ||
                data.error ||
                data.title ||
                data.detail ||
                (typeof data === 'object' && Object.keys(data).length ? JSON.stringify(data) : '') ||
                (status ? ('HTTP ' + status + (statusText ? (' ' + statusText) : '')) : 'Request failed');

            alert(fieldText ? (baseMsg + '\r\n' + fieldText) : baseMsg);
        }
    }]);














