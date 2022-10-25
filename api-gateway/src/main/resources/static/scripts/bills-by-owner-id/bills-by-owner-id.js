'use strict';

angular.module('billsByOwnerId', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('billsByOwnerId', {
                parent: 'app',
                url: '/bills/ownerId/:ownerId/bills',
                params: {ownerId: null},
                template: '<bills-by-owner-id></bills-by-owner-id>'
            })
    }]);