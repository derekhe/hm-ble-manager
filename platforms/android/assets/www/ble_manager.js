controllers.controller("mainController", function ($scope) {
    var HM_DEVICES = "HMDevices";

    $scope.startScan = function () {
        $scope.isScanning = true;
        $scope.devices = [];

        cordova.exec(function (discoveryFinished) {
            console.log(discoveryFinished);
            $scope.devices = discoveryFinished;
            $scope.isScanning = false;
            $scope.$apply();
        }, null, HM_DEVICES, "reg_discovery_finished_callback", []);

        cordova.exec(null, null, HM_DEVICES, "discovery", []);

        $scope.$apply();
    }

    document.addEventListener('deviceready', function () {
        $scope.startScan();
    });
})