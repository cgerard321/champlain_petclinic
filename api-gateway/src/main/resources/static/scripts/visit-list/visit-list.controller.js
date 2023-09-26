'use strict';
angular.module('visitList')
    .controller('VisitListController', ['$http', '$scope', function ($http, $scope) {
        let self = this;
        // Lists holding visits for the tables to display
        self.upcomingVisits = []
        self.previousVisits = []

        let eventSource = new EventSource("api/gateway/visits")
        eventSource.addEventListener('message', function (event){
            $scope.$apply(function(){
                self.upcomingVisits.push(JSON.parse(event.data))
            })
        })
        eventSource.onerror = (error) =>{
            if(eventSource.readyState === 0){
                eventSource.close()
                console.log("EventSource was closed by server successfully."+error)
            }else{
                console.log("EventSource error: "+error)
            }
        }

        $scope.cancelVisit = function (visitId, status){
            console.log("Called Function")
            console.log(status)

            if (status === "CANCELLED") {
                status = "CANCELLED"
            }else if(status === "CONFIRMED"){
                status = "CONFIRMED"
            }else if(status === "IN_PROGRESS"){
                status = "IN_PROGRESS"
            }else if(status === "COMPLETED"){
                status = "COMPLETED"
            }else{
                status = "CANCELLED"
            }
            console.log("Function Finished")

            let putURL = 'api/gateway/visits/' + visitId + '/status/' + status;

            console.log(putURL);

            $http.put(putURL, status)
                .then(successCallback, errorCallback)

            function successCallback(response) {
                $scope.errors = [];
                alert(visitId + " visit was cancelled successfully");
                console.log(response, 'res');
                delayedReload();
            }

            function errorCallback(error) {
                alert($scope.errors);
                console.log(error, 'Could not receive data');
            }
        }

        function delayedReload() {
            var loadingIndicator = document.getElementById('loadingIndicator');
            loadingIndicator.style.display = 'block';
            setTimeout(function() {
                location.reload();
            }, 1000); //delay by 1 second
    }
}]);

//     // self.sortFetchedVisits = function() {
    //     //     let currentDate = getCurrentDate()
    //     //     $.each(self.visits, function(i, visit) {
    //     //         let selectedVisitDate = Date.parse(visit.date);
    //     //         if(selectedVisitDate >= currentDate) {
    //     //             self.upcomingVisits.push(visit)
    //     //         } else {
    //     //             self.previousVisits.push(visit)
    //     //         }
    //     //     })
    //     // }
    //     // function getCurrentDate() {
    //     //     let dateObj = new Date()
    //     //     var dd = String(dateObj.getDate()).padStart(2, '0')
    //     //     var mm = String(dateObj.getMonth() + 1).padStart(2, '0')
    //     //     var yyyy = dateObj.getFullYear()
    //     //     return Date.parse(yyyy + '-' + mm + '-' + dd)
    //     // }
    // }])

