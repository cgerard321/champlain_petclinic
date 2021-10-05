'use strict';

angular.module('billDetails', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('billDetails', {
                parent: 'app',
                url: '/bills/details/:billId',
                template: '<bill-details></bill-details>'
            })
    }]);