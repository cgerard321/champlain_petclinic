'use strict';

angular.module('petForm').controller('PetFormController', [
  '$http',
  '$state',
  '$stateParams',
  '$filter',
  '$q',
  function ($http, $state, $stateParams, $filter, $q) {
    var self = this;
    var ownerId = $stateParams.ownerId || 0;
    var petId = $stateParams.petId || 0;

    // Helper to get pet type name
    self.getPetTypeName = function (petTypeId) {
      switch (petTypeId) {
        case '1':
          return 'Cat';
        case '2':
          return 'Dog';
        case '3':
          return 'Lizard';
        case '4':
          return 'Snake';
        case '5':
          return 'Bird';
        case '6':
          return 'Hamster';
        default:
          return 'Unknown';
      }
    };

    // Initialize
    self.pet = {};
    self.showModal = false;

    // Load types
    $http.get('api/gateway/owners/petTypes').then(function (resp) {
      self.types = resp.data;
    });

    // Load pet & owner info
    $q.all([
      $http.get('api/gateway/pets/' + petId),
      $http.get('api/gateway/owners/' + ownerId),
    ])
      .then(function (responses) {
        var petData = responses[0].data;
        petData.birthDate = new Date(petData.birthDate);

        var ownerData = responses[1].data;
        petData.owner = ownerData.firstName + ' ' + ownerData.lastName;

        self.pet = petData;
        self.checked = false;
      })
      .catch(function (error) {
        console.error('Error loading pet/owner details:', error);
      });

    // Open modal
    self.submit = function () {
      self.showModal = true;
    };

    // Cancel modal
    self.cancelModal = function () {
      self.showModal = false;
    };

    // Confirm modal (submit form)
    self.confirmModal = function () {
      self.showModal = false;

      var data = {
        petId: self.pet.petId,
        name: self.pet.name,
        birthDate: new Date(self.pet.birthDate).toISOString(),
        ownerId: self.pet.ownerId,
        petTypeId: self.pet.petTypeId,
        weight: self.pet.weight,
        isActive: self.pet.isActive,
      };

      $http
        .put('api/gateway/pets/' + self.pet.petId, data)
        .then(function () {
          $state.go('petDetails', { petId: self.pet.petId });
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
