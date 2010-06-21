$(document).ready(function(){
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
    * Filter menu always in view
    */

    $("#header").bind('inview', function(event,inview){
        if(inview){
            if($("#filtersContainer").hasClass("fixed")){
                $("#filtersContainer").removeClass("fixed");
            }
        }else {
            $("#filtersContainer").addClass("fixed");
        }
    });

    jB.filter.levelListener();
});

/**
* Filter - Level
*/
jB = {};

jB.filter = {};

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
      var labelsString = $(this).attr("class").toLowerCase();
      var sessionHas = labelsString.split(" ");
      $(sessionHas).each(function(){
          // If session has one of the labels
          if ($.inArray(this.toString(), labels) > -1) {
            hideSession = false;

            // Exit loop
            return false;
          }
      });
    });

    // If session matches no selected tags, hide it
    if (hideSession) {
      $(this).addClass("hide");
    } else {
      $(this).removeClass("hide");
    }
  });
};

jB.filter.levelListener = function(){
  $(".filter.levels li").click(function(){
       $(this).toggleClass("on");
       var labels = jB.filter.findEnabled($(this).parent());
       jB.filter.hideNotIn(labels,"level");
  });
};
