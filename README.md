PATCH operation example
-----------------------

Usage:

curl -X PATCH -H "Content-Type: text/json" -d '{"op":"put", "path":"headliner/name", "value":"This will be the new headliner name"}' http://localhost:9000/events
