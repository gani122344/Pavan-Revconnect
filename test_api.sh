#!/bin/bash
BODY='{"usernameOrEmail":"ganesh","password":"Chgani123"}'
RESP=$(curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "$BODY")
TOKEN=$(echo "$RESP" | python3 -c 'import json,sys;d=json.load(sys.stdin);print(d["data"]["accessToken"])')

if [ -z "$TOKEN" ]; then
  echo "Login failed: $RESP"
  exit 1
fi

echo "=== NOTIFICATIONS ==="
curl -s "http://localhost:8080/api/notifications?page=0" -H "Authorization: Bearer $TOKEN" > /tmp/notif.json
python3 << 'PYEOF'
import json
with open("/tmp/notif.json") as f:
    d = json.load(f)
for n in d["data"]["content"][:3]:
    print(n["message"][:50], "=>", repr(n.get("createdAt")))
PYEOF

echo "=== FEED POSTS ==="
curl -s "http://localhost:8080/api/posts/feed?page=0&size=3" -H "Authorization: Bearer $TOKEN" > /tmp/posts.json
python3 << 'PYEOF'
import json
with open("/tmp/posts.json") as f:
    d = json.load(f)
for p in d["data"]["content"][:3]:
    print(str(p.get("content",""))[:30], "=>", repr(p.get("createdAt")))
PYEOF

echo "=== MESSAGES ==="
curl -s "http://localhost:8080/api/messages/conversation/1?page=0&size=3" -H "Authorization: Bearer $TOKEN" > /tmp/msgs.json
python3 << 'PYEOF'
import json
with open("/tmp/msgs.json") as f:
    d = json.load(f)
if isinstance(d.get("data"), list):
    items = d["data"][:3]
elif isinstance(d.get("data"), dict) and "content" in d["data"]:
    items = d["data"]["content"][:3]
else:
    items = []
for m in items:
    print(str(m.get("content",""))[:30], "=>", repr(m.get("timestamp")))
PYEOF
