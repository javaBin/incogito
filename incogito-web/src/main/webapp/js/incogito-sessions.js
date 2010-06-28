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
    * Expand/Minimize all sessions
    */

    $("#expandAll a").click(function(e){

        $(this).toggleClass("open");

        $('#main-content div').each(function(){
           $("h2", this).toggleClass("open");
           $(".sessions", this).toggleClass("open");
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
    jB.filter.labelListener();
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
