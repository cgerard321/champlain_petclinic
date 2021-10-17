'use strict';

angular.module('verification', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('verification', {
                parent: 'app',
                url: '/auth/verification/:token',
                template: '<verification></verification>'
            })
    }]);