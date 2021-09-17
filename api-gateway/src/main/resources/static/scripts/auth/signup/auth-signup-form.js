'use strict';

angular.module('signupForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('signupNew', {
                parent: 'app',
                url: '/signup/new',
                template: '<signup-form></signup-form>'
            })
    }]);
