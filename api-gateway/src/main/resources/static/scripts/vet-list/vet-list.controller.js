'use strict';

app = angular.module('vetList')
.controller('VetListController', ['$http', function ($http) {
        var self = this;
        this.show = (e) => {
//               let body = document.getElementsByTagName("body")[0];
               let child = e.target.
               child.classList.add("modalOn");
               child.classList.remove("modalOff");

        }
        this.hide = () => {
//            document.querySelector('.modal').remove();
        }
        this.test = console.log;

        $http.get('api/gateway/vets').then(function (resp) {
            self.vetList = resp.data;
        });
    }]);
