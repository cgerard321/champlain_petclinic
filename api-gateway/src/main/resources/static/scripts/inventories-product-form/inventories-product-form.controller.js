'use strict';

angular.module('inventoriesProductForm')
  .controller('InventoriesProductFormController', [
    "$http", '$state', '$stateParams', '$scope', 'InventoryService',
    function ($http, $state, $stateParams, $scope, InventoryService) {

      var self = this;
      $scope.saving = false;

      var inventoryId = $stateParams.inventoryId || (InventoryService && InventoryService.getInventoryId && InventoryService.getInventoryId());
      $scope.inventoryId = inventoryId; // used by hidden field if needed
      if (!inventoryId) { console.warn('No inventoryId found for product creation.'); }

      self._nameSet = new Set();

      // Fetch existing product names once; afterwards validation is client-side
      if (inventoryId) {
        $http.get('/api/gateway/inventories/' + inventoryId + '/products')
          .then(function (resp) {
            var list = Array.isArray(resp.data) ? resp.data : [];
            list.forEach(function (p) {
              if (p && p.productName != null) {
                self._nameSet.add(p.productName.toString().trim().toLowerCase());
              }
            });
          }, function (err) {
            try { console.warn('Could not prefetch product names for uniqueness check.', err); } catch(e){}
          });
      }

      self.onNameChange = function (ngModelCtrl) {
        if (!ngModelCtrl || !self.product) return;
        var current = (self.product.productName || '').toString().trim().toLowerCase();
        var isDuplicate = current && self._nameSet.has(current);
        ngModelCtrl.$setValidity('dupname', !isDuplicate);
      };

      // Submit handler
      self.submitProductForm = function () {
        // Block submit if invalid and reveal messages
        if ($scope.productForm && $scope.productForm.$invalid) {
          angular.forEach($scope.productForm.$error, function (fields) {
            (fields || []).forEach(function (f) { f.$setTouched(); });
          });
          return;
        }

        if (!inventoryId) { alert('Inventory ID is missing.'); return; }

        var data  = {
          productName:        (self.product.productName || '').trim(),
          productDescription: (self.product.productDescription || '').trim(),
          productPrice:       parseFloat(self.product.productPrice),
          productQuantity:    parseInt(self.product.productQuantity, 10),
          productSalePrice:   parseFloat(self.product.productSalePrice)
        };

        $scope.saving = true;
        $http.post('/api/gateway/inventories/' + inventoryId + '/products', data)
          .then(function () {
            $state.go('inventoriesProduct', { inventoryId: inventoryId });
          }, handleHttpError)
          .finally(function(){ $scope.saving = false; });
      };

      function handleHttpError(response) {
        try { console.error('HTTP error:', response); } catch (e) {}
        var data = (response && response.data);
        var status = (response && response.status);
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
