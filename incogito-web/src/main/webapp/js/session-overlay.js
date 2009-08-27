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

function showSession() {
    sessionOverlay.show()
    sessionOverlayBg.show()

    var sessionId = $(this).find(".session-id").text()
    getSession(eventName, sessionId, function(session) {
        console.log("session:")
        console.log(session)
        var sessionDetails = $("#template-session-details").clone()

        sessionDetails.
            removeAttr("id").
            removeClass("template")

        sessionDetails.find(".session-details-title").text(session.title)
        var ul = sessionDetails.find(".session-details-labels ul");
        $.each(session.labels, function(i, label) {
            var li = $("<li>").appendTo(ul)
            li.text(label.displayName)
        })
        sessionDetails.find(".session-details-room .text").text(session.room)
        sessionDetails.find(".session-details-level").text(session.level.displayName)
        sessionDetails.find(".session-details-timeslot").text(session.start.hour + ":" + session.start.minute + " - " + session.end.hour + ":" + session.end.minute) // TODO: Formatting
        sessionDetails.find(".session-details-abstract").html(session.abstractHtml)
        sessionDetails.find(".session-details-body").html(session.bodyHtml)

        var templateSpeaker = $("#template-details-session-speaker")
        var speakers = sessionDetails.find(".session-details-speakers")
        $.each(session.speakers, function(i, speaker) {
            console.log(speaker)
            var speakerDiv = templateSpeaker.clone()
            if (speaker.imageUrl) {
                speakerDiv.find(".session-details-speaker-image").
                    attr({src: speaker.imageUrl, title: speaker.name, alt: speaker.name })
            }
            else {
                speakerDiv.find(".session-details-speaker-image").remove()
            }
            speakerDiv.find(".session-details-speaker-name").text(speaker.name)
            speakerDiv.find(".session-details-speaker-bio").html(speaker.bioHtml)
            speakerDiv.
                removeAttr("id").
                removeClass("template").
                appendTo(speakers)
        })

        // Copy over the generated session
        $("#session-overlay .overlay-content").empty()
        $("#session-overlay .overlay-content").append(sessionDetails)
        sessionDetails.show()
    })
}

function closeSession() {
    sessionOverlay.hide()
    sessionOverlayBg.hide()
}
