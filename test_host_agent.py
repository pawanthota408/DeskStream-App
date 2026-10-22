"""
DeskStream Test Host Agent
Maintains device registration, sends heartbeats, and auto-accepts incoming connection requests.
"""
import sys
import time
import json
import urllib.request
import urllib.error

SYSTEM_ID = sys.argv[1] if len(sys.argv) > 1 else "461278767"
BASE_URL = "https://friendssoftwaresolutions.in/DeskStream/api"
HEADERS = {
    "Content-Type": "application/json",
    "User-Agent": "DeskStream-Agent/1.2"
}

def post_json(endpoint, data):
    url = f"{BASE_URL}/{endpoint}"
    payload = json.dumps(data).encode('utf-8')
    req = urllib.request.Request(url, data=payload, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        body = e.read().decode('utf-8', errors='ignore')
        return {"error": e.code, "body": body}
    except Exception as e:
        return {"error": str(e)}

def get_json(endpoint):
    url = f"{BASE_URL}/{endpoint}"
    req = urllib.request.Request(url, headers={"User-Agent": "DeskStream-Agent/1.2"})
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            return json.loads(response.read().decode('utf-8'))
    except Exception as e:
        return {"error": str(e)}

def register():
    print(f"[*] Registering Device ID: {SYSTEM_ID}...", flush=True)
    res = post_json("devices/register.php", {
        "system_id": SYSTEM_ID,
        "name": f"DeskStream-PC-{SYSTEM_ID}",
        "os_info": "windows"
    })
    print(f"[+] Registration result: {res.get('status') or res.get('error')}", flush=True)

def heartbeat():
    return post_json("devices/heartbeat.php", {
        "system_id": SYSTEM_ID
    })

def check_incoming():
    res = get_json(f"connections/incoming.php?system_id={SYSTEM_ID}")
    if res.get("has_request") and res.get("request"):
        req = res["request"]
        token = req.get("request_token")
        requester = req.get("requester_name", "Remote Client")
        print(f"\n[!] INCOMING CONNECTION REQUEST from: {requester} (Token: {token})", flush=True)
        print(f"[*] Auto-accepting connection request...", flush=True)
        accept_res = post_json("connections/accept.php", {
            "request_token": token,
            "target_system_id": SYSTEM_ID
        })
        print(f"[+] Acceptance status: {accept_res.get('status') or accept_res.get('error')}", flush=True)
        print(f"[+] Session established! Remote phone can now view and control.\n", flush=True)

if __name__ == "__main__":
    print(f"=== DeskStream Active Host Agent Simulation ===", flush=True)
    print(f"System ID : {SYSTEM_ID}", flush=True)
    print(f"Targeting : {BASE_URL}", flush=True)
    register()
    
    last_heartbeat = 0
    print("[*] Host Agent is now actively online and waiting for incoming requests...", flush=True)
    print("[*] (Press Ctrl+C to stop)", flush=True)
    
    try:
        while True:
            now = time.time()
            if now - last_heartbeat >= 10:
                hb = heartbeat()
                last_heartbeat = now
            check_incoming()
            time.sleep(2)
    except KeyboardInterrupt:
        print("\n[*] Stopping Host Agent.")
