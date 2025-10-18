'use strict';

angular.module('productDetailsInfo', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('productsDetails', {
      parent: 'app',
      url: '/products/:productId',
      template: '<product-details-info></product-details-info>',
    });
  },
]);
