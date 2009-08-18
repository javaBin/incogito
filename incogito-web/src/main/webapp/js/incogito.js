var InterestLevel = {
    ATTEND: "ATTEND",
    INTEREST: "INTEREST",
    NO_INTEREST: "NO_INTEREST"
}

function createConsole() {
    var names = ["log", "debug", "info", "warn", "error", "assert", "dir", "dirxml", "group", "groupEnd", "time", "timeEnd", "count", "trace", "profile", "profileEnd"];
    if (window.opera && !window.console) {
        window.console = {};
        for (var i = 0; i < names.length; ++i) {
            window.console[names[i]] = function() {
            }
        }

        window.console.info = function() {
            opera.postError(arguments);
        }
    }

    if (!window.console) {
        window.console = {};
        for (i = 0; i < names.length; ++i) {
            window.console[names[i]] = function() {
            }
        }
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

function getSession(eventName, sessionId, success) {
    console.log("Fetching " + sessionId + " for event " + eventName + "...")
    var s = success
    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/sessions/" + sessionId,
        success: function(data) {
            console.log("Got '" + data.title + "' for event " + eventName)
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

function updateInterest(eventName, sessionId, state, success, unauthorized) {
    console.log("Setting interest level on " + sessionId + " for event " + eventName + " to " + state + "...")

    $.ajax({
        dataType: "json",
        url: baseurl + "/rest/events/" + eventName + "/" + sessionId + "/session-interest",
        type: "POST",
        contentType: "application/json",
        data: state,
        complete: function(xhr) {
            switch (xhr.status) {
                case 201:
                    console.log("Updated interest level on " + sessionId + " for event " + eventName)
                    if (typeof success == "function")
                        success()
                    break;
                case 401:
                    console.log("Unauthorized")
                    if (typeof unauthorized == "function")
                        unauthorized()
                    break;
            }
        }
    })
}

Functional.install()
createConsole()
