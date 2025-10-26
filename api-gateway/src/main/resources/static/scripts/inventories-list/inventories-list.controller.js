'use strict';

angular.module('inventoriesList')
  .controller('InventoriesListController', [
    '$http', '$scope', '$stateParams', '$state', '$location',
    function ($http, $scope, $stateParams, $state, $location) {
      var self = this;
      self.currentPage = $stateParams.page = 0;
      self.listSize = $stateParams.size = 10;
      self.realPage = parseInt(self.currentPage) + 1;

      self.inventoryList = [];
      $scope.inventoryTypeOptions = [];

      var numberOfPage;
      var name;
      var code;
      var type;
      var desc;

      var esSearch = null;



      getInventoryList();

      $scope.inventoryTypeOptions = [];

      (function initTypesSSE () {
        if ($scope.inventoryTypeOptions.length) return;

        var es = new EventSource('api/gateway/inventories/types');
        var seen = new Set();
        var idleTimer = null;
        var IDLE_MS = 1000;
        var reconnects = 0;
        var MAX_RECONNECTS = 2;

        function bumpIdle() {
          if (idleTimer) clearTimeout(idleTimer);
          idleTimer = setTimeout(function () {
            try { es.close(); } catch (e) {}
          }, IDLE_MS);
        }

        function addType(t) {
          if (!t) return;
          var label = (typeof t === 'string') ? t : (t.type || t.inventoryType);
          if (!label || seen.has(label)) return;
          seen.add(label);
          $scope.$evalAsync(function () { $scope.inventoryTypeOptions.push(label); });
        }

        es.onopen = function () { reconnects = 0; };

        es.onmessage = function (e) {
          if (!e.data || e.data === 'heartbeat' || e.data === ':') { bumpIdle(); return; }
          try {
            var payload = JSON.parse(e.data);
            if (Array.isArray(payload)) payload.forEach(addType);
            else addType(payload);
          } catch (_e) {
            addType(e.data);
          }
          bumpIdle();
        };

        es.onerror = function () {
          if (es.readyState !== EventSource.OPEN) {
            reconnects += 1;
            if (reconnects > MAX_RECONNECTS) {
              try { es.close(); } catch (e) {}
            }
          }
        };

        $scope.$on('$destroy', function () {
          if (idleTimer) clearTimeout(idleTimer);
          try { es.close(); } catch (e) {}
        });
      })();

      (function initInventoriesSSE () {
        var esInv = new EventSource('api/gateway/inventories');
        var idleTimer = null;
        var INV_IDLE_MS = 2000;
        var reconnects = 0;
        var MAX_RECONNECTS = 2;

        function bumpIdle() {
          if (idleTimer) clearTimeout(idleTimer);
          idleTimer = setTimeout(function () {
            try { esInv.close(); } catch (e) {}
          }, INV_IDLE_MS);
        }

        function upsertById(list, item, idKey) {
          if (!item || !list || !Array.isArray(list)) return;
          var idx = list.findIndex(function (x) { return x[idKey] === item[idKey]; });
          if (idx >= 0) list[idx] = item; else list.unshift(item);
        }

        esInv.onopen = function () { reconnects = 0; };

        esInv.onmessage = function (e) {
          if (!e.data || e.data === 'heartbeat' || e.data === ':') { bumpIdle(); return; }
          try {
            var payload = JSON.parse(e.data);
            $scope.$evalAsync(function () {
              if (!Array.isArray(self.inventoryList)) self.inventoryList = [];

              if (Array.isArray(payload)) {
                payload.forEach(function (it) { upsertById(self.inventoryList, it, 'inventoryId'); });
              } else {
                upsertById(self.inventoryList, payload, 'inventoryId');
              }
            });
          } catch (_e) {
          }
          bumpIdle();
        };


        esInv.onerror = function () {
          if (esInv.readyState !== EventSource.OPEN) {
            reconnects += 1;
            if (reconnects > MAX_RECONNECTS) {
              try { esInv.close(); } catch (e) {}
            }
          }
        };

        $scope.$on('$destroy', function () {
          if (idleTimer) clearTimeout(idleTimer);
          try { esInv.close(); } catch (e) {}
        });
      })();

      $scope.$on('$destroy', function () {
        try { if (esSearch) esSearch.close(); } catch (e) {}
      });


      //clear inventory queries
      $scope.clearQueries = function (){
        $scope.inventoryName = '';
        $scope.inventoryCode = '';
        $scope.inventoryType = '';
        $scope.inventoryDescription = '';
        $scope.searchInventory('', '', '');
      };

      //search by inventory field
      $scope.searchInventory = function (inventoryCode, inventoryName, inventoryType, inventoryDescription){
        getInventoryList(inventoryCode, inventoryName, inventoryType, inventoryDescription);
      };

      function getInventoryList(inventoryCode, inventoryName, inventoryType, inventoryDescription) {
        $state.transitionTo('inventories', {page: self.currentPage, size: self.listSize}, {notify: false});
        var prevCode = code || '';
        var prevName = name || '';
        var prevType = type || '';
        var prevDesc = desc || '';

        // Build the next filters from args (use locals first)
        var nextCode = '';
        var nextName = '';
        var nextType = '';
        var nextDesc = '';
        var queryString = '';

        if (inventoryCode != null && inventoryCode !== '') {
          nextCode = (inventoryCode || '').toUpperCase();
          queryString += "inventoryCode=" + encodeURIComponent(nextCode);
        }
        if (inventoryName != null && inventoryName !== '') {
          nextName = inventoryName;
          if (queryString !== '') queryString += "&";
          queryString += "inventoryName=" + encodeURIComponent(nextName);
        }
        if (inventoryType) {
          nextType = inventoryType;
          if (queryString !== '') queryString += "&";
          queryString += "inventoryType=" + encodeURIComponent(nextType);
        }
        if (inventoryDescription) {
          nextDesc = inventoryDescription;
          if (queryString !== '') queryString += "&";
          queryString += "inventoryDescription=" + encodeURIComponent(nextDesc);
        }

        // Decide if filters actually changed (only then reset to page 0)
        var isFilterChange =
            (nextCode || '') !== prevCode ||
            (nextName || '') !== prevName ||
            (nextType || '') !== prevType ||
            (nextDesc || '') !== prevDesc;

        if (isFilterChange) {
          self.currentPage = 0;
          self.realPage = 1;
        }

        // Now update the globals to the new filter values
        code = nextCode;
        name = nextName;
        type = nextType;
        desc = nextDesc;

        var base = "api/gateway/inventories?page=" + self.currentPage + "&size=" + self.listSize;
        var url  = (queryString !== '') ? (base + "&" + queryString) : base;


        try {
          if (esSearch) esSearch.close();
        } catch (e) {
        }
        esSearch = new EventSource(url);

        var order = [];                            // array of ids in desired order
        var byId = Object.create(null);           // id -> item
        var hasId = Object.prototype.hasOwnProperty;

        var idleTimer = null;
        var INV_IDLE_MS = 2000;
        var reconnects = 0;
        var MAX_RECONNECTS = 2;

        function bumpIdle() {
          if (idleTimer) clearTimeout(idleTimer);
          idleTimer = setTimeout(function () {
            try {
              if (esSearch) esSearch.close();
            } catch (e) {
            }
          }, INV_IDLE_MS);
        }

        function ensureInOrder(id) {
          if (!hasId.call(byId, id)) {
            order.push(id);                        // append new ids at the end
          }
        }

        esSearch.onopen = function () {
          reconnects = 0;
        };

        esSearch.onmessage = function (e) {
          if (!e.data || e.data === 'heartbeat' || e.data === ':') {
            bumpIdle();
            return;
          }
          try {
            var payload = JSON.parse(e.data);

            if (Array.isArray(payload)) {
              // First array from server defines the canonical order for this page
              if (order.length === 0) {
                for (var i = 0; i < payload.length; i++) {
                  var it = payload[i];
                  if (!it) continue;
                  var id = it.inventoryId;
                  if (id == null) continue;
                  if (!hasId.call(byId, id)) order.push(id);
                  byId[id] = it;
                }
              } else {
                // Subsequent arrays (rare) – update items but keep the existing order
                for (var j = 0; j < payload.length; j++) {
                  var it2 = payload[j];
                  if (!it2) continue;
                  var id2 = it2.inventoryId;
                  if (id2 == null) continue;
                  ensureInOrder(id2);
                  byId[id2] = it2;
                }
              }
            } else {
              // Single item upsert – update value, keep order (append if new)
              var one = payload;
              if (one && one.inventoryId != null) {
                ensureInOrder(one.inventoryId);
                byId[one.inventoryId] = one;
              }
            }

            // Rebuild list in stable order
            var tmp = [];
            for (var k = 0; k < order.length; k++) {
              var oid = order[k];
              if (hasId.call(byId, oid)) tmp.push(byId[oid]);
            }

            $scope.$evalAsync(function () {
              numberOfPage = Math.ceil(tmp.length / 10);
              self.inventoryList = tmp;
              try {
                arr = tmp;
              } catch (e) {
              }
            });
          } catch (_e) {
            // ignore malformed chunks
          }
          bumpIdle();
        };

        esSearch.onerror = function () {
          if (esSearch.readyState !== EventSource.OPEN) {
            reconnects += 1;
            if (reconnects > MAX_RECONNECTS) {
              try {
                esSearch.close();
              } catch (e) {
              }
              alert('An error occurred: failed to stream inventories.');
            }
          }
        };
      }

      // ---- helpers (local-only; prevent logout on wrong creds) ----
      function getCurrentEmail() {
        try {
          var stored = localStorage.getItem('auth.user');
          if (stored) {
            var obj = JSON.parse(stored);
            if (obj && obj.email) return obj.email;
          }
          var raw = localStorage.getItem('email');
          return raw || null;
        } catch (e) { return null; }
      }

      // ---- NEW: role label for personalized prompt ----
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

      // Temporarily whitelist current route so 401 from login probe won't purge/redirect
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

      // prompt user and verify creds with up to N retries; never logs the user out
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

      // ---- refined delete (confirm -> retrying verify -> delete) ----
      $scope.deleteInventory = function(inventory) {
        var ifConfirmed = confirm('Warning: Deleting this inventory cannot be undone. Continue?');
        if (!ifConfirmed) return;

        promptAndVerify(3)
          .then(function () { return proceedToDelete(inventory); })
          .catch(function (e) {
            if (e && (e.message === 'cancelled' || e.message === 'max-tries')) return;
          });
      };

      // kept for compatibility with old UI (not used by refined flow)
      $scope.undoDelete = function(inventory) {
        inventory.isTemporarilyDeleted = false;
      };

      function proceedToDelete(inventory) {
        return $http.delete('api/gateway/inventories/' + inventory.inventoryId)
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
            showNotification(inventory.inventoryCode + " - " + inventory.inventoryName + " has been deleted successfully!");
            setTimeout(() => { location.reload(); }, 1000);
          }, 1000);
        }
        function errorCallback(error) {
          try { alert((error.data && (error.data.errors || error.data.message)) || 'Delete failed'); }
          catch (e) { alert('Delete failed'); }
          console.log(error, 'Data is inaccessible.');
        }
      }

      $scope.pageBefore = function () {
        if (self.currentPage - 1 >= 0){
          self.currentPage = (parseInt(self.currentPage) - 1).toString();
          self.realPage = parseInt(self.currentPage) + 1;
          getInventoryList(code, name, type, desc);
        }
      };

      $scope.pageAfter = function () {
        if (self.currentPage + 1 <= numberOfPage) {
          self.currentPage = (parseInt(self.currentPage) + 1).toString();
          self.realPage = parseInt(self.currentPage) + 1;
          getInventoryList(code, name, type, desc);
        }
      };

      $scope.inventoryType = '';
      $scope.$watch('inventoryType', function(newType, oldType) {
        if (newType !== oldType) {
          $scope.searchInventory($scope.inventoryCode || '', $scope.inventoryName || '', newType, $scope.inventoryDescription || '');
        }
      });
    }
  ]);
