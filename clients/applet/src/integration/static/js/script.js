var cardSelected = false;
var eventHandler = {};
var selectedId = null;

function showSelection(data) {
    "use strict";
    selectedId = data.id;
    $(".control").hide();
    $(".application").show("slow");
}

function startPAOS(name) {
    "use strict";
    var applet = window.document.getElementById("ecard-applet");
    applet.startPAOS(name);
}

function getUrl(cardType) {
    "use strict";
    var url = "http://demo.skidentity.de/external?session=" + xfSessionId + "&card-type=" + cardType;
    return url;
}

function activate() {
    "use strict";
    cardSelected = true;
    $(".control").hide();
    $(".application").show("slow");
    $("#app").attr("src", getUrl("soft-cert"));
}

function startEid(data) {
    "use strict";
    var width = 200,
	height = 100,
	left = (screen.width - width) / 2,
	top = (screen.height - height) / 2,
	params = "width=" + width + ", height=" + height + ", top=" + top + ", left=" + left;
    window.open(getUrl(data.cardType) + "&return-url=http://ekiosk.vserver-001.urospace.de/auto_close.html", "eID", params);
}

function onClick(data) {
    "use strict";
    if (eventHandler[data.id] === true) {
	eventHandler[data.id] = false;
	cardSelected = true;
	showSelection(data);
	if (data.cardType === "http://ws.gematik.de/egk/1.0.0") {
	    startPAOS(data.name);
	}
	if (data.cardType === "http://bsi.bund.de/cif/npa.xml") {
	    startEid(data);
	    $("#app").attr("src", "http://demo.skidentity.de/demo?session=" + xfSessionId);
	} else {
	    $("#app").attr("src", getUrl(data.cardType));
	}
    }
}

function updateTerminal(data) {
    "use strict";
    eventHandler[data.id] = false;
    $("#" + data.id).attr("class", "terminal");
    $("#" + data.id + " img").attr("src", "static/img/no_card.jpg")
	.attr("alt", data.name)
	.off()
	.on("click", function () {
	    activate();
	});
}

function hideMessage() {
    "use strict";
//    $(".message").hide("slow");
}

function addImageToDiv(img, div) {
    "use strict";
    img.appendTo(div);
}

function addTextToDiv(text, div) {
    "use strict";
    var p = $("<p>").text(text);
    p.appendTo(div);
}

function addTerminal(data) {
    "use strict";
    if ($(".message").is(":visible")) {
	hideMessage();
    }
    if ($("#" + data.id).length === 0) {
	var div = $("<div>").attr("id", data.id);
	addImageToDiv($("<img>"), div);
	addTextToDiv(data.name, div);
	div.appendTo(".control");
	updateTerminal(data);
    }
}

function showMessage(msg) {
    "use strict";
//    var p = $("<p>").append(msg);
//    $(".message p").replaceWith(p);
//    $(".message").show("slow");
}

function removeTerminal(data) {
    "use strict";
    $("#" + data.id).remove();
    if ($(".terminal").length === 0 && $(".card").length === 0 && $(".selected").length === 0) {
	showMessage("Please connect Terminal.");
    }
}

function getCardImage(data) {
    "use strict";
    switch (data.cardType) {
    case "http://ws.gematik.de/egk/1.0.0":
	return "static/img/egk.jpg";
    case "http://bsi.bund.de/cif/npa.xml":
	return "static/img/npa.jpg";
    default:
	return "static/img/unknown_card.jpg";
    }
}

function updateCard(data) {
    "use strict";
    eventHandler[data.id] = true;
    $("#" + data.id).attr("class", "card");
    $("#" + data.id + " img").attr("src", getCardImage(data))
	.attr("alt", data.cardType)
	.off()
	.one("click", function () {
	    onClick(data);
	});
}

function back() {
    "use strict";
    window.location.reload(true);
}

function removeCard(data) {
    "use strict";
    if (data.id === selectedId) {
	selectedId = null;
	back();
    } else {
	$("#" + data.id + " img").off();
	updateTerminal(data);
    }
}

function signalEvent(eventData) {
    "use strict";
    var data = JSON.parse(eventData);
    if (cardSelected === false) {
	switch (data.eventType) {
	case "TERMINAL_ADDED":
	    addTerminal(data);
	    break;
	case "TERMINAL_REMOVED":
	    removeTerminal(data);
	    break;
	case "CARD_INSERTED":
	    updateCard(data);
	    break;
	case "CARD_RECOGNIZED":
	    updateCard(data);
	    break;
	case "CARD_REMOVED":
	    removeCard(data);
	    break;
	default:
	    break;
	}
    } else {
	if (data.eventType === "CARD_REMOVED" && data.id === selectedId) {
	    removeCard(data);
	}
    }
}
