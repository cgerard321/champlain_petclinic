'use strict';

angular
  .module('inventoriesProductForm')
  .controller('InventoriesProductFormController', [
    '$http',
    '$state',
    '$stateParams',
    '$scope',
    'InventoryService',
    function ($http, $state, $scope, $stateParams, InventoryService) {
      var self = this;
      // post request to create a new product
      self.submitProductForm = function () {
        var data = {
          productName: self.product.productName,
          productDescription: self.product.productDescription,
          productPrice: self.product.productPrice,
          productQuantity: self.product.productQuantity,
          productSalePrice: self.product.productSalePrice,
        };
        var inventoryId =
          $stateParams.inventoryId || InventoryService.getInventoryId();
        // console.log removed('InventoryId: ' + inventoryId);
        $http
          .post('/api/gateway/inventories/' + inventoryId + '/products', data)
          .then(
            function () {
              // console.log removed(response);
              $state.go('inventoriesProduct', { inventoryId: inventoryId });
            },
            function (response) {
              var data = (response && response.data) || {};
              var baseMsg =
                (typeof data === 'string' && data) ||
                data.message ||
                data.error ||
                (response && response.status
                  ? 'HTTP ' +
                    response.status +
                    ' ' +
                    (response.statusText || '')
                  : 'Request failed');
              var fieldErrors =
                (Array.isArray(data.errors) && data.errors) ||
                (Array.isArray(data.details) && data.details) ||
                data.fieldErrors ||
                [];

              var fieldText = '';
              if (Array.isArray(fieldErrors) && fieldErrors.length) {
                fieldText = fieldErrors
                  .map(function (e) {
                    if (typeof e === 'string') return e;
                    var field = e.field || e.path || e.parameter || '';
                    var msg =
                      e.defaultMessage ||
                      e.message ||
                      e.reason ||
                      JSON.stringify(e);
                    return field ? field + ': ' + msg : msg;
                  })
                  .join('\r\n');
              }

              alert(fieldText ? baseMsg + '\r\n' + fieldText : baseMsg);
              // ---------------------------------------------------
            }
          );
      };
    },
  ]);
