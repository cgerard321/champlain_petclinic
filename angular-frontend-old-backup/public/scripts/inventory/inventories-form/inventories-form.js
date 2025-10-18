'use strict';

angular.module('inventoriesForm', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('inventoriesNew', {
      parent: 'app',
      url: '/inventories/new',
      template: '<inventories-form></inventories-form>',
    });
  },
]);
