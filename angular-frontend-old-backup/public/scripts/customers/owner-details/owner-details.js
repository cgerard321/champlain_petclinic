'use strict';

angular.module('ownerDetails', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('ownerDetails', {
      parent: 'app',
      url: '/owners/details/:ownerId',
      params: { ownerId: null },
      template: '<owner-details></owner-details>',
    });
    // .state('petOwnerDetails', {
    //     parent: 'app',
    //     url: '/owners/:ownerId/pets/:petId/',
    //     params: {ownerId: null, petId: null},
    //     template: '<owner-details></owner-details>'
    // }
  },
]);
