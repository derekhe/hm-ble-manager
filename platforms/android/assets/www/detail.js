controllers.controller("detailController", function ($scope, $routeParams, $location, $timeout) {
    $scope.id = $routeParams.id;
    $scope.connecting = true;

    $scope.back = function () {
        cordova.exec(null, null, HM_DEVICES, "disconnect", []);
        $location.path("/main");
    }

    $scope.connect = function () {
        cordova.exec(function (connected) {
            $scope.connected = (connected === "connected");
            $scope.connecting = false;
            $scope.$apply();
            $scope.test();
        }, null, HM_DEVICES, "reg_connect_callback", []);

        cordova.exec(function (success) {

        }, function (fail) {

        }, HM_DEVICES, "connect", [$scope.id]);
    }

    $scope.test = function () {
        $scope.testPassed = false;

        cordova.exec(function () {
            $scope.testPassed = true;
            $scope.$apply();
        }, function (fail) {
            $scope.testPassed = false;
            $scope.$apply();
        }, HM_DEVICES, "test", [$scope.id]);
    }

    $scope.getADVI = function () {
        $scope.ADVI = "";

        cordova.exec(function (advi) {
            $scope.ADVI = advi;
            $scope.$apply();
        }, function (error) {
            $scope.error = error;
            $scope.$apply();
        }, HM_DEVICES, "AT+ADVI", []);
    }

    $scope.getBattery = function () {
        $scope.battery = "";

        cordova.exec(function (battery) {
            $scope.battery = battery;
            $scope.$apply();
        }, function (error) {
            $scope.error = error;
            $scope.$apply();
        }, HM_DEVICES, "AT+BATT", []);
    }
    document.addEventListener('deviceready', function () {
        $timeout($scope.connect, 500);
    });
});