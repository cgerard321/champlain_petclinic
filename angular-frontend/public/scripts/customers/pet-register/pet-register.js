'use strict';

angular.module('petRegister', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('petRegister', {
      parent: 'app',
      url: '/owners/{ownerId}/pets/register',
      param: { ownerId: null },
      template: '<pet-register></pet-register>',
    });
  },
]);
