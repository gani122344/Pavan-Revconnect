#!/bin/bash
# Login and get token
RESP=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"pavan","password":"Pavan123"}')
echo "LOGIN RESP: $RESP" | head -c 200
echo ""

TOKEN=$(echo "$RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)

if [ -z "$TOKEN" ]; then
  # Try ganesh
  RESP=$(curl -s -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"usernameOrEmail":"ganesh","password":"Ganesh123"}')
  echo "LOGIN2 RESP: $RESP" | head -c 200
  echo ""
  TOKEN=$(echo "$RESP" | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
fi

if [ -z "$TOKEN" ]; then
  echo "FAILED TO GET TOKEN"
  exit 1
fi

echo "GOT TOKEN"

# Fetch notifications
curl -s "http://localhost:8080/api/notifications?page=0&size=2" \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool 2>/dev/null | head -60
