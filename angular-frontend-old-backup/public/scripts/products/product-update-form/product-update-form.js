'use strict';

angular.module('productUpdateForm', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('updateProduct', {
      parent: 'app',
      url: '/products/:productId',
      template: '<product-update-form></product-update-form>',
    });
  },
]);
