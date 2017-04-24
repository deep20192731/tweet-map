var apiApp = angular.module('apiApp', ['ngWebSocket']);

var renderMap = function($scope, $http) {
	if($scope.map == undefined) {
		var mapOptions = {
		        zoom: 2,
		        center: new google.maps.LatLng(37.775, -122.434),
		        mapTypeId: google.maps.MapTypeId.HYBRID
		}
		$scope.map = new google.maps.Map(document.getElementById('map'), mapOptions);
	}

	$scope.startTweet = 0;
	$scope.sizeOfEachCall = 200;
	
	//$scope.pointArray = new google.maps.MVCArray();
	$scope.pointArray = new Array();
	
	/*$scope.heatmap = new google.maps.visualization.HeatmapLayer({
        data: $scope.pointArray,
        map: $scope.map
    });*/

	$scope.getTweets = function(term, startTweet) {
		if($scope.pointArray.length > 0) {
			//$scope.heatmap.setMap(null);
			clearMarkers($scope.pointArray);
			$scope.pointArray = new Array();
			/*$scope.heatmap = new google.maps.visualization.HeatmapLayer({
		        data: $scope.pointArray,
		        map: $scope.map
		    });*/
		}
		
		callApi(term, startTweet);
	}
	
	function clearMarkers(markers) {
		for(i=0; i<markers.length; i++) {
			markers[i].setMap(null);
		}
	}
	
	function callApi(term, startTweet) {
		var location = document.URL;
		$http.get(location + "tweet/" + term + "/" + startTweet + "/"
				+ $scope.sizeOfEachCall).
		success(function(data, status, headers, config) {
			if(data.length > 0) {
				getPoints(data);
				var newStart = startTweet + $scope.sizeOfEachCall;
				callApi(term, newStart);
			}
		});
	}
	
	function getPoints(objects) {
		var length = objects.length;
		if($scope.pointArray != undefined) {
			for(i = 0; i < length; i++) {
				//$scope.pointArray.push(new google.maps.LatLng(objects[i].latitude, objects[i].longitude));
				var pinColor;
				if(objects[i].sentimentType == "positive") {
					pinColor = "0000FF";
				} else if(objects[i].sentimentType == "negative") {
					pinColor = "FE7569";
				} else if(objects[i].sentimentType == "neutral") {
					pinColor = "FFFFFF"
				} else {
					pinColor = "808080"
				}

				var pinImage = new google.maps.MarkerImage("http://chart.apis.google.com/" +
						"chart?chst=d_map_pin_letter&chld=%E2%80%A2|" + pinColor,
				        new google.maps.Size(21, 34),
				        new google.maps.Point(0,0),
				        new google.maps.Point(10, 34));

				 var pinShadow = new google.maps.MarkerImage("http://chart.apis.google.com/" +
				 		"chart?chst=d_map_pin_shadow",
					        new google.maps.Size(40, 37),
					        new google.maps.Point(0, 0),
					        new google.maps.Point(12, 35));

				var marker = new google.maps.Marker({
				    position: { lat: objects[i].latitude, lng: objects[i].longitude },
				    label: "S",
				    map: $scope.map,
				    icon: pinImage,
				    shadow: pinShadow
				  });
				$scope.pointArray.push(marker);
			}
		}
	}
}

apiApp.factory('ATP', function ($websocket) {
    // Open a WebSocket connection
    var ws = $websocket("ws://" + location.hostname + ":8888");
    var atp = [];
    ws.onMessage(function (event) {
        var response;
        try {
        	console.log("Message = " + event.data);
            response = event.data;
            if(response != undefined) {
            	document.getElementById("indexMessage").innerHTML = 
            		"TweetID=" + response + " indexed";
            }
        } catch (e) {
            document.getElementById("indexMessage").innerHTML = 
                     "Sorry, connection failed ...";
            console.log('error: ', e);
            response = {'error': e};
        }
    });
    ws.onError(function (event) {
        console.log('connection Error', event);
    });
    ws.onClose(function (event) {
        console.log('connection closed', event);
    });
    ws.onOpen(function () {
        console.log('connection open');
        ws.send('HELLO SERVER');
    });
    return {
        atp: "Dummy Message",
        status: function () {
            return ws.readyState;
        },
        send: function (message) {
            if (angular.isString(message)) {
                ws.send(message);
            }
            else if (angular.isObject(message)) {
                ws.send(JSON.stringify(message));
            }
        }
    };
})
.controller('tweetIndexController', function ($scope, $http, ATP) {
    $scope.ATP = ATP;
    renderMap($scope, $http);
});