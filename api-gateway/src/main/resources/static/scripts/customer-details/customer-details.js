'use strict';

angular.module('customerDetails', ['ui.router'])
    .config(['$stateProvider',function ($stateProvider) {
        $stateProvider
            .state('customerDetails', {
                parent: 'app',
                url: '/customers/details/:customerId',
                params: {customerId: null},
                template: '<customer-details></customer-details>'
            })
            // .state('petCustomerDetails', {
            //     parent: 'app',
            //     url: '/customers/:customerId/pets/:petId/',
            //     params: {customerId: null, petId: null},
            //     template: '<customer-details></customer-details>'
            // }

    }]);


