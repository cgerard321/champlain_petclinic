'use strict';

angular.module('customerRegister', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('customerRegister', {
                parent: 'app',
                url: '/customers/register',
                template: '<customer-register></customer-register>'
            })
            // .state('customerEdit', {
            //     parent: 'app',
            //     url: '/customers/:customerId/:method',
            //     template: '<customer-form></customer-form>'
            // })
    }]);

