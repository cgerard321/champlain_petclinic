'use strict';

angular.module('billForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('billNew', {
                parent: 'app',
                url: '/bill/new',
                template: '<bill-form></bill-form>'
            })
    }]);