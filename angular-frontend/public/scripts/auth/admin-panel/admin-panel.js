'use strict';

angular.module('adminPanel', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('AdminPanel', {
                parent: 'app',
                url: '/adminPanel',
                template: '<admin-panel></admin-panel>'
            })
    }]);