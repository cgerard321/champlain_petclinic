'use strict';



angular.module('visitList')
    // .controller('VisitListController', ['$http','$stateParams', '$scope', function ($http, $stateParams, $scope) {
    .controller('VisitListController', ['$http', '$scope', function ($http, $scope) {
        var self = this;
        self.upcomingVisits = [];
        var eventSource = new EventSource("api/gateway/visits");
        eventSource.addEventListener('message', function (event){
            // $scope.init(function(){
            $scope.$apply(function(){
                var data = JSON.parse(event.data);
                self.upcomingVisits.push(data);
                console.log(data);
            })
        }, true);

        // $scope.$on('$destroy', function () {
        //     eventSource.onmessage("ngRepeat:dupes").close();
        // });

        // $http.get("api/gateway/visits").then(function (resp) {
        //     self.upcomingVisits = resp.data;


            // console.log(resp.data);
        // });
    }]);
        //     var self = this;
    //
    //     $http.get("api/gateway/visits").then(function (resp) {
    //         // self.visits = resp.data;
    //         self.upcomingVisits = resp.data;
    //         // self.sortFetchedVisits();
    //         console.log(resp)
    //     });
    //     // Lists holding visits for the tables to display
    //     // self.upcomingVisits = [];
    //     // self.previousVisits = [];
    //
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

