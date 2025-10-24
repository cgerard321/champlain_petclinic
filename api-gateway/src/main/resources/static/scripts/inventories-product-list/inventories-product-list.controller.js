'use strict';

angular.module('inventoriesProductList')
  .controller('InventoriesProductController', [
    '$http', '$scope', '$stateParams', '$window', '$location', 'InventoryService',
    function ($http, $scope, $stateParams, $window, $location, InventoryService) {
      var self = this;
      var inventoryId;
      const pageSize = 15;
      self.currentPage = $stateParams.page || 0;
      self.pageSize = $stateParams.size || pageSize;
      self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
      self.baseUrl = "api/gateway/inventories/" + $stateParams.inventoryId + "/products-pagination?page=" + self.currentPage + "&size=" + self.pageSize;
      self.baseURLforTotalNumberOfProductsByFiltering = "api/gateway/inventories/" + $stateParams.inventoryId + "/products-count";
      self.lastParams = {
        productName: '',
        productQuantity: '',
        productPrice: '',
        productSalePrice: ''
      };
      $scope.inventory = {};

      $http.get('api/gateway/inventories/' + $stateParams.inventoryId).then(function (resp) {
        $scope.inventory = resp.data;
      });
      fetchProductList();

      // ===== helper to get the logged-in user's email (stored at login) =====
      function getCurrentEmail() {
        try {
          var stored = localStorage.getItem('auth.user');
          if (stored) {
            var obj = JSON.parse(stored);
            return obj && obj.email ? obj.email : null;
          }
        } catch (e) {}
        return null;
      }

      // Fallback: if email is stored as raw string, support that too (non-breaking)
      (function patchEmailFallback(){
        if (!getCurrentEmail.__patched) {
          var _orig = getCurrentEmail;
          getCurrentEmail = function(){
            var v = _orig.call(this);
            if (v) return v;
            try { return localStorage.getItem('email') || null; } catch(e) { return null; }
          };
          getCurrentEmail.__patched = true;
        }
      })();

      // ---- NEW: resolve role label from localStorage to personalize prompt ----
      function getRoleLabel() {
        var roles = [];
        try {
          var stored = localStorage.getItem('auth.user');
          if (stored) {
            var obj = JSON.parse(stored);
            if (obj && Array.isArray(obj.roles)) {
              roles = obj.roles.map(function (r) { return (r.name || r).toString(); });
            }
          }
          if (!roles.length) {
            var raw = localStorage.getItem('roles');
            if (raw) roles = raw.split(',').map(function (s) { return s.trim(); });
          }
        } catch (e) {}
        var hasAdmin  = roles.some(function (r){ return /admin/i.test(r); });
        var hasInvMgr = roles.some(function (r){ return /(inventory[_\s-]?manager)/i.test(r); });
        if (hasAdmin && !hasInvMgr) return 'ADMIN';
        if (!hasAdmin && hasInvMgr) return 'INVENTORY MANAGER';
        return 'ADMIN or INVENTORY MANAGER';
      }

      // ---- NEW: tiny vanilla masked-password dialog (no deps) ----
      function showPasswordDialog(roleLabel) {
        return new Promise(function (resolve, reject) {
          var overlay = document.createElement('div');
          overlay.style.position = 'fixed';
          overlay.style.inset = '0';
          overlay.style.background = 'rgba(0,0,0,0.5)';
          overlay.style.zIndex = '9999';
          overlay.style.display = 'flex';
          overlay.style.alignItems = 'center';
          overlay.style.justifyContent = 'center';

          var box = document.createElement('div');
          box.style.background = '#fff';
          box.style.borderRadius = '6px';
          box.style.padding = '16px';
          box.style.width = '360px';
          box.style.maxWidth = '90vw';
          box.style.boxShadow = '0 10px 24px rgba(0,0,0,.2)';
          box.innerHTML =
            '<h4 style="margin:0 0 8px;">Confirm deletion</h4>' +
            '<p style="margin:0 0 12px;">Enter <strong>' + roleLabel + '</strong> password to confirm:</p>' +
            '<input type="password" id="pwField" class="form-control" ' +
            'style="width:100%;padding:8px 10px;margin:0 0 12px;box-sizing:border-box;" placeholder="Password" />' +
            '<div style="text-align:right;">' +
            '  <button id="pwCancel" class="btn btn-default" style="margin-right:8px;">Cancel</button>' +
            '  <button id="pwOk" class="btn btn-primary">OK</button>' +
            '</div>';

          overlay.appendChild(box);
          document.body.appendChild(overlay);

          var input = box.querySelector('#pwField');
          var okBtn = box.querySelector('#pwOk');
          var cancelBtn = box.querySelector('#pwCancel');

          function cleanup() {
            if (overlay && overlay.parentNode) overlay.parentNode.removeChild(overlay);
            document.removeEventListener('keydown', onKey);
          }
          function onKey(e) {
            if (e.key === 'Escape') { cleanup(); reject('cancelled'); }
            if (e.key === 'Enter')  { if (input.value) { var v=input.value; cleanup(); resolve(v); } }
          }

          okBtn.addEventListener('click', function () {
            if (!input.value) return;
            var val = input.value; cleanup(); resolve(val);
          });
          cancelBtn.addEventListener('click', function () { cleanup(); reject('cancelled'); });
          document.addEventListener('keydown', onKey);
          setTimeout(function(){ input.focus(); }, 0);
        });
      }

      // Temporarily whitelist current route so a 401 from the login probe won't purge/redirect
      function withTempWhitelist(run) {
        var added = false;
        try {
          var key = $location.path().substring(1); // same logic as interceptor
          if (typeof whiteList !== 'undefined' && !whiteList.has(key)) {
            whiteList.add(key);
            added = true;
          }
        } catch (e) {}
        return Promise.resolve()
          .then(run)
          .finally(function () {
            try {
              if (added) {
                var key = $location.path().substring(1);
                whiteList.delete(key);
              }
            } catch (e) {}
          });
      }

      // ---- REFINED: Prompt for masked password (role-specific) & verify; no logout on 401 ----
      function promptAndVerify(maxTries) {
        var tries = 0;
        var email = getCurrentEmail();
        if (!email) return Promise.reject(new Error('no-email'));
        var roleLabel = getRoleLabel();

        function ask() {
          return showPasswordDialog(roleLabel).then(function (password) {
            if (!password) throw new Error('cancelled');
            return withTempWhitelist(function () {
              return $http.post('/api/gateway/users/login', { email: email, password: password });
            });
          }).then(function () {
            return true;
          }).catch(function (err) {
            if (err === 'cancelled' || err === 'cancel') throw new Error('cancelled');
            if (err && err.status === 401) {
              tries++;
              alert('Wrong password. Please try again.');
              if (tries < maxTries) return ask();
              throw new Error('max-tries');
            }
            throw err || new Error('auth-failed');
          });
        }
        return ask();
      }

      // ===== refined delete: warn -> (retrying) verify -> delete =====
      $scope.deleteProduct = function (product) {
        if (!confirm('Warning: Deleting this product cannot be undone. Continue?')) {
          return;
        }
        promptAndVerify(3)
          .then(function () { return proceedToDelete(product); })
          .catch(function (e) {
            if (e && (e.message === 'cancelled' || e.message === 'max-tries')) return;
          });
      };

      // Keeping undo function to avoid breaking other parts
      $scope.undoDelete = function(product) {
        product.isTemporarilyDeleted = false;
      };

      function proceedToDelete(product) {
        return $http.delete('api/gateway/inventories/' + product.inventoryId + '/products/' + product.productId)
          .then(successCallback, errorCallback);

        function showNotification(message) {
          const notificationElement = document.getElementById('notification');
          notificationElement.innerHTML = message;
          notificationElement.style.display = 'block';
          setTimeout(() => { notificationElement.style.display = 'none'; }, 5000);
        }

        function successCallback(response) {
          $scope.errors = [];
          console.log(response, 'res');
          setTimeout(() => {
            showNotification(product.productName + " has been deleted successfully!");
            setTimeout(() => { location.reload(); }, 1000);
          }, 1000);
        }
        function errorCallback(error) {
          try { alert(error.data.errors || error.data.message || 'Delete failed'); }
          catch (e) { alert('Delete failed'); }
          console.log(error, 'Data is inaccessible.');
        }
      }

      $scope.clearQueries = function (){
        self.lastParams.productName = '';
        self.lastParams.productQuantity = '';
        self.lastParams.productPrice = '';
        self.lastParams.productSalePrice = '';
        $scope.productName = '';
        $scope.productQuantity = '';
        $scope.productPrice = '';
        $scope.productSalePrice = '';
        $scope.searchProduct('', '', '', '');
      };

      $scope.searchProduct = function(productName, productQuantity, productPrice, productSalePrice) {
        var inventoryId = $stateParams.inventoryId;
        var queryString = '';
        resetDefaultValues();

        if (productName && productName !== '') {
          queryString += "productName=" + productName;
          self.lastParams.productName = productName;
        }
        if (productQuantity && productQuantity !== '') {
          if (queryString !== '') queryString += "&";
          queryString += "productQuantity=" + productQuantity;
          self.lastParams.productQuantity = productQuantity;
        }
        if (productPrice && productPrice !== '') {
          if (queryString !== '') queryString += "&";
          queryString += "productPrice=" + productPrice;
          self.lastParams.productPrice = productPrice;
        }
        if (productSalePrice && productSalePrice !== '') {
          if (queryString !== '') queryString += "&";
          queryString += "productSalePrice=" + productSalePrice;
          self.lastParams.productSalePrice = productSalePrice;
        }

        var apiUrl = "api/gateway/inventories/" + inventoryId + "/products";
        if (queryString !== '') apiUrl += "?" + queryString;

        let response = [];
        $http.get(apiUrl)
          .then(function(resp) {
            resp.data.forEach(function (current) {
              current.productPrice = current.productPrice.toFixed(2);
              current.productSalePrice = current.productSalePrice.toFixed(2);
              response.push(current);
            });
            self.inventoryProductList = response;
            loadTotalItem(productName, productPrice, productQuantity);
            InventoryService.setInventoryId(inventoryId);
          })
          .catch(function(error) {
            if (error.status === 404) {
              self.inventoryProductList = [];
              self.currentPage = 0;
              updateActualCurrentPageShown();
            } else {
              alert('An error occurred: ' + error.statusText);
            }
          });
      };

      function fetchProductList(productName, productPrice, productQuantity, productSalePrice) {
        if (productName || productPrice || productQuantity) {
          self.lastParams.productName = productName;
          self.lastParams.productPrice = productPrice;
          self.lastParams.productQuantity = productQuantity;
          self.lastParams.productSalePrice = productSalePrice;
          self.searchProduct(productName, productPrice, productQuantity, productSalePrice);
        } else {
          self.lastParams.productSalePrice = null;
          self.lastParams.productName = null;
          self.lastParams.productPrice = null;
          self.lastParams.productQuantity = null;
          let inventoryId = $stateParams.inventoryId;
          let response = [];
          $http.get('api/gateway/inventories/' + $stateParams.inventoryId + '/products-pagination?page=' + self.currentPage + '&size=' + self.pageSize)
            .then(function (resp) {
              resp.data.forEach(function (current) {
                current.productPrice = current.productPrice.toFixed(2);
                current.productSalePrice = current.productSalePrice.toFixed(2);
                response.push(current);
              });
              self.inventoryProductList = response;
              inventoryId = $stateParams.inventoryId;
              loadTotalItem(productName, productPrice, productQuantity, productSalePrice);
              InventoryService.setInventoryId(inventoryId);
              if (resp.data.length === 0) {
                console.log("The inventory is empty!");
              }
            }).catch(function (error) {
              console.error('An error occurred:', error);
            });
        }
      }

      self.nextPage = function () {
        if (parseInt(self.currentPage) + 1 < self.totalPages) {
          var currentPageInt = parseInt(self.currentPage) + 1;
          self.currentPage = currentPageInt.toString();
          updateActualCurrentPageShown();
          fetchProductList(self.lastParams.productName, self.lastParams.productPrice, self.lastParams.productQuantity, self.lastParams.productSalePrice);
        }
      };

      self.previousPage = function () {
        console.log("Previous page called, current self.page: ");
        if (self.currentPage - 1 >= 0) {
          var currentPageInt = parseInt(self.currentPage) - 1;
          self.currentPage = currentPageInt.toString();
          updateActualCurrentPageShown();
          console.log("Called the previous page and the new current page " + self.currentPage);
          fetchProductList(self.lastParams.productName, self.lastParams.productPrice, self.lastParams.productQuantity, self.lastParams.productSalePrice);
        }
      };

      function resetDefaultValues() {
        self.currentPage = 0;
        self.pageSize = pageSize;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        self.lastParams = {
          productName: '',
          productQuantity: '',
          productPrice: ''
        };
      }

      function updateActualCurrentPageShown() {
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        console.log(self.currentPage);
      }

      function loadTotalItem(productName, productPrice, productQuantity, productSalePrice) {
        var query = '';
        if (productName) {
          if (query === '') query += '?productName=' + productName;
        }
        if (productPrice){
          if (query === '') query += '?productPrice=' + productPrice;
          else query += '&productPrice=' + productPrice;
        }
        if (productQuantity){
          if (query === '') query += '?productQuantity=' + productQuantity;
          else query += '&productQuantity=' + productQuantity;
        }
        if (productSalePrice){
          if (query === '') query += '?productSalePrice=' + productSalePrice;
          else query += '&productSalePrice=' + productSalePrice;
        }
        $http.get('api/gateway/inventories/' + $stateParams.inventoryId + '/products-count' + query)
          .then(function (resp) {
            self.totalItems = resp.data;
            self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));
          });
      }
    }
  ]);
