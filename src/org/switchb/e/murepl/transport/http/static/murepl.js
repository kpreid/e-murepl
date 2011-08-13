// Copyright 2011 Kevin Reid, under the terms of the MIT X license
// found at http://www.opensource.org/licenses/mit-license.html ...............

"use strict";

function ajax() {
  var src = $('#input').get(0).value;

  var responseHolder = $("<div><pre>? <span class='input'></span></pre><div>...evaluating...</div></div>")
  responseHolder.find(".input").text(src);
  $('#out').append(responseHolder);
  
  responseHolder.load('/repl', {
    src: src, 
    noout: 1, 
    log: $('#form input[name="log"]').val()
  }, function (response, status, xhr) {
    if (status == "error") {
      responseHolder.html("Error contacting server: " + xhr.status + " " + xhr.statusText);
    }
    
    // grab logmark data supplied by server to use on next request
    $('#form input[name="log"]').remove();
    $('#form').append($('#logmark input'));
    
    $(document.body).scrollTop(responseHolder.position().top - 10);
  });
  $("#input").get(0).value = "";
  $("#input").select();
}

function murepl_init() {
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
}