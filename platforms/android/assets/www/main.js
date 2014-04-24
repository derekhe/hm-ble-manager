controllers.controller("mainController", function ($scope, $location, $rootScope) {
    $scope.startScan = function () {
        $scope.isScanning = true;
        $scope.clearScanResult();

        cordova.exec(function () {
            $scope.$apply(function () {
                $scope.isScanning = false;
            });
        }, null, HM_DEVICES, "reg_discovery_finished_callback", []);

        cordova.exec(function (device) {
            $scope.$apply(function () {
                $rootScope.devices[device.address] = device;
            });
        }, null, HM_DEVICES, "reg_discovered_device", []);

        cordova.exec(null, null, HM_DEVICES, "discovery", []);

    }

    $scope.clearScanResult = function () {
        $rootScope.devices = {};
    }

    $scope.showDetails = function (device) {
        $location.path("/detail/" + device.address + "/" + device.uuid + "/" + device.major + "/" + device.minor);
    }

    document.addEventListener('deviceready', function () {
            $scope.startScan();
    });
})