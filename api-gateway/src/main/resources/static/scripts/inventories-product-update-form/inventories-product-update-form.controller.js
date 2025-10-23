'use strict';

angular.module('inventoriesProductUpdateForm')
  .controller('InventoriesProductUpdateFormController', [
    "$http", '$state', '$stateParams', '$scope', 'InventoryService',
    function ($http, $state, $stateParams, $scope, InventoryService) {

      var self = this;
      $scope.saving = false;

      var inventoryId = InventoryService.getInventoryId();
      var productId = $stateParams.productId;

      $http.get('/api/gateway/inventories/' + inventoryId + '/products/' + productId)
        .then(function (resp) { self.product = resp.data; }, handleHttpError);

      self.submitProductUpdateForm = function () {
        // Block submit if form invalid; also reveal errors by touching fields
        if ($scope.inventoryProductUpdateForm && $scope.inventoryProductUpdateForm.$invalid) {
          angular.forEach($scope.inventoryProductUpdateForm.$error, function (fields) {
            (fields || []).forEach(function (f) { f.$setTouched(); });
          });
          return; // <-- NO HTTP CALL
        }

        if (!inventoryId) { alert("Inventory ID is missing."); return; }
        if (!productId) { alert("Product ID is missing."); return; }

        // Payload is valid here (all client checks passed)
        var data = {
          productName: (self.product.productName || '').trim(),
          productDescription: (self.product.productDescription || '').trim(),
          productPrice: parseFloat(self.product.productPrice),
          productQuantity: parseInt(self.product.productQuantity, 10),
          productSalePrice: parseFloat(self.product.productSalePrice)
        };

        $scope.saving = true;
        $http.put('/api/gateway/inventories/' + inventoryId + '/products/' + productId, data)
          .then(function () {
            $state.go('inventoriesProduct', { inventoryId: inventoryId });
          }, handleHttpError)
          .finally(function () { $scope.saving = false; });
      };

      function handleHttpError(response) {
        try { console.error('HTTP error:', response); } catch (e) {}
        var data = response && response.data;
        var status = response && response.status;
        var statusText = (response && response.statusText) || '';

        if (typeof data === 'string') {
          try { data = JSON.parse(data); }
          catch (e) {
            var plain = data.trim();
            if (plain) { alert(plain); return; }
            data = {};
          }
        }
        data = data || {};
        var msg =
          data.message || data.error || data.title || data.detail ||
          (status ? ('HTTP ' + status + (statusText ? (' ' + statusText) : '')) : 'Request failed');
        alert(msg);
      }
    }
  ]);
