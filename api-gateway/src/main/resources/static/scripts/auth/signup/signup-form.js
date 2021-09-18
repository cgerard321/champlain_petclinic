'use strict';

angular.module('signupForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('signupForm', {
                parent: 'app',
                url: '/signup',
                template: '<signup-form></signup-form>'
            })
    }]);
