var app = angular.module('hmBleManager', [
    'ngRoute',
    'appControllers'
]);

app.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
            when('/main', {
                templateUrl: 'main.html',
                controller: 'mainController'
            }).
            when("/detail/:id", {
                templateUrl: "detail.html",
                controller: 'detailController'
            }).
            otherwise({
                redirectTo: '/main'
            });
    }
]);

var controllers = angular.module("appControllers", []);
