'use strict';

angular.module('inventoryProductList')
  .component('inventoryProductItem', {
    templateUrl: 'scripts/inventory-product-list/inventory-product-item.html',
    controller: ['$http', '$stateParams', function($http, $stateParams) {
      const vm = this;

      vm.loading = true;
      vm.error = null;
      vm.product = null;
      vm.inventoryId = $stateParams.inventoryId;
      vm.productId = $stateParams.productId;

      vm.$onInit = function () {
        // GET /api/gateway/inventories/{inventoryId}/products/{productId}
        $http.get(`api/gateway/inventories/${vm.inventoryId}/products/${vm.productId}`)
          .then(resp => { vm.product = resp.data; })
          .catch(err => { vm.error = err; })
          .finally(() => { vm.loading = false; });
      };
    }]
  });
