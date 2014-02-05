controllers.controller("detailController", function ($scope, $routeParams, $location) {
    $scope.id = $routeParams.id;

    $scope.back = function () {
        $location.path("/main");
    }

    $scope.connect = function () {
        cordova.exec(function (success) {

        }, function (fail) {

        }, HM_DEVICES, "connect", [$scope.id]);
    }

    $scope.test = function () {
        cordova.exec(function (success) {

        }, function (fail) {

        }, HM_DEVICES, "test", [$scope.id]);
    }
});