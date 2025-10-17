'use strict';

angular.module('ownerRegister', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('ownerRegister', {
                parent: 'app',
                url: '/owners/register',
                template: '<owner-register></owner-register>'
            })
            // .state('ownerEdit', {
            //     parent: 'app',
            //     url: '/owners/:ownerId/:method',
            //     template: '<owner-form></owner-form>'
            // })
    }]);

