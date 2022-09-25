'use strict';



angular.module('visitList')
    .controller('VisitListController', ['$http', function ($http) {
        var self = this;
        
        $http.get('api/gateway/visits').then(function (resp) {
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
    }]);
