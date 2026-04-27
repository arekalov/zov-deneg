#!/usr/bin/env python3
"""
Test script for Securities Service GET /securities/{id} endpoint.
Tests all securities from the list endpoint.
"""

import requests
import json
from datetime import datetime

# Service URLs
USER_SERVICE_URL = "http://localhost:8080"
SECURITIES_SERVICE_URL = "http://localhost:8081"


def create_user():
    """Create a new user in user service and return access token."""
    print("\n" + "=" * 60)
    print("Step 1: Creating user in User Service...")
    print("=" * 60)

    # Generate unique user data
    timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
    user_data = {
        "firstName": "Test",
        "lastName": "User",
        "email": f"test{timestamp}@example.com",
        "phone": f"+79004636193",
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


def get_all_securities(token):
    """Get all securities from the securities service."""
    print("\n" + "=" * 60)
    print("Step 2: Getting all securities...")
    print("=" * 60)

    try:
        headers = {"Authorization": f"Bearer {token}"} if token else {}

        all_securities = []
        page = 1
        page_size = 100

        while True:
            response = requests.get(
                f"{SECURITIES_SERVICE_URL}/securities",
                params={"page": page, "pageSize": page_size},
                headers=headers
            )

            if response.status_code != 200:
                print(f"❌ Failed to get securities: {response.status_code}")
                print(f"   Response: {response.text}")
                return None

            data = response.json()
            securities = data.get("data", [])
            all_securities.extend(securities)

            pagination = data.get("pagination", {})
            total_pages = pagination.get("totalPages", 1)

            print(f"   Page {page}/{total_pages}: fetched {len(securities)} securities")

            if page >= total_pages:
                break
            page += 1

        print(f"\n✅ Found {len(all_securities)} securities")
        return all_securities

    except requests.exceptions.ConnectionError:
        print(f"❌ Cannot connect to Securities Service at {SECURITIES_SERVICE_URL}")
        print("   Make sure the service is running: docker-compose up -d")
        return None
    except Exception as e:
        print(f"❌ Error: {e}")
        return None


def test_security_by_id(security, token):
    """Test GET /securities/{id} for a single security."""
    security_id = security.get("id")
    ticker = security.get("ticker")
    name = security.get("name")

    try:
        headers = {"Authorization": f"Bearer {token}"} if token else {}

        response = requests.get(
            f"{SECURITIES_SERVICE_URL}/securities/{security_id}",
            headers=headers
        )

        if response.status_code == 200:
            data = response.json()
            print(f"   ✅ {ticker} ({name}): OK")
            return True
        elif response.status_code == 404:
            print(f"   ⚠️  {ticker} ({name}): NOT FOUND (404)")
            return False
        else:
            print(f"   ❌ {ticker} ({name}): FAILED ({response.status_code})")
            print(f"       Response: {response.text}")
            return False

    except requests.exceptions.ConnectionError:
        print(f"   ❌ {ticker} ({name}): Connection error")
        return False
    except Exception as e:
        print(f"   ❌ {ticker} ({name}): Error - {e}")
        return False


def test_invalid_uuid(token):
    """Test GET /securities/{id} with invalid UUID format."""
    print("\n" + "=" * 60)
    print("Bonus: Testing invalid UUID format...")
    print("=" * 60)

    try:
        headers = {"Authorization": f"Bearer {token}"} if token else {}

        response = requests.get(
            f"{SECURITIES_SERVICE_URL}/securities/not-a-uuid",
            headers=headers
        )

        if response.status_code == 400:
            print(f"   ✅ Invalid UUID correctly returns 400 Bad Request")
            return True
        else:
            print(f"   ⚠️  Invalid UUID returned {response.status_code} (expected 400)")
            return False

    except Exception as e:
        print(f"   ❌ Error testing invalid UUID: {e}")
        return False


def test_nonexistent_id(token):
    """Test GET /securities/{id} with non-existent UUID."""
    print("\n" + "=" * 60)
    print("Bonus: Testing non-existent UUID...")
    print("=" * 60)

    try:
        headers = {"Authorization": f"Bearer {token}"} if token else {}

        response = requests.get(
            f"{SECURITIES_SERVICE_URL}/securities/00000000-0000-0000-0000-000000000000",
            headers=headers
        )

        if response.status_code == 404:
            print(f"   ✅ Non-existent UUID correctly returns 404 Not Found")
            return True
        else:
            print(f"   ⚠️  Non-existent UUID returned {response.status_code} (expected 404)")
            return False

    except Exception as e:
        print(f"   ❌ Error testing non-existent UUID: {e}")
        return False


def main():
    """Main execution flow."""
    print("\n" + "=" * 60)
    print("  Securities Service - GET /securities/{id} Test")
    print("=" * 60)
    print(f"User Service: {USER_SERVICE_URL}")
    print(f"Securities Service: {SECURITIES_SERVICE_URL}")
    print("=" * 60)

    # Step 1: Create user
    token, user_id = create_user()

    if not token:
        print("\n" + "=" * 60)
        print("  ❌ Test failed - could not create user")
        print("=" * 60)
        return

    # Step 2: Get all securities
    securities = get_all_securities(token)

    if not securities:
        print("\n" + "=" * 60)
        print("  ❌ Test failed - could not get securities list")
        print("=" * 60)
        return

    # Step 3: Test each security by ID
    print("\n" + "=" * 60)
    print("Step 3: Testing GET /securities/{id} for each security...")
    print("=" * 60)

    results = {
        "success": 0,
        "failed": 0,
        "not_found": 0
    }

    for security in securities:
        success = test_security_by_id(security, token)
        if success:
            results["success"] += 1
        else:
            # Check if it was 404
            security_id = security.get("id")
            try:
                headers = {"Authorization": f"Bearer {token}"} if token else {}
                response = requests.get(
                    f"{SECURITIES_SERVICE_URL}/securities/{security_id}",
                    headers=headers
                )
                if response.status_code == 404:
                    results["not_found"] += 1
                else:
                    results["failed"] += 1
            except:
                results["failed"] += 1

    # Step 4: Bonus tests
    test_invalid_uuid(token)
    test_nonexistent_id(token)

    # Summary
    print("\n" + "=" * 60)
    print("  Test Summary")
    print("=" * 60)
    print(f"  Total securities tested: {len(securities)}")
    print(f"  ✅ Successful: {results['success']}")
    print(f"  ⚠️  Not Found:   {results['not_found']}")
    print(f"  ❌ Failed:       {results['failed']}")
    print("=" * 60)

    if results['failed'] == 0 and results['not_found'] == 0:
        print("\n  🎉 All tests passed!")
    elif results['failed'] == 0:
        print("\n  ⚠️  Some securities not found (may be expected)")
    else:
        print("\n  ❌ Some tests failed")


if __name__ == "__main__":
    main()
