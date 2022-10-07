'use strict';

angular.module('billsByOwnerId', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('billsByOwnerId', {
                parent: 'app',
                url: '/bills/customer/:customerId',
                template: '<bills-by-customer-id></bills-by-customer-id>'
            })
    }]);