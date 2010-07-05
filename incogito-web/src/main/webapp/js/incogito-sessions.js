$(window).load(function(){

    /**
    * Initialize the overlay from jQuery Tools
    */
    jB.overlay.initialize();

    /**
    * Open overlay and display session info if defained
    */
    jB.overlay.showOnAccess();

    /**
    * toggle show/hide time slot content
    */
    $("h2").click(function(e){
        $(this).toggleClass("open");
        $(this).parent().find(".sessions").each(function(){
            $(this).toggleClass("open");
        });
        e.preventDefault();
    });

    /**
    * Lightbox
    */

    jB.overlay.sessionListener();

    /**
    * Expand/Minimize all sessions
    */

    $("#expandAll a").click(function(e){

        $(this).toggleClass("open");
        var hasClassOpen = $(this).hasClass("open") ? true:false;

        $('#main-content div').each(function(){
           if(hasClassOpen){
             $("h2", this).addClass("open");
             $(".sessions", this).addClass("open");
           }else {
             $("h2", this).removeClass("open");
             $(".sessions", this).removeClass("open");
           }
        });

        e.preventDefault();
    
    });

    /**
    * Filter menu always in view
    */

    $("#header").bind('inview', function(event,inview){
        if(inview){
            $("#filtersContainer").removeClass("fixed");
        }else {
            $("#filtersContainer").addClass("fixed");
        }
    });

    jB.filter.levelListener();
    jB.filter.labelListener();
});

/**
* Filter - Level
*/
jB = {};

jB.filter = {};

jB.overlay = {};

// requires structure "keyword on"
jB.filter.findEnabled = function(elm){
  var labels = [];

  // Find all active labels and add to the filter
  $(".on", elm).each(function() {
    var classes = $(this).attr("class");
    labels.push(classes.split(" ")[0]);
  });

  return labels;
};

jB.filter.hideNotIn = function(labels,type){

  $(".session").each(function() {
    // Get all labels for current session
    var sessionClasses = $(".legends ."+type, this);

    var hideSession = true;

    // Loop thru all labels for the session
    $(sessionClasses).each(function() {
      var labelsString = $(this).attr("class");
      var sessionHas = labelsString.split(" ");
      $(sessionHas).each(function(){
          // If session has at least one of the labels

          if ($.inArray(this.toString(), labels) > -1) {
            hideSession = false;

            // Exit loop
            return false;
          }
      });
    });

    // If session matches no selected tags, hide it
    if (hideSession) {
      $(this).addClass(type+"-hide");
    } else {
      $(this).removeClass(type+"-hide");
    }
  });
};

jB.filter.levelListener = function(){
  $(".filter.levels li").click(function(e){
       if($(".filter.levels li.off").length == 0){
          $(".filter.levels li").each(function(){
            $(this).toggleClass("on");
            $(this).toggleClass("off");
          });
       }

       $(this).toggleClass("on");
       $(this).toggleClass("off"); 

       if($(".filter.levels li.on").length == 0){
          $(".filter.levels li").each(function(){
            $(this).toggleClass("on");
            $(this).toggleClass("off");
          });
       }
       
       var labels = jB.filter.findEnabled($(this).parent());
       jB.filter.hideNotIn(labels,"level");
       e.preventDefault();
    });
};

jB.filter.labelListener = function(){
  $(".filter.labels li a").click(function(e){
       if($(".filter.labels li.off").length == 0){
          $(".filter.labels li").each(function(){
            $(this).toggleClass("on");
            $(this).toggleClass("off");
          });
       }

       $(this).parent().toggleClass("on");
       $(this).parent().toggleClass("off"); 

       if($(".filter.labels li.on").length == 0){
          $(".filter.labels li").each(function(){
            $(this).toggleClass("on");
            $(this).toggleClass("off");
          });
       }

       var labels = jB.filter.findEnabled($(this).parent().parent());
       jB.filter.hideNotIn(labels,"label");
       e.preventDefault();
    });
};

/**
* Overlay with session-information
*/

