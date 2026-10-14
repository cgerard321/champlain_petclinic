"use strict";

const whiteList = new Set([
  "login",
  "signup",
  "forgot_password",
  "reset_password",
]);

/* App Module */
const petClinicApp = angular.module("petClinicApp", [
  "ui.router",
  "ui.bootstrap",
  "vetList",
  "vetForm",
  "vetDetails",
  "loginForm",
  "signupForm",
]);

petClinicApp.factory("authProvider", [
  "$window",
  function ($window) {
    return {
      setUser: ({ username, email, userId, roles, token }) => {
        $window.localStorage.setItem("username", username);
        $window.localStorage.setItem("email", email);
        $window.localStorage.setItem("UUID", userId);
        if (token) {
          $window.localStorage.setItem("token", token);
        }
        let rolesArr = [];
        if (roles) {
          roles.forEach((role) => rolesArr.push(role.name || role));
        }
        $window.localStorage.setItem("roles", rolesArr.toString());
      },
      getUser: () => ({
        username: $window.localStorage.getItem("username"),
        email: $window.localStorage.getItem("email"),
        UUID: $window.localStorage.getItem("UUID"),
        roles: $window.localStorage.getItem("roles"),
        token: $window.localStorage.getItem("token"),
      }),
      purgeUser: () => {
        $window.localStorage.removeItem("username");
        $window.localStorage.removeItem("email");
        $window.localStorage.removeItem("UUID");
        $window.localStorage.removeItem("roles");
        $window.localStorage.removeItem("token");
      },
      isLoggedIn: () => !!$window.localStorage.getItem("email"),
      isAdmin: () =>
          $window.localStorage.getItem("roles") != null &&
          !!$window.localStorage.getItem("roles").includes("ADMIN"),
    };
  },
]);

petClinicApp.factory("httpErrorInterceptor", [
  "$q",
  "$location",
  "authProvider",
  function ($q, $location, authProvider) {
    return {
      request: function (config) {
        const token = authProvider.getUser().token;
        if (token) {
          config.headers["Authorization"] = "Bearer " + token;
        }
        return config;
      },
      responseError: (rej) => {
        if (
            !whiteList.has($location.path().substring(1)) &&
            rej.status === 401
        ) {
          authProvider.purgeUser();
          $location.path("/login");
          return $q(() => null);
        } else if (rej.status === 403) {
          $location.path("/welcome");
          return $q(() => null);
        }
        return $q.reject(rej);
      },
    };
  },
]);

petClinicApp.run([
  "$rootScope",
  "$location",
  "authProvider",
  function ($rootScope, $location, authProvider) {
    $rootScope.$on("$locationChangeSuccess", function (event) {
      if (
          whiteList.has($location.path().substring(1)) ||
          $location.path().startsWith("/reset_password")
      ) {
        return;
      }
      if (!authProvider.isLoggedIn()) {
        event.preventDefault();
        $location.path("/login");
      }
    });
  },
]);

petClinicApp.config([
  "$stateProvider",
  "$urlRouterProvider",
  "$locationProvider",
  "$httpProvider",
  function (
      $stateProvider,
      $urlRouterProvider,
      $locationProvider,
      $httpProvider,
  ) {
    $httpProvider.defaults.headers.common["Cache-Control"] = "no-cache";
    $locationProvider.hashPrefix("!");
    $urlRouterProvider.otherwise("/vets");

    $stateProvider.state("app", {
      abstract: true,
      url: "",
      template: "<ui-view></ui-view>",
    });

    $httpProvider.interceptors.push("httpErrorInterceptor");
  },
]);