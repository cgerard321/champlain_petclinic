'use strict';

angular.module('inventoriesUpdateForm')
  .controller('InventoriesUpdateFormController', ["$http", '$state', '$stateParams', '$scope', function ($http, $state, $stateParams, $scope) {
    var self = this;
    var inventoryId = $stateParams.inventoryId || "";
    var method = 'edit';
    $scope.saving = false;

    $scope.existingInventoryNames = new Set();
    self.originalName = '';

    var inventoryLoaded = false;
    var typesLoaded = false;

    function syncSelectedType() {
      if (!inventoryLoaded || !typesLoaded) return;

      var invType = (self.inventory && self.inventory.inventoryType) ? String(self.inventory.inventoryType).trim() : '';

      if (invType && $scope.inventoryTypeUpdateOptions.indexOf(invType) !== -1) {
        $scope.selectedUpdateOption = invType;
        $scope.inventoryTypeFormUpdateSearch = '';
      } else if (invType) {
        $scope.selectedUpdateOption = 'New Type';
        $scope.inventoryTypeFormUpdateSearch = invType;
      } else {
        $scope.selectedUpdateOption = $scope.inventoryTypeUpdateOptions[0] || 'New Type';
        $scope.inventoryTypeFormUpdateSearch = '';
      }
    }

    $http.get('/api/gateway/inventories/' + inventoryId).then(function (resp) {
      self.inventory = resp.data || {};
      self.originalName = (self.inventory.inventoryName || '').toLowerCase().trim();
      inventoryLoaded = true;
      syncSelectedType();

      return $http.get('/api/gateway/inventories');
    }).then(function (listResp) {
      (listResp.data || []).forEach(function (inv) {
        if (inv && inv.inventoryName) {
          $scope.existingInventoryNames.add(inv.inventoryName.toLowerCase().trim());
        }
      });
      if ($scope.checkNameUpdate) $scope.checkNameUpdate();
    }, handleHttpError);

    $scope.inventoryTypeFormUpdateSearch = "";
    $scope.inventoryTypeUpdateOptions = ["New Type"];
    $http.get("/api/gateway/inventories/types").then(function (typesResp) {
      (typesResp.data || []).forEach(function (t) {
        if (t && t.type) $scope.inventoryTypeUpdateOptions.push(t.type);
      });
      typesLoaded = true;
      syncSelectedType();
    }, handleHttpError);

    $scope.checkNameUpdate = function () {
      if (!$scope.inventoryUpdateForm || !$scope.inventoryUpdateForm.inventoryName) return;
      var raw = (self.inventory && self.inventory.inventoryName) || '';
      var name = raw.toLowerCase().trim();
      var isDup = false;
      if (name) {
        isDup = $scope.existingInventoryNames.has(name) && name !== self.originalName;
      }
      $scope.inventoryUpdateForm.inventoryName.$setValidity('duplicate', !isDup);
    };

    self.submitUpdateInventoryForm = function () {
      if ($scope.inventoryUpdateForm && $scope.inventoryUpdateForm.$invalid) {
        angular.forEach($scope.inventoryUpdateForm.$error, function (fields) {
          (fields || []).forEach(function (f) { f.$setTouched(); });
        });
        return;
      }

      var data;
      $scope.saving = true;

      if ($scope.selectedUpdateOption === "New Type" && !($scope.inventoryTypeFormUpdateSearch || '').trim()) {
        alert("Please provide a name for the new type.");
        $scope.saving = false;
        return;
      }

      if ($scope.selectedUpdateOption === "New Type") {
        $scope.selectedUpdateOption = $scope.inventoryTypeFormUpdateSearch.trim();
        data = {
          inventoryName: (self.inventory.inventoryName || '').trim(),
          inventoryType: $scope.selectedUpdateOption,
          inventoryDescription: (self.inventory.inventoryDescription || '').trim()
        };

        $http.post("/api/gateway/inventories/types", { "type": $scope.selectedUpdateOption })
          .then(function () {
            if (method === 'edit') {
              return $http.put('/api/gateway/inventories/' + inventoryId, data);
            } else {
              console.error("Invalid method:", method);
              return Promise.reject(new Error("Invalid method"));
            }
          }, handleHttpError)
          .then(function () { $state.go('inventories'); }, handleHttpError)
          .finally(function () { $scope.saving = false; });
      } else {
        data = {
          inventoryName: (self.inventory.inventoryName || '').trim(),
          inventoryType: $scope.selectedUpdateOption,
          inventoryDescription: (self.inventory.inventoryDescription || '').trim()
        };
        if (method === 'edit') {
          $http.put('/api/gateway/inventories/' + inventoryId, data)
            .then(function () { $state.go('inventories'); }, handleHttpError)
            .finally(function () { $scope.saving = false; });
        } else {
          console.error("Invalid method:", method);
          $scope.saving = false;
        }
      }
    };

    $scope.updateOptionUpdate = function() {
      var searchLowerCase = ($scope.inventoryTypeFormUpdateSearch || '').toLowerCase();
      $scope.selectedUpdateOption = $scope.inventoryTypeUpdateOptions[0];
      for (var i = 0; i < $scope.inventoryTypeUpdateOptions.length; i++) {
        var optionLowerCase = $scope.inventoryTypeUpdateOptions[i].toLowerCase();
        if (optionLowerCase.indexOf(searchLowerCase) !== -1) {
          $scope.selectedUpdateOption = $scope.inventoryTypeUpdateOptions[i];
          break;
        }
      }
    };

    function handleHttpError(response) {
      try { console.error('HTTP error:', response); } catch (e) {}
      var data = (response && response.data);
      var status = (response && response.status);
      var statusText = (response && response.statusText) || '';
      if (typeof data === 'string') {
        try { data = JSON.parse(data); }
        catch (e) { var plain = data.trim(); if (plain) { alert(plain); return; } data = {}; }
      }
      data = data || {};
      var violations = data.violations || data.constraintViolations || [];
      var detailsArr = Array.isArray(data.details) ? data.details : [];
      var errorsArr  = Array.isArray(data.errors)  ? data.errors  : [];
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
        .concat(Array.isArray(violations) ? violations.map(mapErr) : [])
        .filter(Boolean)
        .join('\r\n');
      var baseMsg =
        data.message || data.error_description || data.errorMessage || data.error || data.title || data.detail ||
        (typeof data === 'object' && Object.keys(data).length === 0 ? '' : JSON.stringify(data)) ||
        (status ? ('HTTP ' + status + (statusText ? (' ' + statusText) : '')) : 'Request failed');
      alert(fieldText ? (baseMsg + '\r\n' + fieldText) : baseMsg);
    }
  }]);
