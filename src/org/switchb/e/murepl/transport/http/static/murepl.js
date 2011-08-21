// Copyright 2011 Kevin Reid, under the terms of the MIT X license
// found at http://www.opensource.org/licenses/mit-license.html ...............

"use strict";

var mureplNetStatus;

function ajax() {
  var src = $('#input').get(0).value;

  $.ajax('', {
    type: 'POST',
    data: {
      src: src, 
      output: "none"
    },
    error: function (xhr, status, httpError) {
      $('#out').append("<div>Error sending code to server: " + status + " " + httpError + "</div>");
    },
  });
  $("#input").get(0).value = "";
  $("#input").select();
}

var longPollFailures = 0;

function lp(responseHolder) {
  if (!responseHolder) {
    responseHolder = $("<div></div>")
    $('#out').append(responseHolder);
  }
  
  responseHolder.load('', {
    output: "log",
    wait: 1,
    log: $('#form input[name="log"]').val()
  }, function (response, status, xhr) {
    if (status == "error") {
      // Exponential backoff
      var delaySec = Math.pow(2, ++longPollFailures);

      mureplNetStatus.text("Network error: " + xhr.status + ". Retrying in " + delaySec + " s.");
      
      setTimeout(function () { lp(responseHolder); },
                 delaySec * 1000);
    } else {
      // grab logmark data supplied by server to use on next request
      $('#form input[name="log"]').remove();
      $('#form').append($('#logmark input'));
      $('#logmark').remove();
      
      // start next long-poll immediately
      longPollFailures = 0;
      lp();
    }

    // TODO: only do this if already at bottom/user evaled something
    $(document.body).scrollTop(responseHolder.position().top - 10);
  });

  mureplNetStatus.text("Ready");
}

function murepl_init() {
  mureplNetStatus = $('#netstatus');

  $("#input").focus();
  
  $(document).bind("keydown", function (e) {
    // Put focus on the input field if any key is pressed
    //   ...unless it's just a modifier state change
    //   ...or a keyboard shortcut (we're assuming that shortcuts have at least
    //      one of alt, ctrl, meta)
    if (!(e.altKey || e.ctrlKey || e.metaKey || e.keyCode == 0 || e.keyCode == 16)) {
      $("#input").focus();
    }
  });
  
  setTimeout(function () { lp(); }, 0);
}