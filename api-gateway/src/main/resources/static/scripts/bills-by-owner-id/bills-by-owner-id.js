'use strict';

angular.module('billsByOwnerId', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('billsByOwnerId', {
                parent: 'app',
                url: '/bills/customerId/:customerId',
                template: '<bills-by-owner-id></bills-by-owner-id>'
            })
    }]);