controllers.controller("detailController", function ($scope, $routeParams, $location) {
    $scope.id = $routeParams.id;

    $scope.back = function()
    {
        $location.path("/main");
    }
});