'use strict';
angular.module('visitList')
    .controller('VisitListController', ['$http', '$scope', function ($http, $scope) {
        let self = this
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
    }])
// .controller('VisitListController', ['$http','$stateParams', '$scope', function ($http, $stateParams, $scope) {

//     // self.sortFetchedVisits = function() {
    //     //     let currentDate = getCurrentDate();
    //     //
    //     //     $.each(self.visits, function(i, visit) {
    //     //         let selectedVisitDate = Date.parse(visit.date);
    //     //
    //     //         if(selectedVisitDate >= currentDate) {
    //     //             self.upcomingVisits.push(visit);
    //     //         } else {
    //     //             self.previousVisits.push(visit);
    //     //         }
    //     //     });
    //     // }
    //     // function getCurrentDate() {
    //     //     let dateObj = new Date();
    //     //     var dd = String(dateObj.getDate()).padStart(2, '0');
    //     //     var mm = String(dateObj.getMonth() + 1).padStart(2, '0');
    //     //     var yyyy = dateObj.getFullYear();
    //     //     return Date.parse(yyyy + '-' + mm + '-' + dd);
    //     // }
    // }]);

