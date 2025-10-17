'use strict';

angular.module('inventoriesProductDetailsInfo')
  .controller('InventoriesProductDetailsInfoController', [
    '$http', '$stateParams',
    function ($http, $stateParams) {
      var self = this;
      self.loading = true;
      self.error = null;
      self.product = null;
      self.inventory = null;

      var inventoryId = $stateParams.inventoryId;
      var productId = $stateParams.productId;

      self.inventoryId = inventoryId;

      $http.get('api/gateway/inventories/' + inventoryId + '/products/' + productId)
        .then(function (resp) {
          self.product = resp.data;
          self.loading = false;
        })
        .catch(function (err) {
          console.error(err);
          self.error = 'Could not load product details.';
          self.loading = false;
        });
      $http.get('api/gateway/inventories/' + inventoryId)
          .then(function(resp){
              self.inventory = resp.data;
          });
    }
  ]);
