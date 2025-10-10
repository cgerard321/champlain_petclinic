'use strict';

angular.module('forgotPwdForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('forgotPwdPost', {
                parent: 'app',
                url: '/forgot_password',
                template: '<forgot-pwd-form></forgot-pwd-form>'
            })
    }]);