var sessionOverlay
var sessionOverlayBg
var sessionCloseButton

$(document).ready(function() {
    sessionOverlay = $("#session-overlay")
    sessionOverlayBg = $("#session-overlay-bg")
    sessionCloseButton = $("#session-close-button")

    sessionCloseButton.bind("click", closeSession)
    sessionOverlayBg.bind("click", closeSession)

    $(document).keypress(function(e) {
        if (e.keyCode == 27) {
            if (sessionOverlay.css("display") != "none") {
                closeSession()
            }
        }
    })
})

function formatTime(time) {
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
}

function formatSpeakerSummary(speakers) {
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
}

function showSession() {
    sessionOverlay.show()
    sessionOverlayBg.show()

    var sessionUrl = $(this).find(".session-url").text()
    getSession(eventName, sessionUrl, function(session) {
        $("#session-overlay .overlay-content").empty()
        console.log("session:")
        console.log(session)
        var sessionDetails = $("#template-session-details").clone()

        sessionDetails.
            removeAttr("id").
            removeClass("template")

        sessionDetails.find(".session-details-title").text(session.title)
        var ul = sessionDetails.find(".session-metadata");
        console.log(ul)
        // <li class="session-detail-label label-${label.id}">${label.displayName}</li>
        $.each(session.labels, function(i, label) {
            console.log("label: " + label.displayName)
            var li = $("<li>").
                addClass("session-detail-label").
                addClass("label-" + label.id).
                appendTo(ul)
            li.text(label.displayName)
        })
        var li = $("<li>").
            text("Room: " + session.room).
            appendTo(ul)
        li = $("<li>").
            text(formatTime(session.start) + " - " + formatTime(session.end)).
            addClass("format-" + session.format).
            appendTo(ul)
        li = $("<li>").
            text(formatSpeakerSummary(session.speakers)).
            appendTo(ul)
        sessionDetails.find(".session-details-level").text(session.level.displayName)
        sessionDetails.find(".session-details-body").html(session.bodyHtml)

        var templateSpeaker = $(".template.speaker")
        var speakerBioses = sessionDetails.find(".speaker-bioses")
        $.each(session.speakers, function(i, speaker) {
            console.log(speaker)
            var speakerDiv = templateSpeaker.clone()
            var speakerImage = speakerDiv.find(".speaker-image");
            if (speaker.photoUrl) {
                $("<img>").
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
                appendTo(speakerBioses)
        })

        // Copy over the generated session
        $("#session-overlay .overlay-content").append(sessionDetails)
        sessionDetails.show()
    })
}

function closeSession() {
    sessionOverlay.hide()
    sessionOverlayBg.hide()
}
