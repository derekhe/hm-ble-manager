var app = angular.module('hmBleManager', [
    'ngRoute',
    'appControllers'
]);

app.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
            when('/main', {
                templateUrl: 'ble_manager.html',
                controller: 'mainController'
            }).
            when("/detail/:address/:uuid/:major/:minor", {
                templateUrl: "detail.html",
                controller: 'detailController'
            }).
            otherwise({
                redirectTo: '/main'
            });
    }
]);

var controllers = angular.module("appControllers", []);

var HM_DEVICES = "HMDevices";
