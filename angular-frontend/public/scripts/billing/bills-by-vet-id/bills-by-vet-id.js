'use strict';

angular.module('billsByVetId', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider
      .state('billsByVetId', {
        parent: 'app',
        url: '/bills/vetId/:vetId',
        template: '<bills-by-vet-id></bills-by-vet-id>',
      })
      .state('deleteBillsByVetId', {
        parent: 'app',
        url: '/bills/vetId/:vetId/deleteBill',
        template: '<bill-by-vet-id></bill-by-vet-id>',
      });
  },
]);
