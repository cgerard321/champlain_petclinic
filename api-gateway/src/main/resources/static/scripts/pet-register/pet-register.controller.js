"use strict";

angular.module("petRegister").controller("PetRegisterController", [
  "$http",
  "$state",
  "$stateParams",
  "$scope",
  function ($http, $state, $stateParams, $scope) {
    var self = this;
    var ownerId = $stateParams.ownerId || 0;
    console.log("Pet Register Controller loaded");

    // Initialize
    self.pet = {};
    self.showModal = false;
    self.loadingPetTypes = true;
    self.types = [];

    // Load pet types
    fetch("api/gateway/owners/petTypes")
      .then((response) => response.text())
      .then((text) => {
        self.types = text
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
        self.loadingPetTypes = false;
        $scope.$apply();
      })
      .catch((error) => {
        console.error("Error loading pet types:", error);
        self.loadingPetTypes = false;
        self.types = [];
      });

    // Generate a UUID for the new pet
    function generateUUID() {
      return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(
        /[xy]/g,
        function (c) {
          var r = (Math.random() * 16) | 0,
            v = c === "x" ? r : (r & 0x3) | 0x8;
          return v.toString(16);
        }
      );
    }
    self.randomUUID = generateUUID();
    console.log("Generated UUID:", self.randomUUID);

    // Open modal before submitting
    self.submitPetForm = function () {
      self.showModal = true;
    };

    // Cancel modal
    self.cancelModal = function () {
      self.showModal = false;
    };

    // Confirm modal and submit form
    self.confirmModal = function () {
      self.showModal = false;

      var petType = {
        id: self.pet.type.id,
        name: self.pet.type.name,
      };

      var data = {
        ownerId: ownerId,
        petId: self.randomUUID,
        name: self.pet.name,
        birthDate: self.pet.birthDate,
        type: petType.id,
        isActive: "true",
        weight: self.pet.weight,
      };

      $http
        .post("api/gateway/owners/" + ownerId + "/pets", data)
        .then(function () {
          console.log("Pet registered successfully");
          $state.go("ownerDetails", { ownerId: ownerId });
        })
        .catch(function (response) {
          var error = response.data;
          error.errors = error.errors || [];
          alert(
            error.error +
              "\r\n" +
              error.errors
                .map(function (e) {
                  return e.field + ": " + e.defaultMessage;
                })
                .join("\r\n")
          );
        });
    };
  },
]);
