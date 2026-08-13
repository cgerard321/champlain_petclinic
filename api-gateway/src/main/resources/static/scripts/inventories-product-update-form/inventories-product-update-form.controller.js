'use strict';

angular.module('inventoriesProductUpdateForm')
  .controller('InventoriesProductUpdateFormController', [
    "$http", '$state', '$stateParams', '$scope', 'InventoryService',
    function ($http, $state, $stateParams, $scope, InventoryService) {

      var self = this;
      $scope.saving = false;

      var inventoryId = InventoryService.getInventoryId();
      var productId   = $stateParams.productId;

      self._nameSet = new Set();        // all existing product names (lowercased)
      self._originalNameLower = null;   // the original name of the product being edited


      // Fetch current product
      $http.get('/api/gateway/inventories/' + inventoryId + '/products/' + productId)
        .then(function (resp) {
          self.product = resp.data || {};
          self._originalNameLower = (self.product.productName || '').toString().trim().toLowerCase();
        }, handleHttpError);

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

      self.onNameChange = function (ngModelCtrl) {
        if (!ngModelCtrl || !self.product) return;

        var current = (self.product.productName || '').toString().trim().toLowerCase();

        var isDuplicate = self._nameSet.has(current) && current !== self._originalNameLower;

        ngModelCtrl.$setValidity('dupname', !isDuplicate);
      };

      self.submitProductUpdateForm = function () {
        if ($scope.inventoryProductUpdateForm && $scope.inventoryProductUpdateForm.$invalid) {
          angular.forEach($scope.inventoryProductUpdateForm.$error, function (fields) {
            (fields || []).forEach(function (f) { f.$setTouched(); });
          });
          return;
        }

        if (!inventoryId) { alert("Inventory ID is missing."); return; }
        if (!productId)   { alert("Product ID is missing.");   return; }

        var data = {
          productName:        (self.product.productName || '').trim(),
          productDescription: (self.product.productDescription || '').trim(),
          productPrice:       parseFloat(self.product.productPrice),
          productQuantity:    parseInt(self.product.productQuantity, 10),
          productSalePrice:   parseFloat(self.product.productSalePrice)
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
