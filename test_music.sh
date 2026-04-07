#!/bin/bash
echo "=== Testing iTunes API ==="
curl -s 'https://itunes.apple.com/search?term=Koluvaithiva+Rangasayi&media=music&limit=3' | python3 -c '
import sys, json
d = json.load(sys.stdin)
print("resultCount:", d["resultCount"])
for r in d["results"]:
    print("track:", r.get("trackName","?"), "| preview:", (r.get("previewUrl","NONE") or "NONE")[:80])
'

echo ""
echo "=== Testing backend proxy ==="
curl -s 'http://localhost:8080/api/music/search?query=Koluvaithiva%20Rangasayi' | python3 -c '
import sys, json
d = json.load(sys.stdin)
print("resultCount:", d.get("resultCount",0))
for r in d.get("results",[]):
    print("track:", r.get("trackName","?"), "| preview:", (r.get("previewUrl","NONE") or "NONE")[:80])
'
