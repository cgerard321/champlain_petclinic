'use strict';
// Whitelist for all things related to auth and Q 401/403 handling
const whiteList = new Set([
    'login',
    'signup',
    'forgot_password',
    'reset_password'
]);

/* App Module */
const petClinicApp = angular.module('petClinicApp', [
    'ui.router', 'ui.bootstrap', 'layoutNav', 'layoutFooter', 'layoutWelcome', 'ownerList', 'ownerDetails', 'ownerForm', 'ownerRegister', 'petRegister', 'petForm',
    'productDetailsInfo', 'productForm', 'productList', 'productUpdateForm', "productTypeList", 'productBundleList', 'productBundleDetailsInfo', 'productBundleForm', 'productBundleUpdateForm', 'visits', 'visit', 'visitList' , 'vetList','vetForm',
    'vetDetails', 'billForm', 'billUpdateForm', 'loginForm', 'rolesDetails', 'signupForm',
    'billDetails', 'billsByOwnerId', 'billHistory','billsByVetId','inventoriesList', 'inventoriesForm','inventoriesService','inventoriesProductList', 'inventoriesUpdateForm', 'inventoriesProductUpdateForm','inventoriesProductDetailsInfo','inventoriesProductForm',
    'verification' , 'adminPanel','resetPwdForm','forgotPwdForm','petTypeList', 'petDetails','userDetails','managerForm','userModule']);




petClinicApp.factory("authProvider", ["$window", function ($window) {

    return {
        setUser: ({ username, email, userId, roles }) => {
            console.log("Setting user")
            console.log(username)
            console.log(email)
            console.log(userId)
            console.log(roles)

            $window.localStorage.setItem("username", username)
            $window.localStorage.setItem("email", email)
            $window.localStorage.setItem("UUID", userId)
            let rolesArr = []
            roles.forEach(role => {
                rolesArr.push(role.name)
            });
            console.log(rolesArr.toString())
            $window.localStorage.setItem("roles", rolesArr.toString())
        },
        getUser: () => ({
            username: $window.localStorage.getItem("username"),
            email: $window.localStorage.getItem("email"),
            UUID: $window.localStorage.getItem("UUID"),
            roles : $window.localStorage.getItem("roles")
        }),
        purgeUser: () => {
            $window.localStorage.removeItem("username")
            $window.localStorage.removeItem("email")
            $window.localStorage.removeItem("UUID")
            $window.localStorage.removeItem("roles")
        },
        isLoggedIn: () => !!$window.localStorage.getItem("email"),
        isAdmin: () =>  $window.localStorage.getItem("roles") != null && !!$window.localStorage.getItem("roles").includes("ADMIN")
    }
}]);

petClinicApp.factory("httpErrorInterceptor", ["$q", "$location", "authProvider", function ($q, $location, authProvider) {
    return {
        responseError: rej => {
            if (!whiteList.has($location.path().substring(1)) && (rej.status === 401)) {
                authProvider.purgeUser();
                $location.path('/login');
                return $q(() => null)
            }else if(rej.status === 403){
                $location.path('/welcome');
                console.log("You are not authorized to access this page")
                return $q(() => null)
            }

            return $q.reject(rej);
        }
    }
}]);

petClinicApp.run(['$rootScope', '$location', 'authProvider', function ($rootScope, $location, authProvider) {
    $rootScope.$on('$locationChangeSuccess', function (event) {

        if(whiteList.has($location.path().substring(1)) || $location.path().startsWith('/reset_password')) {
            return console.log("WHITE LISTED: Ignoring");
        }

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

    $httpProvider.interceptors.push('httpErrorInterceptor');
}]);

['welcome', 'nav', 'footer'].forEach(function (c) {
    var mod = 'layout' + c.toUpperCase().substring(0, 1) + c.substring(1);
    const controller = mod+"Controller";

    angular.module(mod, []);
    angular.module(mod)
        .controller(controller, ['$rootScope', '$scope', 'authProvider', function ($rootScope, $scope, authProvider) {

            const load = () => {
                $scope.isLoggedIn = authProvider.isLoggedIn();
                $scope.isAdmin = authProvider.isAdmin();
                if(!$scope.isLoggedIn)return;

                const { email, username, roles, UUID } = authProvider.getUser();
                $scope.email = email;
                $scope.username = username;
                $scope.roles = roles;
                $scope.UUID = UUID;

            }

            $rootScope.$on('$locationChangeSuccess', load);
            load();
    }]);

    angular.module(mod).component(mod, {
        templateUrl: "scripts/fragments/" + c + ".html",
        controller,
    });
});


