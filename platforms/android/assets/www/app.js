var app = angular.module('hmBleManager', [
    'ngRoute',
    'ionic',
    'appControllers'
]);

app.config(function($stateProvider, $urlRouterProvider) {
    $stateProvider
        .state('main', {
            url: '/main',
            templateUrl: 'main.html',
            controller: 'mainController'
        })
        .state('detail', {
            url: '/detail/:address/:uuid/:major/:minor',
            templateUrl: 'detail.html',
            controller: 'detailController'
        });

    $urlRouterProvider.otherwise('/main');
});

var controllers = angular.module("appControllers", []);

var HM_DEVICES = "HMDevices";
