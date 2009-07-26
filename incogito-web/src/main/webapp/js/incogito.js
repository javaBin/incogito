Functional.install()

var InterestLevel = {
    ATTEND: "ATTEND",
    INTEREST: "INTEREST",
    NO_INTEREST: "NO_INTEREST"
}

if (window.opera && !window.console) {
    window.console = {};
    var names = ["log", "debug", "info", "warn", "error", "assert", "dir", "dirxml", "group", "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd"];
    for (var i = 0; i < names.length; ++i) {
        window.console[names[i]] = function() {
        }
    }

    window.console.info = function() {
        opera.postError(arguments);
    }
}

function getEvents(success) {
    console.log("Fetching events...")
    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events",
        success: function(data) {
            var events = data.events;
            console.log("Got " + events.length + " events")
            s(events)
        }
    })
}

function getSessionsByEventName(eventName, success) {
    console.log("Fetching sessions for event " + eventName + "...")
    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/sessions",
        success: function(data) {
            var sessions = data.sessions;
            console.log("Got " + sessions.length + " for event " + eventName + "...")
            s(sessions)
        }
    })
}

function getSession(eventName, sessionName, success) {
    console.log("Fetching " + sessionName + " for event " + eventName + "...")
    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/sessions/" + sessionName,
        success: function(data) {
            console.log("Got " + sessionName + " for event " + eventName)
            s(data)
        }
    })
}

function getMySchedule(eventName, success) {
    console.log("Fetching schedule for event " + eventName + "...")
    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/my-schedule",
        success: function(data) {
            console.log("Got schedule for event " + eventName)
            s(data)
        }
    })
}

function getSchedule(eventName, userName, success) {
    console.log("Fetching " + userName + "' schedule for event " + eventName + "...")
    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/schedules/" + userName,
        success: function(data) {
            console.log("Got " + userName + "' schedule for event " + eventName)
            s(data)
        }
    })
}

function markInterest(eventName, sessionId, success) {
    updateInterest(eventName, sessionId, InterestLevel.INTEREST, success)
}

function markAttendance(eventName, sessionId, success) {
    updateInterest(eventName, sessionId, InterestLevel.ATTEND, success)
}

function dropInterest(eventName, sessionId, success) {
    updateInterest(eventName, sessionId, InterestLevel.NO_INTEREST, success)
}

function updateInterest(eventName, sessionId, state, success) {
    console.log("Setting interest level on " + sessionId + " for event " + eventName + " to " + state + "...")

    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/" + sessionId + "/session-interest",
        type: "POST",
        contentType: "application/json",
        data: state,
        complete: function(xhr, textStatus) {

            if(xhr.status != 201) {
                return
            }

            console.log("Updated interest level on " + sessionId + " for event " + eventName)
            if(typeof s == "function") s()
        }
    })
}
