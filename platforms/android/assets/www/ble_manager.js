controllers.controller("mainController", function ($scope, $location, $rootScope) {
    var HM_DEVICES = "HMDevices";

    $scope.startScan = function () {
        $scope.isScanning = true;
        $rootScope.devices = [];

        cordova.exec(function (discoveryFinished) {
            console.log(discoveryFinished);
            $rootScope.devices = discoveryFinished;
            $scope.isScanning = false;
            $scope.$apply();
        }, null, HM_DEVICES, "reg_discovery_finished_callback", []);

        cordova.exec(null, null, HM_DEVICES, "discovery", []);

        $scope.$apply();
    }

    $scope.showDetails = function (device) {
        $location.path("/detail/" + device.address);
    }

    document.addEventListener('deviceready', function () {
        if (_.isEmpty($rootScope.devices)) {
            $scope.startScan();
        }
    });
})