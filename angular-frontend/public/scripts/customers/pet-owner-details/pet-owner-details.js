'use strict';

angular.module('petOwnerDetails', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('petDetails', {
      parent: 'app',
      url: '/owners/:ownerId/pets/:petId/',
      params: { ownerId: null, petId: null },
      template: '<pet-owner-details></pet-owner-details>',
    });
  },
]);
