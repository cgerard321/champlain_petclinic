'use strict';

angular.module('ownerList', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('owners', {
      parent: 'app',
      url: '/owners-pagination?page&size&ownerId&firstName&lastName&phoneNumber&city',
      template: '<owner-list></owner-list>',
      controller: 'OwnerListController',
      controllerAs: 'vm',
    });
  },
]);
