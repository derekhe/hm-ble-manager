controllers.controller("mainController", function ($scope, $location, $rootScope) {
    $scope.startScan = function () {
        $scope.isScanning = true;
        $scope.clearScanResult();

        cordova.exec(function () {
            $scope.isScanning = false;
            $scope.$apply();
        }, null, HM_DEVICES, "reg_discovery_finished_callback", []);

        cordova.exec(function (device)
        {
            $rootScope.devices[device.address] = device;
            $scope.$apply();
        }, null, HM_DEVICES, "reg_discovered_device", [])

        cordova.exec(null, null, HM_DEVICES, "discovery", []);

        $scope.apply();
    }

    $scope.clearScanResult = function()
    {
        $rootScope.devices = {};
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