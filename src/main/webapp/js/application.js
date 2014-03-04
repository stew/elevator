$(function() {
  "use strict";

  var detect = $("#detect");
  var header = $('#header');
  var content = $('#content');
  var status = $('#status');
  var socket = $.atmosphere;
  var subSocket;
//  var transport = 'long-polling';
  var transport = 'websocket';

  var tableHead = function(elevators) {
    $("<table><thead><tr><tr></thead></table>")

  }
  
  var request = {
    url: "/elevator",
    contentType: "application/json",
    logLevel: 'debug',
    transport: transport,
    fallbackTransport: 'long-polling'
  };

  request.onOpen = function(response) {
    content.html($('<p>', {
      text: 'Atmosphere connected using ' + response.transport
    }));
    transport = response.transport;
  };

  request.onReconnect = function(rq, rs) {
    socket.info("Reconnecting")
  };

  request.onMessage = function(rs) {
    var message = rs.responseBody;
    try {
      var json = jQuery.parseJSON(message);
      console.log("got a message")
      console.log(json)
    } catch (e) {
      console.log('This doesn\'t look like a valid JSON object: ', message.data);
      return;
    }

    // json....
  };

  request.onClose = function(rs) {
    socket.info = "connection closed";
  };

  request.onError = function(rs) {
    content.html($('<p>', {
      text: 'Sorry, but there\'s some problem with your ' + 'socket or the server is down'
    }));
  };

  
  
  subSocket = socket.subscribe(request);

});
