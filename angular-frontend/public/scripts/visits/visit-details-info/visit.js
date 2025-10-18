'use strict';

angular.module('visit', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('visitDetails', {
      parent: 'app',
      url: '/visit/:visitId/details',
      template: '<visit></visit>',
    });
  },
]);
