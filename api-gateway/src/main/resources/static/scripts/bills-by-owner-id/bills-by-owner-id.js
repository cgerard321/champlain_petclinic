'use strict';

angular.module('billsByOwnerId', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('billsByOwnerId', {
                parent: 'app',
                url: '/bills/customerId/:customerId',
                template: '<bills-by-customer-id></bills-by-customer-id>'
            })
    }]);