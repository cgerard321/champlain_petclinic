'use strict';

angular.module('billHistory', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('bills', {
                parent: 'app',
                url: '/bills',
                template: '<bill-history></bill-history>'
            })
            .state('deleteBill', {
                parent: 'app',
                url: '/bills/:billId/deleteBill',
                template: '<bill-history></bill-history>'
            })
    }]);