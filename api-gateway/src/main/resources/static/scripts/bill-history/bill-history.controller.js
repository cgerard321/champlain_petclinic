'use strict';


angular.module('billHistory')
    .controller('BillHistoryController', ['$http','$scope', function ($http,$scope) {
        let self = this;
        self.billHistory = []

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

        $scope.getCustomerDetails = function(customerId) {
            const customer = self.owners.find(function(customer) {
                return customer.id === customerId;
            });

            if (customer) {
                return customer.firstName + ' ' + customer.lastName;
            } else {
                return 'Unknown Customer';
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
