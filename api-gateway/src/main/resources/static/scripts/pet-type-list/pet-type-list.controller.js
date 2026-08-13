"use strict";

angular.module("petTypeList").controller("PetTypeListController", [
  "$http",
  "$stateParams",
  "$scope",
  "$state",
  function ($http, $stateParams, $scope, $state) {
    var self = this;

    self.petTypes = [];
    self.showAddForm = false;
    self.newPetType = { name: "", petTypeDescription: "" };

    self.toggleAddForm = function () {
      self.showAddForm = !self.showAddForm;
      if (!self.showAddForm) {
        self.newPetType = { name: "", petTypeDescription: "" };
      }
    };

    self.addPetType = function () {
      var lettersOnly = /^[a-zA-Z\s]+$/;

      if (
        !lettersOnly.test(self.newPetType.name) ||
        !lettersOnly.test(self.newPetType.petTypeDescription)
      ) {
        alert("Name and Description must contain letters and spaces only.");
        return;
      }

      if (!self.newPetType.name || !self.newPetType.petTypeDescription) {
        alert("Name and Description are required.");
        return;
      }

      $http.post("api/gateway/owners/petTypes", self.newPetType).then(
        function () {
          alert("Pet type added successfully!");
          self.showAddForm = false;
          self.newPetType = { name: "", petTypeDescription: "" };
          loadDefaultData();
        },
        function (error) {
          alert(
            "Failed to add pet type: " + (error.data.message || "Unknown error")
          );
        }
      );
    };

    self.editingPetType = null;
    self.editForm = {};

    self.editPetType = function (petType) {
      self.editingPetType = petType.petTypeId;
      self.editForm = {
        name: petType.name,
        petTypeDescription: petType.petTypeDescription,
      };
    };

    self.savePetType = function (petTypeId) {
      $http.put("api/gateway/owners/petTypes/" + petTypeId, self.editForm).then(
        function () {
          alert("Pet type updated successfully!");
          loadDefaultData();
          self.cancelEdit();
        },
        function (error) {
          alert(
            "Failed to update pet type: " +
              (error.data.message || "Unknown error")
          );
        }
      );
    };

    self.cancelEdit = function () {
      self.editingPetType = null;
      self.editForm = {};
    };

    self.deletePetType = function (petTypeId) {
      let isConfirmed = confirm(
        "Are you sure you want to delete this pet type?"
      );
      if (isConfirmed) {
        $http.delete("api/gateway/owners/petTypes/" + petTypeId).then(
          function () {
            alert("Pet type deleted successfully!");
            loadDefaultData();
          },
          function (error) {
            alert(
              "Failed to delete pet type: " +
                (error.data.message || "Unknown error")
            );
          }
        );
      }
    };

    self.currentPage = $stateParams.page || 0;
    self.pageSize = $stateParams.size || 5;
    self.currentPageOnSite = parseInt(self.currentPage) + 1;

    self.petTypeId = null;
    self.name = null;
    self.description = null;
    self.selectedSize = null;

    self.searchActive = false;
    self.allPetTypes = [];

    loadDefaultData();

    function loadDefaultData() {
      fetch("api/gateway/owners/petTypes")
        .then((response) => response.text())
        .then((text) => {
          self.allPetTypes = text
            .split("data:")
            .map((payload) => {
              try {
                if (payload.trim() === "") return null;
                return JSON.parse(payload);
              } catch (err) {
                console.error("Can't parse JSON: " + err);
                return null;
              }
            })
            .filter((data) => data !== null);
          self.petTypes = self.allPetTypes;

          self.totalItems = self.allPetTypes.length;
          self.totalPages = Math.ceil(
            self.totalItems / parseInt(self.pageSize)
          );

          applyPagination();
          updateCurrentPageOnSite();
          $scope.$apply();
        });
    }

    function applyPagination() {
      var startIndex = self.currentPage * parseInt(self.pageSize);
      var endIndex = startIndex + parseInt(self.pageSize);
      self.petTypes = self.allPetTypes.slice(startIndex, endIndex);
    }

    self.searchPetTypesByPaginationAndFilters = function (
      currentPage = 0,
      prevOrNextPressed = false
    ) {
      self.selectedSize = document.getElementById("sizeInput").value;

      if (!prevOrNextPressed) {
        self.petTypeId = document.getElementById("petTypeIdInput").value;
        self.name = document.getElementById("nameInput").value;
        self.description = document.getElementById("descriptionInput").value;

        if (
          !self.petTypeId &&
          !self.name &&
          !self.description &&
          !self.selectedSize
        ) {
          alert("Oops! It seems like you forgot to enter any filter criteria.");
          return;
        }
      }

      self.searchActive = true;

      var filteredPetTypes = self.allPetTypes;

      if (self.petTypeId) {
        filteredPetTypes = filteredPetTypes.filter(function (petType) {
          return (
            petType.petTypeId &&
            petType.petTypeId.toString().includes(self.petTypeId)
          );
        });
      }

      if (self.name) {
        filteredPetTypes = filteredPetTypes.filter(function (petType) {
          return (
            petType.name &&
            petType.name.toLowerCase().includes(self.name.toLowerCase())
          );
        });
      }

      if (self.description) {
        filteredPetTypes = filteredPetTypes.filter(function (petType) {
          return (
            petType.petTypeDescription &&
            petType.petTypeDescription
              .toLowerCase()
              .includes(self.description.toLowerCase())
          );
        });
      }

      if (self.selectedSize) {
        self.pageSize = self.selectedSize;
      }

      if (!prevOrNextPressed) {
        self.currentPage = 0;
      }

      self.totalItems = filteredPetTypes.length;
      self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));

      var startIndex = self.currentPage * parseInt(self.pageSize);
      var endIndex = startIndex + parseInt(self.pageSize);
      self.petTypes = filteredPetTypes.slice(startIndex, endIndex);

      updateCurrentPageOnSite();
    };

    self.clearInputAndResetDefaultData = function () {
      document.getElementById("petTypeIdInput").value = "";
      document.getElementById("nameInput").value = "";
      document.getElementById("descriptionInput").value = "";
      document.getElementById("sizeInput").selectedIndex = 0;

      self.currentPage = 0;
      self.pageSize = 5;
      self.petTypeId = null;
      self.name = null;
      self.description = null;
      self.selectedSize = null;
      self.searchActive = false;

      self.petTypes = self.allPetTypes;
      self.totalItems = self.allPetTypes.length;
      self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));
      applyPagination();
      updateCurrentPageOnSite();

      alert("All filters have been cleared successfully.");
    };

    self.goNextPage = function () {
      if (parseInt(self.currentPage) + 1 < self.totalPages) {
        var currentPageInt = parseInt(self.currentPage) + 1;
        self.currentPage = currentPageInt.toString();
        updateCurrentPageOnSite();

        if (self.searchActive) {
          self.searchPetTypesByPaginationAndFilters(currentPageInt, true);
        } else {
          applyPagination();
        }
      }
    };

    self.goPreviousPage = function () {
      if (self.currentPage - 1 >= 0) {
        var currentPageInt = parseInt(self.currentPage) - 1;
        self.currentPage = currentPageInt.toString();
        updateCurrentPageOnSite();

        if (self.searchActive) {
          self.searchPetTypesByPaginationAndFilters(currentPageInt, true);
        } else {
          applyPagination();
        }
      }
    };

    function updateCurrentPageOnSite() {
      self.currentPageOnSite = parseInt(self.currentPage) + 1;
    }
  },
]);
