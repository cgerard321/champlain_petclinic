'use strict';

angular.module('ownerList')
    .controller('OwnerListController', ['$http', function ($http) {
        var self = this;

        $http.get('api/gateway/customer/owners').then(function (resp) {
            // self.owners = resp.data;
            // console.log(resp)
            self.owners = [{ firstName: 'fname', lastName: 'lname', address: 'somewhere', telephone:'1111111111',custodian:'custodian1' }]
        });
    }]);
