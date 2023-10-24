'use strict';


angular.module('billHistory')
    .controller('BillHistoryController', ['$http','$scope', function ($http,$scope) {
        let self = this;
        self.billHistory = []
        self.paidBills = []
        self.unpaidBills = []
        self.overdueBills = []


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

        self.ownersUUID = [
            { ownerId: 'f470653d-05c5-4c45-b7a0-7d70f003d2ac', firstName: 'George', lastName: 'Franklin' },
            { ownerId: 'e6c7398e-8ac4-4e10-9ee0-03ef33f0361a', firstName: 'Betty', lastName: 'Davis' },
            { ownerId: '3f59dca2-903e-495c-90c3-7f4d01f3a2aa', firstName: 'Eduardo', lastName: 'Rodriguez' },
            { ownerId: 'a6e0e5b0-5f60-45f0-8ac7-becd8b330486', firstName: 'Harold', lastName: 'Davis' },
            { ownerId: 'c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2', firstName: 'Peter', lastName: 'McTavish' },
            { ownerId: 'b3d09eab-4085-4b2d-a121-78a0a2f9e501', firstName: 'Jean', lastName: 'Coleman' },
            { ownerId: '5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd', firstName: 'Jeff', lastName: 'Black' },
            { ownerId: '48f9945a-4ee0-4b0b-9b44-3da829a0f0f7', firstName: 'Maria', lastName: 'Escobito' },
            { ownerId: '9f6accd1-e943-4322-932e-199d93824317', firstName: 'David', lastName: 'Schroeder' },
            { ownerId: '7c0d42c2-0c2d-41ce-bd9c-6ca67478956f', firstName: 'Carlos', lastName: 'Esteban' }
        ];


        $http.get('api/gateway/vets').then(function (resp) {
            self.vetList = resp.data;
            arr = resp.data;
            // console.log(resp)
        });

        $http.get('api/gateway/owners').then(function (owners) {
            self.ownersInfoArray = owners.data;
            // console.log(self.ownersInfoArray)
            self.ownersInfoArray.forEach(function(owner) {
                console.log(owner.ownerId);
            });
        });
        // self.getOwnerUUIDByName = function(customerId) {
        //     const owner = self.ownersInfoArray.find(function(owner) {
        //         return owner.firstName === firstName && owner.lastName === lastName;
        //     });
        //
        //     if (owner) {
        //         return owner.ownerId;
        //     }
        //
        //     return 'unknown-uuid';
        // };

        $scope.getOwnerUUIDByCustomerId = function(customerId) {
            let foundOwner;
            // Iterate through self.owners to find a matching customerId
            self.owners.forEach(function(owner) {
                if (owner.ownerId === customerId.toString()) {
                    foundOwner = owner;
                }
            });
            self.ownersUUID.forEach(function(owner) {
                if (owner.ownerId === customerId.toString()) {
                    foundOwner = owner;
                }
            });
            if (foundOwner) {
                // Get the first and last name from the foundOwner
                const firstName = foundOwner.firstName;
                const lastName = foundOwner.lastName;

                const ownerInfo = self.ownersInfoArray.find(function(owner) {
                    return owner.firstName === firstName && owner.lastName === lastName;
                });
                if (ownerInfo) {
                    return ownerInfo.ownerId;
                }
            }

            return 'Unknown Owner';
        };




        self.getOwnerInfoByCustomerId = function (customerId) {
            const owner = self.ownerIdToInfoMap[customerId];
            return owner || {};
        };

        self.getOwnerFullName = function (customerId) {
            const owner = self.ownerIdToInfoMap[customerId];
            if (owner) {
                return owner.firstName + ' ' + owner.lastName;
            }
            return 'Unknown Owner';
        };

        self.customerNameMap = {};
        self.customerNameMap2 = {};

        self.owners.forEach(function (customer) {
            // The customer's ownerId is used as the key, and their full name as the value
            self.customerNameMap[customer.ownerId] = customer.firstName + ' ' + customer.lastName;
        });

        self.ownersUUID.forEach(function (customer) {
            self.customerNameMap2[customer.ownerId] = customer.firstName + ' ' + customer.lastName;
        });

        let eventSource = new EventSource("api/gateway/bills")
        eventSource.addEventListener('message',function (event){
            $scope.$apply(function (){
                self.billHistory.push(JSON.parse(event.data))
            })
        })
        eventSource.onerror = (error)=>{
            if(eventSource.readyState === 0){
                eventSource.close()
                console.log("Event source was closed by server successfully. " + error)
            }else{
                console.log("EventSource error: "+error)
            }
        }

        let eventSourcePaid = new EventSource("api/gateway/bills/paid")
        eventSourcePaid.addEventListener('message',function (event){
            $scope.$apply(function (){
                self.paidBills.push(JSON.parse(event.data))
            })
        })

        eventSourcePaid.onerror = (error)=>{
            if(eventSourcePaid.readyState === 0){
                eventSourcePaid.close()
                console.log("Event source was closed by server successfully. " + error)
            }else{
                console.log("EventSource error: "+error)
            }
        }

        let eventSourceUnpaid = new EventSource("api/gateway/bills/unpaid")
        eventSourceUnpaid.addEventListener('message',function (event){
            $scope.$apply(function (){
                self.unpaidBills.push(JSON.parse(event.data))
            })
        })

        eventSourceUnpaid.onerror = (error)=>{
            if(eventSourceUnpaid.readyState === 0){
                eventSourceUnpaid.close()
                console.log("Event source was closed by server successfully. " + error)
            }else{
                console.log("EventSource error: "+error)
            }
        }

        let eventSourceOverdue = new EventSource("api/gateway/bills/overdue")
        eventSourceOverdue.addEventListener('message',function (event){
            $scope.$apply(function (){
                self.overdueBills.push(JSON.parse(event.data))
            })
        })

        eventSourceOverdue.onerror = (error)=>{
            if(eventSourceOverdue.readyState === 0){
                eventSourceOverdue.close()
                console.log("Event source was closed by server successfully. " + error)
            }else{
                console.log("EventSource error: "+error)
            }
        }

        // Assuming that self.owners is an array of owner objects
        self.getOwnerById = function(ownerId) {
            // Find and return the owner with the given ownerId
            for (var i = 0; i < self.owners.length; i++) {
                if (self.owners[i].ownerId === ownerId) {
                    return self.owners[i];
                }
            }
            return null; // Handle if owner not found
        };


        $scope.getVetDetails = function(vetId) {
            const vet = self.vetList.find(function(vet) {
                return vet.vetBillId === vetId;
            });
            const vet2 = self.vetList.find(function(vet) {
                return vet.vetId === vetId;
            });
            if (vet) {
                return vet.firstName + ' ' + vet.lastName;
            } else if (vet2) {
                return vet2.firstName + ' ' + vet2.lastName;
            } else {
                return 'Unknown Vet';
            }
        };

        // $scope.getCustomerDetails = function(customerId) {
        //     const customer = self.owners.find(function(customer) {
        //         return customer.ownerId === customerId;
        //     });
        //
        //     if (customer) {
        //         return customer.firstName + ' ' + customer.lastName;
        //     } else {
        //         return 'Unknown Customer';
        //     }
        // }
        $scope.getCustomerDetails = function(customerId) {
            const customerName = self.customerNameMap[customerId];
            const customerName2 = self.customerNameMap2[customerId];
            if (customerName) {
                return customerName;
            } else if (customerName2) {
                return customerName2;
            } else {
                return 'Unknown Customer';
            }
        };


        $scope.deleteAllBills = function () {
            let varIsConf = confirm('Are you sure you want to delete all the bills in the bill history');
            if (varIsConf) {
                $http.delete('api/gateway/bills')
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert("bill history was deleted successfully");
                    console.log(response, 'res');
                    //refresh list
                    $http.get('api/gateway/bills').then(function (resp) {
                        self.billHistory = resp.data;
                        arr = resp.data;
                    });
                }
                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'Could not receive data');
                }

            } else {
                return;
            }
        }

        $scope.deleteBill = function (billId) {
            let varIsConf = confirm('You are about to delete billId ' + billId + '. Is it what you want to do ? ');
            if (varIsConf) {

                $http.delete('api/gateway/bills/' + billId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert(billId + " bill was deleted successfully");
                    console.log(response, 'res');
                    //refresh list
                    $http.get('api/gateway/bills').then(function (resp) {
                        self.billHistory = resp.data;
                        arr = resp.data;
                    });
                }

                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'Could not receive data');
                }
            }
        };

    }]);
