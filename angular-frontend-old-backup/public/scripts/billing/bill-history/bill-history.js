'use strict';

angular.module('billHistory', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider
      .state('bills', {
        parent: 'app',
        url: '/bills/bills-pagination?page&size&billId&customerId&customerFirstName&customerLastName',
        template: '<bill-history></bill-history>',
        controller: 'BillHistoryController',
        controllerAs: 'vm',
      })
      .state('deleteBill', {
        parent: 'app',
        url: '/bills/:billId/deleteBill',
        template: '<bill-history></bill-history>',
      })

      .state('deleteAllBills', {
        parent: 'app',
        url: '/bills/deleteAllBills',
        template: '<bill-history></bill-history>',
      });
  },
]);

var expectedOwnerId = function (expectedOwnerId, key) {
  return element
    .all(by.repeater(key + ' in owners').column(key + '.customerId'))
    .then(function (arr) {
      return arr.forEach(function (wd, i) {
        return expect(wd.getText()).toMatch(expectedOwnerId[i]);
      });
    });
};

it('should return the expected ownerId with strict comparison', async function () {
  var searchOwnerId = element(by.model('search.customerId'));
  var strict = element(by.model('strict'));
  searchOwnerId.clear();
  searchOwnerId.sendKeys('2');
  strict.click();
  await expectedOwnerId(['2'], 'bill');
});

var expectedVetId = function (expectedVetId, key) {
  return element
    .all(by.repeater(key + ' in vets').column(key + '.vetId'))
    .then(function (arr) {
      return arr.forEach(function (wd, i) {
        return expect(wd.getText()).toMatch(expectedVetId[i]);
      });
    });
};

it('should return the expected vetId with strict comparison', async function () {
  var searchVetId = element(by.model('search.vetId'));
  var strict = element(by.model('strict'));
  searchVetId.clear();
  searchVetId.sendKeys('3');
  strict.click();
  await expectedVetId(['3'], 'bill');
});
