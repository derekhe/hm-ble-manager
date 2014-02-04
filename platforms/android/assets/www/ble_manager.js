controllers.controller("mainController", function ($scope, $location, $rootScope) {
    var HM_DEVICES = "HMDevices";

    $scope.startScan = function () {
        $scope.isScanning = true;
        $scope.clearScanResult();

        cordova.exec(function (discoveryFinished) {
            console.log(discoveryFinished);
            $rootScope.devices = discoveryFinished;
            $scope.isScanning = false;
            $scope.$apply();
        }, null, HM_DEVICES, "reg_discovery_finished_callback", []);

        cordova.exec(null, null, HM_DEVICES, "discovery", []);

        $scope.$apply();
    }

    $scope.clearScanResult = function()
    {
        $rootScope.devices = [];
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