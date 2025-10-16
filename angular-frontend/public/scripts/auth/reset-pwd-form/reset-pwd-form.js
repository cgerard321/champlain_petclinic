'use strict';

angular.module('resetPwdForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('resetPwdForm', {
                parent: 'app',
                url: '/reset_password/:token',
                params: {token: null},
                template: '<reset-pwd-form></reset-pwd-form>'
            })
    }]);