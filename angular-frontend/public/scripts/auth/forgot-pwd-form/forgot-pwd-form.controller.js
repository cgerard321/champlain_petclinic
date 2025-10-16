'use strict';

angular.module('forgotPwdForm')
    .controller('forgotPwdFormController', ["$http", '$location', "$scope", function ($http, $location, $scope) {

        $scope.isLoading = false;
        let loaderDiv = document.getElementById("loaderDiv");
        loaderDiv.style.display = "none";


        this.forgotPwdPost = () => {
            console.log("forgotPwdPost");
            loaderDiv.style.display = "block";
            console.log($scope.isLoading)
            $http.post("/api/gateway/users/forgot_password", {
                email: $scope.forgotPwdPost.email,
                url: "http://localhost:8080/#!/reset_password/"
            })

                .then(response => {
                    alert("Email was sent !");
                    $location.path("/welcome");
                })
                .catch(error => {
                    console.log(error);
                    alert("Email was not sent !, please try again!\n" + error.data.message)
                })
                .finally(()=> {
                    loaderDiv.style.display = "none";
                    }
                )

        };

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add();
    }]);
