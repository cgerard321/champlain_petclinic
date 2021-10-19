'use strict';
/* App Module */
var petClinicApp = angular.module('petClinicApp', [
    'ui.router', 'layoutNav', 'layoutFooter', 'layoutWelcome', 'ownerList', 'ownerDetails', 'ownerForm', 'petForm'
    , 'visits', 'vetList','vetForm','vetDetails', 'loginForm', 'rolesDetails', 'signupForm', 'billDetails', 'billHistory'
    , 'verification']);

petClinicApp.factory("authProvider", ["$window", function ($window) {

    return {
        setUser: ({ token, username, email }) => {
            $window.localStorage.setItem("token", token)
            $window.localStorage.setItem("username", username)
            $window.localStorage.setItem("email", email)
        },
        getUser: () => ({
            token: $window.localStorage.getItem("token"),
            username: $window.localStorage.getItem("username"),
            email: $window.localStorage.getItem("email"),
        }),
        purgeUser: () => {
            $window.localStorage.removeItem("token")
            $window.localStorage.removeItem("username")
            $window.localStorage.removeItem("email")
        },
        isLoggedIn: () => !!$window.localStorage.getItem("token")
    }
}]);

petClinicApp.run(['$rootScope', '$location', 'authProvider', function ($rootScope, $location, authProvider) {
    $rootScope.$on('$locationChangeSuccess', function (event) {

        console.log("based")
        if (!authProvider.isLoggedIn()) {
            console.log('DENY : Redirecting to Login');
            event.preventDefault();
            $location.path('/login');
        }
        else {
            console.log('ALLOW');
        }
    });
}])

petClinicApp.config(['$stateProvider', '$urlRouterProvider', '$locationProvider', '$httpProvider', function (
    $stateProvider, $urlRouterProvider, $locationProvider, $httpProvider) {

    // safari turns to be lazy sending the Cache-Control header
    $httpProvider.defaults.headers.common["Cache-Control"] = 'no-cache';

    $locationProvider.hashPrefix('!');

    $urlRouterProvider.otherwise('/welcome');
    $stateProvider
        .state('app', {
            abstract: true,
            url: '',
            template: '<ui-view></ui-view>'
        })
        .state('welcome', {
            parent: 'app',
            url: '/welcome',
            template: '<layout-welcome></layout-welcome>'
        });
}]);

['welcome', 'nav', 'footer'].forEach(function (c) {
    var mod = 'layout' + c.toUpperCase().substring(0, 1) + c.substring(1);
    angular.module(mod, []);
    angular.module(mod).component(mod, {
        templateUrl: "scripts/fragments/" + c + ".html"
    });
});