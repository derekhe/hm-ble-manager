controllers.controller("mainController", function ($scope) {
    $scope.startScan = function () {
        cordova.exec(function (rst) {
            $scope.devices = rst;
        }, function (fail) {

        }, "BluetoothSerial", "list", []);
    }

    document.addEventListener('deviceready', function () {
        $scope.startScan();
    });
})