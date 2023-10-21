'use strict';

angular.module('inventoryManagerSignup', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventoryManagerSignup', {
                parent: 'app',
                url: '/inventoryManagerSignup',
                template: '<manager-form></manager-form>'
            })
    }]);