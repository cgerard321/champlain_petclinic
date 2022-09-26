'use strict';

angular.module('visitList')
    .controller('VisitListController', ['$http', function ($http) {
        var self = this;
        var petIds = [1,2]

        $http.get("api/gateway/visits/"+petIds).then(function (resp) {
            self.visits = resp.data;
            self.sortFetchedVisits();
             console.log(resp)
        });
        // Lists holding visits for the tables to display
        self.upcomingVisits = [];
        self.previousVisits = [];

        self.sortFetchedVisits = function() {
            let currentDate = getCurrentDate();

            $.each(self.visits, function(i, visit) {
                let selectedVisitDate = Date.parse(visit.date);

                if(selectedVisitDate >= currentDate) {
                    self.upcomingVisits.push(visit);
                } else {
                    self.previousVisits.push(visit);
                }
            });
        }
        function getCurrentDate() {
            let dateObj = new Date();
            var dd = String(dateObj.getDate()).padStart(2, '0');
            var mm = String(dateObj.getMonth() + 1).padStart(2, '0');
            var yyyy = dateObj.getFullYear();
            return Date.parse(yyyy + '-' + mm + '-' + dd);
        }
    }]);
