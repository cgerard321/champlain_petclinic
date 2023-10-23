'use strict';

angular.module('billDetails')
    .controller('BillDetailsController', ['$http', '$stateParams', '$scope', function ($http, $stateParams, $scope) {
        let self = this;

        self.owners = [
            { ownerId: '1', firstName: 'George', lastName: 'Franklin' },
            { ownerId: '2', firstName: 'Betty', lastName: 'Davis' },
            { ownerId: '3', firstName: 'Eduardo', lastName: 'Rodriguez' },
            { ownerId: '4', firstName: 'Harold', lastName: 'Davis' },
            { ownerId: '5', firstName: 'Peter', lastName: 'McTavish' },
            { ownerId: '6', firstName: 'Jean', lastName: 'Coleman' },
            { ownerId: '7', firstName: 'Jeff', lastName: 'Black' },
            { ownerId: '8', firstName: 'Maria', lastName: 'Escobito' },
            { ownerId: '9', firstName: 'David', lastName: 'Schroeder' },
            { ownerId: '10', firstName: 'Carlos', lastName: 'Esteban' }
        ];

        $http.get('api/gateway/bills/' + $stateParams.billId).then(function (resp) {
            self.bills = resp.data;

        });
        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
        });

        $http.get('api/gateway/owners').then(function (owners) {
            self.ownersInfoArray = owners.data;
            // console.log(self.ownersInfoArray)
            // self.ownersInfoArray.forEach(function(owner) {
            //     console.log(owner.ownerId);
            // });
        });

        $scope.getOwnerUUIDByCustomerId = function(customerId) {
            let foundOwner;

            // Iterate through self.owners to find a matching customerId
            self.owners.forEach(function(owner) {
                // console.log(customerId);
                // console.log(owner.ownerId);
                if (owner.ownerId === customerId.toString()) {
                    foundOwner = owner;
                }
            });

            if (foundOwner) {
                // Get the first and last name from the foundOwner
                const firstName = foundOwner.firstName;
                const lastName = foundOwner.lastName;

                // console.log('Found Owner:', foundOwner);
                // console.log('First Name:', firstName);
                // console.log('Last Name:', lastName);

                // Find the corresponding owner in self.ownersInfoArray
                const ownerInfo = self.ownersInfoArray.find(function(owner) {
                    return owner.firstName === firstName && owner.lastName === lastName;
                });

                // console.log('Owner Info:', ownerInfo);

                if (ownerInfo) {
                    return ownerInfo.ownerId;
                }
            }

            return 'Unknown Owner';
        };

        self.customerNameMap = {};

        self.owners.forEach(function (customer) {
            // The customer's ownerId is used as the key, and their full name as the value
            self.customerNameMap[customer.ownerId] = customer.firstName + ' ' + customer.lastName;
        });

        $scope.getCustomerDetails = function(customerId) {
            const customerName = self.customerNameMap[customerId];
            if (customerName) {
                return customerName;
            } else {
                return 'Unknown Customer';
            }
        };
        $scope.getVetDetails = function(vetId) {
            const vet = self.vetList.find(function(vet) {
                return vet.vetBillId === vetId;
            });

            if (vet) {
                return vet.firstName + ' ' + vet.lastName;
            } else {
                return 'Unknown Vet';
            }
        };
    }]);

