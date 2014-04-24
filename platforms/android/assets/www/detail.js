controllers.controller("detailController", function ($scope, $stateParams, $location, $timeout, $q, $ionicPopup) {
    $scope.device = {
        address: $stateParams.address,
        uuid: $stateParams.uuid,
        major: $stateParams.major,
        minor: $stateParams.minor,
        battery: 0,
        advi: 0
    };

    $scope.connecting = true;
    $scope.refreshing = false;

    $scope.back = function () {
        cordova.exec(null, null, HM_DEVICES, "disconnect", []);
        $location.path("/main");
    };

    $scope.maskOption = { mask: function () {
        return ["aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"];
    }};

    $scope.connect = function () {
        var connection = $q.defer();
        cordova.exec(function (connected) {
            $scope.$apply(function () {
                $scope.connecting = false;
                $scope.connected = true;
            });
            $timeout($scope.refresh, 500);
        }, null, HM_DEVICES, "reg_connect_callback", []);

        cordova.exec(function () {
            $scope.$apply(function () {
                $scope.connecting = true;
                $scope.connected = false;
            });
        }, function () {
            $scope.$apply(function () {
                $scope.connecting = false;
                $scope.connected = false;
            });
        }, HM_DEVICES, "connect", [$scope.device.address]);
    };

    var getBattery = function () {
        var deferred = $q.defer();
        cordova.exec(function (result) {
            deferred.resolve(parseInt(result.substr(-3)), 10);
        }, function (error) {
            deferred.reject(error);
        }, HM_DEVICES, "AT+BATT?", []);

        return deferred.promise;
    };

    var getADVI = function () {
        var deferred = $q.defer();
        cordova.exec(function (result) {
            deferred.resolve(parseInt(result.substr(-1)), 16);
        }, function (error) {
            deferred.reject(error);
        }, HM_DEVICES, "AT+ADVI?", []);

        return deferred.promise;
    };

    $scope.getADVIms = function () {
        var advimap = { 0: 100, 1: 152, 2: 211, 3: 318, 4: 417, 5: 546, 6: 760, 7: 852, 8: 1022,
            9: 1285, 10: 2000, 11: 3000, 12: 4000, 13: 5000, 14: 6000, 15: 7000};
        return advimap[$scope.device.advi];
    };

    $scope.refresh = function () {
        $scope.refreshing = true;

        getBattery().then(function (battery) {
            $scope.device.battery = battery;
            return getADVI();
        }).then(function (advi) {
            $scope.device.advi = advi;
            $scope.refreshing = false;
        });
    };

    var saveMajor = function () {
        var deferred = $q.defer();
        cordova.exec(function (result) {
            console.log(result);
            deferred.resolve(result);
        }, null, HM_DEVICES, "AT+MARJ0x" + $scope.device.major.toUpperCase(), []);

        return deferred.promise;
    };

    var saveMinor = function () {
        var deferred = $q.defer();
        cordova.exec(function (result) {
            console.log(result);
            deferred.resolve(result);
        }, null, HM_DEVICES, "AT+MINO0x" + $scope.device.minor.toUpperCase(), []);

        return deferred.promise;
    };

    var saveADVI = function () {
        var deferred = $q.defer();
        cordova.exec(function (result) {
            console.log(result);
            deferred.resolve(result);
        }, null, HM_DEVICES, "AT+ADVI" + $scope.device.advi.toString(16).toUpperCase(), []);

        return deferred.promise;
    };

    var resetDevice = function () {
        var deferred = $q.defer();
        cordova.exec(function (result) {
            console.log(result);
            deferred.resolve(result);
        }, null, HM_DEVICES, "AT+RESET", []);

        return deferred.promise;
    }

    $scope.save = function () {
        saveMajor().then(saveMinor).then(saveADVI).then(resetDevice).then(function () {
            $ionicPopup.alert({
                                  title: 'Update!',
                                  content: 'Update success!'
                              }).then($scope.back);
        });
    }

    document.addEventListener('deviceready', function () {
        $timeout($scope.connect, 500);
    });
})
;