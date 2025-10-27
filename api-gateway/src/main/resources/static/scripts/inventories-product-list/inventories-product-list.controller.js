'use strict';

angular.module('inventoriesProductList')
  .controller('InventoriesProductController', [
    '$http', '$scope', '$stateParams', '$window', '$location', 'InventoryService',
    function ($http, $scope, $stateParams, $window, $location, InventoryService) {
      var self = this;
      var inventoryId = $stateParams.inventoryId;
      const pageSize = 15;
      self.currentPage = $stateParams.page || 0;
      self.pageSize = $stateParams.size || pageSize;
      self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
      self.baseUrl = "api/gateway/inventories/" + $stateParams.inventoryId + "/products-pagination?page=" + self.currentPage + "&size=" + self.pageSize;
      self.baseURLforTotalNumberOfProductsByFiltering = "api/gateway/inventories/" + $stateParams.inventoryId + "/products-count";
      self.lastParams = {
        productName: '',
        productQuantity: '',
        minPrice: '',
        maxPrice: '',
        minSalePrice: '',
        maxSalePrice: ''
      };
      $scope.inventory = {};

      $http.get('api/gateway/inventories/' + $stateParams.inventoryId).then(function (resp) {
        $scope.inventory = resp.data;
      });
      fetchProductList();

      var esProducts = null;
      var currentESUrl = null;
      var openTimer = null;
      var idleTimer = null;
      var IDLE_MS = 2000;

      function closeProductsES() {
        if (idleTimer) { clearTimeout(idleTimer); idleTimer = null; }
        if (openTimer) { clearTimeout(openTimer); openTimer = null; }
        if (esProducts) { try { esProducts.close(); } catch(e) {} esProducts = null; }
        currentESUrl = null;
      }

      function bumpIdle() {
        if (idleTimer) clearTimeout(idleTimer);
        idleTimer = setTimeout(function () {
          try { if (esProducts) esProducts.close(); } catch (e) {}
        }, IDLE_MS);
      }

      function upsertById(list, item, idKey) {
        if (!item || !Array.isArray(list)) return;
        if (item.productPrice != null)     item.productPrice     = (+item.productPrice).toFixed(2);
        if (item.productSalePrice != null) item.productSalePrice = (+item.productSalePrice).toFixed(2);
        var idx = list.findIndex(function (x) { return x[idKey] === item[idKey]; });
        if (idx >= 0) list[idx] = item; else list.push(item);
      }

      function openProductsES(url) {
        // avoid reopening same active stream
        if (esProducts && currentESUrl === url && esProducts.readyState === 1) return;

        if (openTimer) clearTimeout(openTimer);
        openTimer = setTimeout(function () {
          closeProductsES();
          currentESUrl = url;
          self.inventoryProductList = []; // reset when opening a NEW stream
          esProducts = new EventSource(url);

          esProducts.onmessage = function (e) {
            if (!e.data || e.data === 'heartbeat' || e.data === ':') { bumpIdle(); return; }
            try {
              var payload = JSON.parse(e.data);
              $scope.$evalAsync(function () {
                if (Array.isArray(payload)) {
                  payload.forEach(function (p) { upsertById(self.inventoryProductList, p, 'productId'); });
                } else {
                  upsertById(self.inventoryProductList, payload, 'productId');
                }
              });
            } catch (_e) {
              // ignore non-JSON lines
            }
            bumpIdle();
          };

          esProducts.onerror = function () {
            // let browser auto-reconnect; do not open a new EventSource here
          };
        }, 150);
      }

      $scope.$on('$destroy', function () { closeProductsES(); });

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
        self.lastParams.minPrice = '';
        self.lastParams.maxPrice = '';
        self.lastParams.minSalePrice = '';
        self.lastParams.maxSalePrice = '';
        $scope.productName = '';
        $scope.productQuantity = '';
        $scope.minPrice = '';
        $scope.maxPrice = '';
        $scope.minSalePrice = '';
        $scope.maxSalePrice = '';
        $scope.searchProduct('', '', '', '', '', '');
      };

      $scope.searchProduct = function(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice) {
        if (productQuantity !== undefined && productQuantity !== null && productQuantity !== '' && productQuantity <= 0) {
          alert('Quantity cannot be 0 or negative. Please enter a valid quantity.');
          return;
        }

        if (minPrice !== undefined && minPrice !== null && minPrice !== '' && minPrice <= 0) {
          alert('Min Price cannot be 0 or negative. Please enter a valid min price.');
          return;
        }
        if (maxPrice !== undefined && maxPrice !== null && maxPrice !== '' && maxPrice <= 0) {
          alert('Max Price cannot be 0 or negative. Please enter a valid max price.');
          return;
        }
        if (minSalePrice !== undefined && minSalePrice !== null && minSalePrice !== '' && minSalePrice <= 0) {
          alert('Min Sale Price cannot be 0 or negative. Please enter a valid min sale price.');
          return;
        }
        if (maxSalePrice !== undefined && maxSalePrice !== null && maxSalePrice !== '' && maxSalePrice <= 0) {
          alert('Max Sale Price cannot be 0 or negative. Please enter a valid max sale price.');
          return;
        }

        if (
            minPrice !== undefined && minPrice !== null && minPrice !== '' &&
            maxPrice !== undefined && maxPrice !== null && maxPrice !== '' &&
            maxPrice < minPrice
        ) {
          alert('Max price must be larger than min price.');
          return;
        }
        if (
            minSalePrice !== undefined && minSalePrice !== null && minSalePrice !== '' &&
            maxSalePrice !== undefined && maxSalePrice !== null && maxSalePrice !== '' &&
            maxSalePrice < minSalePrice
        ) {
          alert('Max sale price must be larger than min sale price.');
          return;
        }

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
        if (minPrice && minPrice !== '') {
          if (queryString !== '') queryString += "&";
          queryString += "minPrice=" + minPrice;
          self.lastParams.minPrice = minPrice;
        }
        if (maxPrice && maxPrice !== '') {
          if (queryString !== '') queryString += "&";
          queryString += "maxPrice=" + maxPrice;
          self.lastParams.maxPrice = maxPrice;
        }

        if (minSalePrice && minSalePrice !== '') {
          if (queryString !== '') {
            queryString += "&";
          }
          queryString += "minSalePrice=" + minSalePrice;
          self.lastParams.minSalePrice = minSalePrice;
        }

        if (maxSalePrice && maxSalePrice !== '') {
          if (queryString !== '') {
            queryString += "&";
          }
          queryString += "maxSalePrice=" + maxSalePrice;
          self.lastParams.maxSalePrice = maxSalePrice;
        }
        var apiUrl = "api/gateway/inventories/" + inventoryId + "/products";
        if (queryString !== '') apiUrl += "?" + queryString;

        openProductsES(apiUrl);

        loadTotalItem(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice);
        InventoryService.setInventoryId(inventoryId);

      };

      function fetchProductList(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice) {
        if (productName || productQuantity || minPrice || maxPrice || minSalePrice || maxSalePrice) {
          self.lastParams.productName = productName;
          self.lastParams.productQuantity = productQuantity;
          self.lastParams.minPrice = minPrice;
          self.lastParams.maxPrice = maxPrice;
          self.lastParams.minSalePrice = minSalePrice;
          self.lastParams.maxSalePrice = maxSalePrice;
          self.searchProduct(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice)
        } else {
          self.lastParams.productName = null;
          self.lastParams.productQuantity = null;
          self.lastParams.minPrice = null;
          self.lastParams.maxPrice = null;
          self.lastParams.minSalePrice = null;
          self.lastParams.maxSalePrice = null;
          let inventoryId = $stateParams.inventoryId;
          var url = 'api/gateway/inventories/' + $stateParams.inventoryId + '/products-pagination?page=' + self.currentPage + '&size=' + self.pageSize;


          openProductsES(url);

          loadTotalItem(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice);
          InventoryService.setInventoryId(inventoryId);
        }
      }

      self.nextPage = function () {
        if (parseInt(self.currentPage) + 1 < self.totalPages) {
          var currentPageInt = parseInt(self.currentPage) + 1;
          self.currentPage = currentPageInt.toString();
          updateActualCurrentPageShown();
          fetchProductList(self.lastParams.productName, self.lastParams.productQuantity, self.lastParams.minPrice, self.lastParams.maxPrice, self.lastParams.minSalePrice, self.lastParams.maxSalePrice);
        }
      };

      self.previousPage = function () {
        console.log("Previous page called, current self.page: ");
        if (self.currentPage - 1 >= 0) {
          var currentPageInt = parseInt(self.currentPage) - 1;
          self.currentPage = currentPageInt.toString();
          updateActualCurrentPageShown();
          console.log("Called the previous page and the new current page " + self.currentPage);
          fetchProductList(self.lastParams.productName, self.lastParams.productQuantity, self.lastParams.minPrice, self.lastParams.maxPrice, self.lastParams.minSalePrice, self.lastParams.maxSalePrice);
        }
      };

      function resetDefaultValues() {
        self.currentPage = 0;
        self.pageSize = pageSize;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        self.lastParams = {
          productName: '',
          productQuantity: '',
          minPrice: '',
          maxPrice: '',
          minSalePrice: '',
          maxSalePrice: ''
        };
      }

      function updateActualCurrentPageShown() {
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        console.log(self.currentPage);
      }

      function loadTotalItem(productName, productQuantity, minPrice, maxPrice, minSalePrice, maxSalePrice) {
        var query = ''
        if (productName) {
          if (query === ''){
            query +='?productName='+productName
          }
        }
        if (productQuantity){
          if (query === ''){
            query +='?productQuantity='+productQuantity
          } else{
            query += '&productQuantity='+productQuantity
          }
        }
        if (minPrice){
          if (query === ''){
            query +='?minPrice='+minPrice
          } else{
            query += '&minPrice='+minPrice
          }
        }
        if (maxPrice){
          if (query === ''){
            query +='?maxPrice='+maxPrice
          } else{
            query += '&maxPrice='+maxPrice
          }
        }
        if (minSalePrice){
          if (query === ''){
            query +='?minSalePrice='+minSalePrice
          } else{
            query += '&minSalePrice='+minSalePrice
          }
        }
        if (maxSalePrice){
          if (query === ''){
            query +='?maxSalePrice='+maxSalePrice
          } else{
            query += '&maxSalePrice='+maxSalePrice
          }
        }
        $http.get('api/gateway/inventories/' + $stateParams.inventoryId + '/products-count' + query)
            .then(function (resp) {
              self.totalItems = resp.data;
              self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));
            });
      }
    }]);
