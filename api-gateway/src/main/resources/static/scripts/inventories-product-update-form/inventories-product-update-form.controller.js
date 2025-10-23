'use strict';

angular.module('inventoriesProductUpdateForm')
    .controller('InventoriesProductUpdateFormController', ["$http", '$state', '$stateParams', '$scope', 'InventoryService', function ($http, $state, $stateParams, $scope, InventoryService) {
        var self = this;
        var product = {}
        var method = $stateParams.method;
        $scope.saving = false;
        var inventoryId = InventoryService.getInventoryId();
        console.log("InventoryId: " + inventoryId);
        var productId = $stateParams.productId;
        console.log("ProductId: " + productId)
        // post request to create a new product



        $http.get('/api/gateway/inventories/' + inventoryId + '/products/' + productId).then(function (resp) {
            self.product = resp.data;
        },handleHttpError);

        self.submitProductUpdateForm = function () {

            if ($scope.inventoryProductUpdateForm && $scope.inventoryProductUpdateForm.$invalid) {
        angular.forEach($scope.inventoryProductUpdateForm.$error, function (fields) {       
          (fields || []).forEach(function (f) { f.$setTouched(); });                        
        });                                                                                  
        return;                                                                             
      }
            if (!inventoryId) {
                console.error("Inventory ID is missing");
                alert("Inventory ID is missing.");
                return;
            }
            if (!productId) {
                console.error("Product ID is missing");
                alert("Product ID is missing.");
                return;
            }

            var data  = {
                productName: self.product.productName,
                productDescription: self.product.productDescription,
                productPrice: self.product.productPrice,
                productQuantity: self.product.productQuantity,
                productSalePrice: self.product.productSalePrice
            }
            $scope.saving = true;
            $http.put('/api/gateway/inventories/' + inventoryId + '/products/' + productId, data)
                .then(function (response) {
                    console.log(response);
                    $state.go('inventoriesProduct', {inventoryId: inventoryId});
                },handleHttpError)
                .finally(function () { $scope.saving = false; });
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

            // Possible arrays the backend may use
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