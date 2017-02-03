/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any 
 * purpose with or without fee is hereby granted, provided that the above 
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES 
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF 
 * MERCHANTABILITY  AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR 
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN 
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF 
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *******************************************************************************/
var websocket;

// Enable websocket to receive binaries from server
function connect()
{
	if (window.WebSocket) 
	{
		websocket = new WebSocket('ws://localhost:8081/audio-stream');
		websocket.binaryType = "arraybuffer";
		
		websocket.onmessage = function(event) 
		{
			play(event.data);			
		};
	}
}

var AudioContext = window.AudioContext || window.webkitAudioContext;
var context = new AudioContext();

var sync = 0;

// Play the audio.
function play(audio) 
{
	var source = context.createBufferSource();
	context.decodeAudioData(audio, function(data) 
	{
		source.buffer = data;
		source.connect(context.destination);

		sync = sync < context.currentTime ? context.currentTime + 0.1 : sync;
		source.start(sync);
		sync += source.buffer.duration;
	}, function(e) 
	{
		console.log(e);
	});
}