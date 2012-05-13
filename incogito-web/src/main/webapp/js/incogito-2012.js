/*global $, console */

var jz = {
	tools: {},
	presentations: {}
};

/* ----------------------------------------- */

jz.tools.init = function() {
	$("#tools .choice a").on("click", function(event) {
		jz.presentations.filterBy($(this).attr("data-toggle"));
		$("#tools .choice a").removeClass("active");
		$(this).toggleClass("active");
		jz.presentations.firstlast();
		event.preventDefault();
	});
	$("#tools a.all").on("click", function(event) {
		jz.tools.reset();
		event.preventDefault();
	});
	$("#tools a.more").on("click", function(event) {
		jz.tools.reset();
		$("#tools ." + $(this).attr("data-toggle")).show();
		jz.presentations.firstlast();
		event.preventDefault();
	});
};

jz.tools.reset = function() {
	$("#tools .choice").hide();	
	$("#tools .choice a").removeClass("active");
	jz.presentations.reset();
};

/* ----------------------------------------- */

jz.presentations.init = function() {
	jz.presentations.firstlast();
	$("a.presentation").on("click", function(event) {
		var more = $(this).find(".more");
		if(more.is(":hidden")) {
			more.slideDown("fast");
		} else {
			more.slideUp("fast");
		}
		event.preventDefault();
	});
};

jz.presentations.reset = function() {
	$("a.presentation").show();
	$("a.presentation .more").hide();
	jz.presentations.firstlast();
};

jz.presentations.firstlast = function() {
	$("a.presentation").removeClass("first").removeClass("last").removeClass("odd");
	$(".agenda").find("a.presentation:visible:first").addClass("first");
	$(".agenda").find("a.presentation:visible:last").addClass("last");
	$(".agenda").find("a.presentation:visible:odd").addClass("odd");
};

jz.presentations.filterBy = function(name) {
	$("a.presentation, a.presentation .more").hide();
	$("." + name).show();
	jz.presentations.firstlast();
};

/* ----------------------------------------- */

$(function() {
	jz.tools.init();
	jz.presentations.init();
});