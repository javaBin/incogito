Functional.install()

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
    console.log("Marking interest on " + sessionId + " for event " + eventName + "...")

    var attendanceMarker = {
        sessionId: sessionId,
        state: "INTERESTED"
    }

    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/attendance-markers",
        type: "POST",
        contentType: "application/json",
        data: $.json.encode(attendanceMarker),
        success: function(data) {
            console.log("Marked interest on " + sessionTitle + " for event " + eventName)
            console.log(data)
            s(data)
        }
    })
}

function markInterest(eventName, sessionId, success) {
    console.log("Marking interest on " + sessionId + " for event " + eventName + "...")

    var attendanceMarker = {
        sessionId: sessionId,
        state: "NOT_ATTENDING"
    }

    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/attendance-markers",
        type: "POST",
        contentType: "application/json",
        data: $.json.encode(attendanceMarker),
        success: function(data) {
            console.log("Deleted interest on " + sessionTitle + " for event " + eventName)
            console.log(data)
            s(data)
        }
    })
}
