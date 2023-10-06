'use strict';

angular.module('visitList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('visitList', {
                parent: 'app',
                url: '/visitList',
                template: '<visit-list></visit-list>'
            })

            .state('confirmVisit', {
                parent: 'app',
                url: '/visits/:visitId',
                template: '<visit-list></visit-list>'
            })

            .state('cancelVisit', {
                parent: 'app',
                url: '/visits/:visitId',
                template: '<visit-list></visit-list>'
            })

            .state('deleteVisit', {
                parent: 'app',
                url: '/visits/:visitId/deleteVisit',
                template: '<visit-list></visit-list>'
            })

            .state('deleteCancelledVisit', {
                parent: 'app',
                url: '/visits/cancelled',
                template: '<visit-list></visit-list>'
            })
    }]);
