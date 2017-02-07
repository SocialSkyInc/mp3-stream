/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
 * LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
 * OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 * PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/
var websocket;

var nextIdx = 0;
var parts = new Array();

window.setInterval(function playNext() {
	if (parts[0] && nextIdx == parts[0].idx) {
		play(parts[0].data);
		parts.splice(0, 1);
		nextIdx++;
	}
}, 50);

// Enable websocket to receive binaries from server
function connectWebsocket() {
	if (window.WebSocket) {
		websocket = new WebSocket("ws://localhost:8081/stream");
		websocket.binaryType = "arraybuffer";

		websocket.onmessage = function(event) {
			var dataView = new DataView(event.data);
			var idx = dataView.getUint32(0);
			var data = event.data.slice(4); // first 4 bytes are the position

			var part = {
				idx : idx,
				data : data
			};

			parts.push(part);
			parts.sort(function(lhs, rhs) {
				return (lhs.idx < rhs.idx) ? -1
						: ((lhs.idx == rhs.idx) ? 0 : 1);
			});
		};
	}
}

function connectUrl() {
	var audiotag = document.createElement("AUDIO");
	document.body.appendChild(audiotag);

	if (audiotag.canPlayType("audio/mpeg")) {
		audiotag.setAttribute("src", "http://localhost:8080/stream");
		audiotag.play();
	}
}

var AudioContext = window.AudioContext || window.webkitAudioContext;
var context = new AudioContext();

var sync = 0;

// Play the audio.
function play(audio) {
	context.decodeAudioData(audio, function(data) {
		var source = context.createBufferSource();
		source.buffer = data;
		source.connect(context.destination);
		sync = sync < context.currentTime ? context.currentTime : sync;
		console.log("Current sync : " + sync);
		source.start(sync);
		sync += source.buffer.duration;
		console.log("Next sync : " + sync);
	}, function(e) {
		console.log(e);
	});
}