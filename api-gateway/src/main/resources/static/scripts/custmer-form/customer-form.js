'use strict';


angular.module('customerForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            // .state('customerNew', {
            //     parent: 'app',
            //     url: '/customers/new',
            //     template: '<customer-form></customer-form>'
            // })
            .state('customerEdit', {
                parent: 'app',
                url: '/customers/:customerId/:method',
                template: '<customer-form></customer-form>'
            })
    }]);

