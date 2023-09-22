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

        // $http.get('api/gateway/bills').then(function (resp) {
        //     self.billHistory = resp.data;
        // });
/*
        $scope.getVetByVetId = function (vetId) {
            $http.get('api/gateway/vets/'+vetId).then(function (resp1) {
                self.vetName = resp1.firstName + ' ' + resp1.lastName;
                //{{vet.firstName}} {{vet.lastName}}
                console.log(self.vetName)
            });
        }
*/


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