jB.overlay.getSession = function (sessionUrl, onSuccess) {
    $('#session-overlay-indicator').show();
    $("#session-overlay-content").hide()
    $.ajax({
        dataType: "json",
        url: sessionUrl,
        success: function(data) {
            onSuccess(data)
        }
    })
};

jB.overlay.formatTime = function (time) {
    var s = ""
    if(time.hour < 10) {
        s += "0"
    }
    s += time.hour + ":"

    if(time.minute < 10) {
        s += "0"
    }
    s += time.minute

    return s
};

jB.overlay.formatSpeakerSummary = function (speakers) {
    var prefix = "Speaker: "
    var s = ""
    $.each(speakers, function(i, speaker) {
        if(i > 0) {
            s += ", "
            prefix = "Speakers: "
        }

        s += speaker.name
    })

    return prefix + s;
};

jB.overlay.formater = function(session) {
    $("#session-overlay-content").empty()
    var sessionDetails = $("#template-session-details").clone()

    sessionDetails.
        removeAttr("id").
        removeClass("template");

    sessionDetails.find(".session-details-title").text(session.title)
    sessionDetails.find(".session-url-reference").text("Link:")

    var currentPage = $("h1.event-title.high-lighted a").attr("href");

    var link = $('<input>').attr({type:'text',value:currentPage+"#"+session.id});
    link.appendTo(sessionDetails.find(".session-url-reference"));

    var ul = sessionDetails.find(".session-metadata");
    // <li class="session-detail-label label-${label.id}">${label.displayName}</li>
    $.each(session.labels, function(i, label) {
        var li = $("<li>").
            addClass("session-detail-label").
            addClass("label-" + label.id).
            appendTo(ul);
        li.text(label.displayName)
    })
    var li = $("<li>").
        text("Room: " + session.room).
        appendTo(ul);
    li = $("<li>").
        text(jB.overlay.formatTime(session.start) + " - " + jB.overlay.formatTime(session.end)).
        addClass("format-" + session.format).
        appendTo(ul);
    li = $("<li>").
        text(jB.overlay.formatSpeakerSummary(session.speakers)).
        appendTo(ul)
    sessionDetails.find(".session-details-level").text(session.level.displayName)
    sessionDetails.find(".session-details-body").html(session.bodyHtml)

    var templateSpeaker = $(".template.speaker")
    var speakerBioses = sessionDetails.find(".speaker-bioses")
    $.each(session.speakers, function(i, speaker) {
        var speakerDiv = templateSpeaker.clone()
        var speakerImage = speakerDiv.find(".speaker-image");
        if (speaker.photoUrl) {
            speakerImage.find("img").
                attr({src: speaker.photoUrl, title: speaker.name, alt: speaker.name }).
                appendTo(speakerImage)
        }
        else {
            speakerImage.remove()
        }
        speakerDiv.find(".speaker-name").text(speaker.name)
        speakerDiv.find(".speaker-bio").html(speaker.bioHtml)
        speakerDiv.
            removeAttr("id").
            removeClass("template").
            appendTo(speakerBioses);
     });
     // Copy over the generated session
     $("#session-overlay-content").append(sessionDetails)
     
     $("#session-overlay-content").show()
     $('#session-overlay-indicator').hide();
};

jB.overlay.initialize = function(){
   $("#session-overlay").overlay({
     effect: 'apple',
     top:'5%',
     mask: {
		color: '#FFF',
		loadSpeed: 0,
		opacity: 0.7
	 }
   });
};

jB.overlay.sessionListener = function(){
  $(".session .info a").click(function(e){
     e.preventDefault();
     jB.overlay.getSession($(".session-rest-uri",$(this).parent()).text(), jB.overlay.formater);
     $("#session-overlay").overlay().load();
  });
};

jB.overlay.showOnAccess = function(){
    var id = document.location.hash.substring(1);
    if(id.length>0){
      jB.overlay.getSession($(".session-rest-uri",$("#"+id)).text(), jB.overlay.formater);
      $("#session-overlay").overlay().load();
    }
}