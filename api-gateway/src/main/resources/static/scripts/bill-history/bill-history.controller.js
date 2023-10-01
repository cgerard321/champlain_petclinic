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

        self.customerNameMap = {};

        self.owners.forEach(function (customer) {
            // The customer's ownerId is used as the key, and their full name as the value
            self.customerNameMap[customer.ownerId] = customer.firstName + ' ' + customer.lastName;
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
                console.log("Event source was closed by server succesfully. " + error)
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
                console.log("Event source was closed by server succesfully. " + error)
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
                console.log("Event source was closed by server succesfully. " + error)
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
                console.log("Event source was closed by server succesfully. " + error)
            }else{
                console.log("EventSource error: "+error)
            }
        }

        $http.get('api/gateway/vets').then(function (resp) {
            self.vetList = resp.data;
            arr = resp.data;
        });

        $http.get('api/gateway/owners').then(function (owners) {
            self.owners = owners.data;
            console.log(owners)
        });

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
            if (customerName) {
                return customerName;
            } else {
                return 'Unknown Customer';
            }
        };

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
