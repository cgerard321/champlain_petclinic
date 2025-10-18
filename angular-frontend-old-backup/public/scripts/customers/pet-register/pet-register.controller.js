'use strict';

angular.module('petRegister').controller('PetRegisterController', [
  '$http',
  '$state',
  '$stateParams',
  function ($http, $state, $stateParams) {
    var self = this;
    var ownerId = $stateParams.ownerId || 0;
    // console.log removed('Pet Register Controller loaded');

    // Initialize
    self.pet = {};
    self.showModal = false;

    // Load pet types
    $http.get('api/gateway/owners/petTypes').then(function (resp) {
      self.types = resp.data;
    });

    // Generate a UUID for the new pet
    function generateUUID() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
        /[xy]/g,
        function (c) {
          var r = (Math.random() * 16) | 0,
            v = c === 'x' ? r : (r & 0x3) | 0x8;
          return v.toString(16);
        }
      );
    }
    self.randomUUID = generateUUID();
    // console.log removed('Generated UUID:', self.randomUUID);

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
        isActive: 'true',
        weight: self.pet.weight,
      };

      $http
        .post('api/gateway/owners/' + ownerId + '/pets', data)
        .then(function () {
          // console.log removed('Pet registered successfully');
          $state.go('ownerDetails', { ownerId: ownerId });
        })
        .catch(function (response) {
          var error = response.data;
          error.errors = error.errors || [];
          alert(
            error.error +
              '\r\n' +
              error.errors
                .map(function (e) {
                  return e.field + ': ' + e.defaultMessage;
                })
                .join('\r\n')
          );
        });
    };
  },
]);
