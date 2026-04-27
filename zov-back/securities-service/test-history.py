import requests
import json
from datetime import datetime, timedelta, timezone

# Service URLs
USER_SERVICE_URL = "http://localhost:8080"
SECURITIES_SERVICE_URL = "http://localhost:8081"

def create_user():
    """Create a new user in user service and return access token."""
    print("=" * 60)
    print("Step 1: Creating user in User Service...")
    print("=" * 60)

    # Generate unique user data
    timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
    user_data = {
        "firstName": "Test",
        "lastName": "User",
        "email": f"test{timestamp}@example.com",
        "phone": f"+7900{timestamp[-6:]}",
        "password": "password123"
    }

    print(f"User data: {json.dumps(user_data, indent=2)}")

    try:
        response = requests.post(
            f"{USER_SERVICE_URL}/auth/register",
            json=user_data,
            headers={"Content-Type": "application/json"}
        )

        if response.status_code == 201:
            data = response.json()
            token = data.get("tokens", {}).get("accessToken")
            user_id = data.get("user", {}).get("id")

            print(f"✅ User created successfully!")
            print(f"   User ID: {user_id}")
            print(f"   Access Token: {token[:50]}...")

            return token, user_id
        else:
            print(f"❌ Failed to create user: {response.status_code}")
            print(f"   Response: {response.text}")
            return None, None

    except requests.exceptions.ConnectionError:
        print(f"❌ Cannot connect to User Service at {USER_SERVICE_URL}")
        print("   Make sure the service is running: docker-compose up -d")
        return None, None
    except Exception as e:
        print(f"❌ Error: {e}")
        return None, None


def get_sber_security_id(token):
    """Get SBER security ID from securities service."""
    print("\n" + "=" * 60)
    print("Step 2: Getting SBER security ID...")
    print("=" * 60)

    try:
        headers = {"Authorization": f"Bearer {token}"} if token else {}

        response = requests.get(
            f"{SECURITIES_SERVICE_URL}/securities",
            params={"q": "SBER"},
            headers=headers
        )

        if response.status_code == 200:
            data = response.json()
            securities = data.get("data", [])

            if securities:
                sber = next((s for s in securities if s.get("ticker") == "SBER"), securities[0])
                security_id = sber.get("id")

                print(f"✅ Found SBER security!")
                print(f"   ID: {security_id}")
                print(f"   Ticker: {sber.get('ticker')}")
                print(f"   Name: {sber.get('name')}")

                return security_id
            else:
                print("❌ No securities found matching 'SBER'")
                return None
        else:
            print(f"❌ Failed to get securities: {response.status_code}")
            print(f"   Response: {response.text}")
            return None

    except requests.exceptions.ConnectionError:
        print(f"❌ Cannot connect to Securities Service at {SECURITIES_SERVICE_URL}")
        print("   Make sure the service is running: docker-compose up -d")
        return None
    except Exception as e:
        print(f"❌ Error: {e}")
        return None


def fetch_price_history(security_id, token):
    """Fetch price history for the given security ID."""
    print("\n" + "=" * 60)
    print("Step 3: Fetching price history...")
    print("=" * 60)

    # Calculate time range (last 24 hours)
    # Set specific time range
    from_ts = int(datetime(2024, 1, 15, 9, 0, 0, tzinfo=timezone.utc).timestamp())
    to_ts = int(datetime(2024, 1, 15, 11, 0, 0, tzinfo=timezone.utc).timestamp())

    print(f"Time range (UTC): {datetime.fromtimestamp(from_ts, tz=timezone.utc)} to {datetime.fromtimestamp(to_ts, tz=timezone.utc)}")

    try:
        headers = {"Authorization": f"Bearer {token}"} if token else {}

        response = requests.get(
            f"{SECURITIES_SERVICE_URL}/securities/{security_id}/price/history",
            params={"from": from_ts, "to": to_ts},
            headers=headers
        )

        print(f"\n📊 Response Status: {response.status_code}")

        if response.status_code == 200:
            data = response.json()
            print(f"✅ Price history retrieved successfully!")
            print(f"\n📈 Data:")
            print(json.dumps(data, indent=2))

            # Show summary
            if isinstance(data, dict):
                if "data" in data and isinstance(data["data"], list):
                    print(f"\n📋 Summary:")
                    print(f"   Records returned: {len(data['data'])}")
                    if data["data"]:
                        print(f"   First record: {data['data'][0]}")
                        print(f"   Last record: {data['data'][-1]}")

            return data
        else:
            print(f"❌ Failed to fetch price history: {response.status_code}")
            print(f"   Response: {response.text}")
            return None

    except requests.exceptions.ConnectionError:
        print(f"❌ Cannot connect to Securities Service at {SECURITIES_SERVICE_URL}")
        return None
    except Exception as e:
        print(f"❌ Error: {e}")
        return None


def main():
    """Main execution flow."""
    print("\n" + "=" * 60)
    print("  Securities Service - Price History Test")
    print("=" * 60)
    print(f"User Service: {USER_SERVICE_URL}")
    print(f"Securities Service: {SECURITIES_SERVICE_URL}")
    print("=" * 60)

    # Step 1: Create user
    token, user_id = create_user()

    # Step 2: Get SBER security ID
    security_id = get_sber_security_id(token)

    # Step 3: Fetch price history
    if security_id:
        history = fetch_price_history(security_id, token)

        if history:
            print("\n" + "=" * 60)
            print("  ✅ Test completed successfully!")
            print("=" * 60)
        else:
            print("\n" + "=" * 60)
            print("  ⚠️ Test completed with warnings")
            print("=" * 60)
    else:
        print("\n" + "=" * 60)
        print("  ❌ Test failed - could not get security ID")
        print("=" * 60)


if __name__ == "__main__":
    main()
