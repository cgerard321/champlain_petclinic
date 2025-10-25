'use strict';

angular.module('inventoriesForm')
  .controller('InventoriesFormController', ["$http", '$state', '$stateParams', '$scope', function ($http, $state, $stateParams, $scope) {
    var self = this;
    console.log("State params: " + $stateParams)
    $scope.saving = false;

    $scope.existingInventoryNames = new Set();
    $http.get("api/gateway/inventories").then(function (resp) {
      (resp.data || []).forEach(function (inv) {
        if (inv && inv.inventoryName) {
          $scope.existingInventoryNames.add(inv.inventoryName.toLowerCase().trim());
        }
      });
    }, handleHttpError);

    $scope.checkNameAdd = function () {
      if (!$scope.inventoryForm || !$scope.inventoryForm.inventoryName) return;
      var raw = (self.inventory && self.inventory.inventoryName) || '';
      var name = raw.toLowerCase().trim();
      var isDup = !!name && $scope.existingInventoryNames.has(name);
      $scope.inventoryForm.inventoryName.$setValidity('duplicate', !isDup);
    };

      $scope.inventoryTypeFormSearch = "";
      $scope.inventoryTypeOptions = ["New Type"];
      $scope.selectedOption = $scope.inventoryTypeOptions[0];

      (function initTypesSSE() {
          var es = new EventSource('api/gateway/inventories/types');
          var seen = new Set();              // to dedupe types coming over time
          var idleTimer = null;
          var IDLE_MS = 1000;

          function bumpIdle() {
              if (idleTimer) clearTimeout(idleTimer);
              idleTimer = setTimeout(function () {
                  try { es.close(); } catch (_) {}
              }, IDLE_MS);
          }

          function addType(t) {
              if (!t) return;
              // accept either string or object { type } / { inventoryType }
              var label = (typeof t === 'string') ? t : (t.type || t.inventoryType);
              if (!label || seen.has(label)) return;
              seen.add(label);
              $scope.$evalAsync(function () {
                  $scope.inventoryTypeOptions.push(label);
                  if (!$scope.selectedOption) $scope.selectedOption = $scope.inventoryTypeOptions[0];
              });
          }

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

          es.onerror = function () { /* let browser auto-reconnect */ };

          $scope.$on('$destroy', function () {
              if (idleTimer) clearTimeout(idleTimer);
              try { es.close(); } catch (_) {}
          });
      })();

    self.submitInventoryForm = function () {

      if ($scope.inventoryForm && $scope.inventoryForm.$invalid) {
        angular.forEach($scope.inventoryForm.$error, function (fields) {
          (fields || []).forEach(function (f) { f.$setTouched(); });
        });
        return;
      }

      var data;
      $scope.saving = true;

      if ($scope.selectedOption === "New Type" && !$scope.inventoryTypeFormSearch.trim()) {
        alert("Please provide a name for the new type.");
        $scope.saving = false;
        return;
      }

      if ($scope.selectedOption === "New Type") {
        $scope.selectedOption = $scope.inventoryTypeFormSearch.trim();

        data = {
          inventoryName: (self.inventory.inventoryName || '').trim(),
          inventoryType: $scope.selectedOption,
          inventoryDescription: (self.inventory.inventoryDescription || '').trim()
        };

        $http.post("api/gateway/inventories/types", {"type": $scope.selectedOption})
          .then(function () {
            return $http.post("api/gateway/inventories", data);
          }, handleHttpError)
          .then(function () { $state.go('inventories'); }, handleHttpError)
          .finally(function () { $scope.saving = false; });
      } else {
        data = {
          inventoryName: (self.inventory.inventoryName || '').trim(),
          inventoryType: $scope.selectedOption,
          inventoryDescription: (self.inventory.inventoryDescription || '').trim()
        };
        $http.post("api/gateway/inventories", data)
          .then(function () { $state.go('inventories'); }, handleHttpError)
          .finally(function () { $scope.saving = false; });
      }
    };

    $scope.updateOption = function() {
      var searchLowerCase = ($scope.inventoryTypeFormSearch || '').toLowerCase();
      $scope.selectedOption = $scope.inventoryTypeOptions[0];
      for (var i = 0; i < $scope.inventoryTypeOptions.length; i++) {
        var optionLowerCase = $scope.inventoryTypeOptions[i].toLowerCase();
        if (optionLowerCase.indexOf(searchLowerCase) !== -1) {
          $scope.selectedOption = $scope.inventoryTypeOptions[i];
          break;
        }
      }
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

      var errorsArr  = Array.isArray(data.errors)  ? data.errors  : [];
      var detailsArr = Array.isArray(data.details) ? data.details : [];
      var violations = Array.isArray(data.violations || data.constraintViolations)
        ? (data.violations || data.constraintViolations) : [];

      function mapErr(e) {
        if (typeof e === 'string') return e;
        var field = e.field || e.path || e.parameter || e.property || '';
        var msg   = e.defaultMessage || e.message || e.reason || e.detail || e.title || '';
        var asStr = msg || JSON.stringify(e);
        return field ? (field + ': ' + asStr) : asStr;
      }

      var fieldText = []
        .concat(errorsArr.map(mapErr))
        .concat(detailsArr.map(mapErr))
        .concat(violations.map(mapErr))
        .filter(Boolean)
        .join('\r\n');

      var baseMsg =
        data.message ||
        data.error_description ||
        data.errorMessage ||
        data.error ||
        data.title ||
        data.detail ||
        (typeof data === 'object' && Object.keys(data).length ? JSON.stringify(data) : '') ||
        (status ? ('HTTP ' + status + (statusText ? (' ' + statusText) : '')) : 'Request failed');

      alert(fieldText ? (baseMsg + '\r\n' + fieldText) : baseMsg);
    }
  }]);
