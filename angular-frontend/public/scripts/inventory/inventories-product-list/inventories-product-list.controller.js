'use strict';

angular
  .module('inventoriesProductList')
  .controller('InventoriesProductController', [
    '$http',
    '$scope',
    '$stateParams',
    '$window',
    'InventoryService',
    function ($http, $scope, $stateParams, $window, InventoryService) {
      var self = this;
      // var inventoryId;
      const pageSize = 15;
      self.currentPage = $stateParams.page || 0;
      self.pageSize = $stateParams.size || pageSize;
      self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
      self.baseUrl =
        'api/gateway/inventories/' +
        $stateParams.inventoryId +
        '/products-pagination?page=' +
        self.currentPage +
        '&size=' +
        self.pageSize;
      self.baseURLforTotalNumberOfProductsByFiltering =
        'api/gateway/inventories/' +
        $stateParams.inventoryId +
        '/products-count';
      self.lastParams = {
        productName: '',
        productQuantity: '',
        productPrice: '',
        productSalePrice: '',
      };
      $scope.inventory = {};

      $http
        .get('api/gateway/inventories/' + $stateParams.inventoryId)
        .then(function (resp) {
          $scope.inventory = resp.data;
        });
      fetchProductList();

      $scope.deleteProduct = function (product) {
        let ifConfirmed = confirm(
          'Are you sure you want to remove this inventory?'
        );
        if (ifConfirmed) {
          // Step 1: Mark as temporarily deleted on frontend.
          product.isTemporarilyDeleted = true;

          // Display an Undo button for say, 5 seconds.
          setTimeout(function () {
            if (product.isTemporarilyDeleted) {
              // If it's still marked as deleted after 5 seconds, proceed with actual deletion.
              proceedToDelete(product);
            }
          }, 5000); // 5 seconds = 5000ms.
        }
      };

      $scope.undoDelete = function (product) {
        product.isTemporarilyDeleted = false;
        // Hide the undo button.
      };

      function proceedToDelete(product) {
        if (!product.isTemporarilyDeleted) return; // In case the user clicked undo just before the timeout.

        $http
          .delete(
            'api/gateway/inventories/' +
              product.inventoryId +
              '/products/' +
              product.productId
          )
          .then(successCallback, errorCallback);

        function showNotification(message) {
          const notificationElement = document.getElementById('notification');
          notificationElement.innerHTML = message;
          notificationElement.style.display = 'block';

          setTimeout(() => {
            notificationElement.style.display = 'none';
          }, 5000); // Hide after 5 seconds
        }

        function successCallback() {
          $scope.errors = [];
          // console.log removed(response, 'res');

          // After deletion, wait for a short moment (e.g., 1 second) before showing the notification
          setTimeout(() => {
            showNotification(
              product.productName + ' has been deleted successfully!'
            );
            // Then, after displaying the notification for 5 seconds, reload the page
            setTimeout(() => {
              location.reload();
            }, 1000);
          }, 1000); // Wait for 1 second before showing notification
        }
        function errorCallback(error) {
          // If the error message is nested under 'data.errors' in your API response:
          alert(error.data.errors);
          // console.log removed(error, 'Data is inaccessible.');
        }
      }

      $scope.clearQueries = function () {
        self.lastParams.productName = '';
        self.lastParams.productQuantity = '';
        self.lastParams.productPrice = '';
        self.lastParams.productSalePrice = '';

        // Clear the input fields
        $scope.productName = '';
        $scope.productQuantity = '';
        $scope.productPrice = '';
        $scope.productSalePrice = '';
        $scope.searchProduct('', '', '', '');
      };

      $scope.searchProduct = function (
        productName,
        productQuantity,
        productPrice,
        productSalePrice
      ) {
        var inventoryId = $stateParams.inventoryId;
        var queryString = '';
        resetDefaultValues();

        if (productName && productName !== '') {
          queryString += 'productName=' + productName;
          self.lastParams.productName = productName;
        }

        if (productQuantity && productQuantity !== '') {
          if (queryString !== '') {
            queryString += '&';
          }
          queryString += 'productQuantity=' + productQuantity;
          self.lastParams.productQuantity = productQuantity;
        }

        if (productPrice && productPrice !== '') {
          if (queryString !== '') {
            queryString += '&';
          }
          queryString += 'productPrice=' + productPrice;
          self.lastParams.productPrice = productPrice;
        }

        if (productSalePrice && productSalePrice !== '') {
          if (queryString !== '') {
            queryString += '&';
          }
          queryString += 'productSalePrice=' + productSalePrice;
          self.lastParams.productSalePrice = productSalePrice;
        }

        var apiUrl = 'api/gateway/inventories/' + inventoryId + '/products';
        if (queryString !== '') {
          apiUrl += '?' + queryString;
        }
        let response = [];
        $http
          .get(apiUrl)
          .then(function (resp) {
            resp.data.forEach(function (current) {
              current.productPrice = current.productPrice.toFixed(2);
              current.productSalePrice = current.productSalePrice.toFixed(2);
              response.push(current);
            });
            self.inventoryProductList = response;
            loadTotalItem(productName, productPrice, productQuantity);
            InventoryService.setInventoryId(inventoryId);
          })
          .catch(function (error) {
            if (error.status === 404) {
              //alert('Product not found.');
              self.inventoryProductList = [];
              self.currentPage = 0;
              updateActualCurrentPageShown();
            } else {
              alert('An error occurred: ' + error.statusText);
            }
          });
      };

      $scope.deleteAllProducts = function () {
        let varIsConf = confirm(
          'Are you sure you want to delete all products for this inventory?'
        );
        if (varIsConf) {
          let inventoryId = $stateParams.inventoryId; // Retrieve the inventoryId from the appropriate location

          $http
            .delete('api/gateway/inventories/' + inventoryId + '/products')
            .then(
              function () {
                alert('All products for this inventory have been deleted!');
                fetchProductList();
              },
              function (error) {
                alert(error.data.errors);
                // console.log removed(error, 'Failed to delete all products.');
              }
            );
        }
      };

      function fetchProductList(
        productName,
        productPrice,
        productQuantity,
        productSalePrice
      ) {
        if (productName || productPrice || productQuantity) {
          self.lastParams.productName = productName;
          self.lastParams.productPrice = productPrice;
          self.lastParams.productQuantity = productQuantity;
          self.lastParams.productSalePrice = productSalePrice;
          self.searchProduct(
            productName,
            productPrice,
            productQuantity,
            productSalePrice
          );
        } else {
          self.lastParams.productSalePrice = null;
          self.lastParams.productName = null;
          self.lastParams.productPrice = null;
          self.lastParams.productQuantity = null;
          let inventoryId = $stateParams.inventoryId;
          let response = [];
          $http
            .get(
              'api/gateway/inventories/' +
                $stateParams.inventoryId +
                '/products-pagination?page=' +
                self.currentPage +
                '&size=' +
                self.pageSize
            )
            .then(function (resp) {
              resp.data.forEach(function (current) {
                current.productPrice = current.productPrice.toFixed(2);
                current.productSalePrice = current.productSalePrice.toFixed(2);
                response.push(current);
              });
              self.inventoryProductList = response;
              inventoryId = $stateParams.inventoryId;
              loadTotalItem(
                productName,
                productPrice,
                productQuantity,
                productSalePrice
              );
              InventoryService.setInventoryId(inventoryId);
              if (resp.data.length === 0) {
                // Handle if inventory is empty
                // console.log removed('The inventory is empty!');
              }
            })
            .catch(function (error) {
              console.error('An error occurred:', error);
            });
        }
      }
      self.nextPage = function () {
        if (parseInt(self.currentPage) + 1 < self.totalPages) {
          var currentPageInt = parseInt(self.currentPage) + 1;
          self.currentPage = currentPageInt.toString();
          updateActualCurrentPageShown();
          //refresh product list
          fetchProductList(
            self.lastParams.productName,
            self.lastParams.productPrice,
            self.lastParams.productQuantity,
            self.lastParams.productSalePrice
          );
        }
      };

      self.previousPage = function () {
        // console.log removed('Previous page called, current self.page: ');
        if (self.currentPage - 1 >= 0) {
          var currentPageInt = parseInt(self.currentPage) - 1;
          self.currentPage = currentPageInt.toString();
          updateActualCurrentPageShown();
          // console.log removed('Called the previous page and the new current page ' + self.currentPage);
          // Refresh the owner's list with the new page size
          fetchProductList(
            self.lastParams.productName,
            self.lastParams.productPrice,
            self.lastParams.productQuantity,
            self.lastParams.productSalePrice
          );
        }
      };
      function resetDefaultValues() {
        self.currentPage = 0;
        self.pageSize = pageSize;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        self.lastParams = {
          productName: '',
          productQuantity: '',
          productPrice: '',
        };
      }

      function updateActualCurrentPageShown() {
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        // console.log removed(self.currentPage);
      }
      function loadTotalItem(
        productName,
        productPrice,
        productQuantity,
        productSalePrice
      ) {
        var query = '';
        if (productName) {
          if (query === '') {
            query += '?productName=' + productName;
          }
        }
        if (productPrice) {
          if (query === '') {
            query += '?productPrice=' + productPrice;
          } else {
            query += '&productPrice=' + productPrice;
          }
        }
        if (productQuantity) {
          if (query === '') {
            query += '?productQuantity=' + productQuantity;
          } else {
            query += '&productQuantity=' + productQuantity;
          }
        }
        if (productSalePrice) {
          if (query === '') {
            query += '?productSalePrice=' + productSalePrice;
          } else {
            query += '&productSalePrice=' + productSalePrice;
          }
        }
        $http
          .get(
            'api/gateway/inventories/' +
              $stateParams.inventoryId +
              '/products-count' +
              query
          )
          .then(function (resp) {
            self.totalItems = resp.data;
            self.totalPages = Math.ceil(
              self.totalItems / parseInt(self.pageSize)
            );
          });
      }
    },
  ]);
