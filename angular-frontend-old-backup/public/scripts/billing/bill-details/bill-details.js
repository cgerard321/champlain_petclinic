'use strict';

angular.module('billDetails', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('billDetails', {
      parent: 'app',
      url: '/bills/details/:billId/owner/:ownerId',
      template: '<bill-details></bill-details>',
    });
  },
]);
