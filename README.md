# mp3-stream
Playing streams with the Audio Web API using either a websocket or an audio tag from a rest service.

Server in Java (Vert.x)
Client in JavaScript

1) Websockets: 

	Limited to one connection at a time.
	The audio is not blending fluently (frequent glitches each time a new chunk is loaded.    
	
2) Audio tag 
	
	Streams start over whenever requested. There is not a background stream clients can attach to. 
	This later is not difficult to implement though, it follows similar principles. 
	


