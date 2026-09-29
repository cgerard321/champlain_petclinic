'use strict';

angular.module('customerList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {

        $stateProvider
            .state('customers', {
                parent: 'app',
                url: '/customers-pagination?page&size&customerId&firstName&lastName&phoneNumber&city',
                template: '<customer-list></customer-list>',
                controller: 'CustomerListController',
                controllerAs: 'vm'
            })
    }]);