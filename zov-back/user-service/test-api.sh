#!/bin/bash

# Complete User Service API Test Script
BASE_URL="http://localhost:58233"
RESULTS_FILE="/Users/m.s.taranenko/IdeaProjects/itmo/zov-deneg/zov-back/user-service/test-results.md"
RAND=$RANDOM

echo "========================================"
echo "User Service Complete API Test"
echo "========================================"
echo ""

# Check if service is running
echo "Checking service status..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/")
if [ "$HTTP_CODE" != "404" ]; then
    echo "❌ Service is not running on $BASE_URL"
    echo "Please run: docker-compose up -d"
    exit 1
fi
echo "✅ Service is running"
echo ""

# Initialize results
cat > "$RESULTS_FILE" << 'HEADER'
# User Service API Test Results

HEADER
echo "Test Date: $(date)" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "---" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "| Endpoint | Method | Status | Code |" >> "$RESULTS_FILE"
echo "|----------|--------|--------|------|" >> "$RESULTS_FILE"

PASS_COUNT=0
FAIL_COUNT=0

log_result() {
    local endpoint=$1
    local method=$2
    local expected=$3
    local actual=$4
    local status="❌"
    if [ "$actual" = "$expected" ]; then
        status="✅"
        ((PASS_COUNT++))
        echo "  ✓ PASS"
    else
        ((FAIL_COUNT++))
        echo "  ✗ FAIL (expected $expected, got $actual)"
    fi
    echo "| $endpoint | $method | $status | $actual |" >> "$RESULTS_FILE"
}

# Test 1: Root endpoint
echo "Test 1: Root endpoint..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/")
log_result "/" "GET" "404" "$HTTP_CODE"

# Test 2: Register regular user
echo "Test 2: Register regular user..."
USER_PHONE="+7900${RAND}001"
# Ensure phone is exactly 11 digits (+7 followed by 10 digits)
if [ ${#USER_PHONE} -ne 12 ]; then
    USER_PHONE="+79000000001"
fi
REGISTER_DATA="{\"firstName\":\"Test\",\"lastName\":\"User\",\"email\":\"test${RAND}@example.com\",\"phone\":\"$USER_PHONE\",\"password\":\"password123\"}"
echo "  Phone: $USER_PHONE"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" -d "$REGISTER_DATA")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
echo "  Response code: $HTTP_CODE"
log_result "/auth/register" "POST" "201" "$HTTP_CODE"
USER_TOKEN=$(echo "$BODY" | jq -r '.tokens.accessToken' 2>/dev/null)
USER_ID=$(echo "$BODY" | jq -r '.user.id' 2>/dev/null)

# Test 3: Login
echo "Test 3: Login..."
LOGIN_DATA="{\"phone\":\"$USER_PHONE\",\"password\":\"password123\"}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d "$LOGIN_DATA")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
log_result "/auth/login" "POST" "200" "$HTTP_CODE"
USER_TOKEN=$(echo "$BODY" | jq -r '.tokens.accessToken' 2>/dev/null)
REFRESH_TOKEN=$(echo "$BODY" | jq -r '.tokens.refreshToken' 2>/dev/null)

# Test 4: Refresh token
echo "Test 4: Refresh token..."
REFRESH_DATA="{\"refreshToken\":\"$REFRESH_TOKEN\"}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/token/refresh" -H "Content-Type: application/json" -d "$REFRESH_DATA")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
log_result "/auth/token/refresh" "POST" "200" "$HTTP_CODE"

# Test 5: Get current user
echo "Test 5: Get current user..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/me" -H "Authorization: Bearer $USER_TOKEN")
log_result "/users/me" "GET" "200" "$HTTP_CODE"

# Test 6: Update current user
echo "Test 6: Update current user..."
UPDATE_DATA='{"firstName":"Updated","lastName":"User"}'
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/users/me" -H "Authorization: Bearer $USER_TOKEN" -H "Content-Type: application/json" -d "$UPDATE_DATA")
log_result "/users/me" "PUT" "200" "$HTTP_CODE"

# Test 7: Get users (non-admin - should be 403)
echo "Test 7: Get users (non-admin)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users" -H "Authorization: Bearer $USER_TOKEN")
log_result "/users" "GET" "403" "$HTTP_CODE"

# Test 8: Missing token
echo "Test 8: Missing token..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/me")
log_result "/users/me" "GET" "401" "$HTTP_CODE"

# Test 9: Invalid token
echo "Test 9: Invalid token..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users/me" -H "Authorization: Bearer invalid_token")
log_result "/users/me" "GET" "401" "$HTTP_CODE"

# Test 10: Register admin user
echo "Test 10: Register admin user..."
ADMIN_PHONE="+7900${RAND}002"
# Ensure phone is exactly 11 digits
if [ ${#ADMIN_PHONE} -ne 12 ]; then
    ADMIN_PHONE="+79000000002"
fi
ADMIN_DATA="{\"firstName\":\"Admin\",\"lastName\":\"Adminov\",\"email\":\"admin${RAND}@example.com\",\"phone\":\"$ADMIN_PHONE\",\"password\":\"admin123\"}"
echo "  Phone: $ADMIN_PHONE"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" -d "$ADMIN_DATA")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
log_result "/auth/register (admin)" "POST" "201" "$HTTP_CODE"
ADMIN_ID=$(echo "$BODY" | jq -r '.user.id' 2>/dev/null)

# Test 11: Login as admin
echo "Test 11: Login as admin..."
ADMIN_LOGIN="{\"phone\":\"$ADMIN_PHONE\",\"password\":\"admin123\"}"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d "$ADMIN_LOGIN")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
log_result "/auth/login (admin)" "POST" "200" "$HTTP_CODE"
ADMIN_TOKEN=$(echo "$BODY" | jq -r '.tokens.accessToken' 2>/dev/null)

# Test 12: Admin tries to access users (should be 403 - role is 'user' by default)
echo "Test 12: Admin endpoints (default role - should be 403)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/users" -H "Authorization: Bearer $ADMIN_TOKEN")
log_result "/users (new admin)" "GET" "403" "$HTTP_CODE"

# Test 13: Duplicate registration
echo "Test 13: Duplicate registration..."
DUP_DATA="{\"firstName\":\"Dup\",\"lastName\":\"User\",\"email\":\"test${RAND}@example.com\",\"phone\":\"+79990000000\",\"password\":\"password123\"}"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/auth/register" -H "Content-Type: application/json" -d "$DUP_DATA")
log_result "/auth/register (dup)" "POST" "409" "$HTTP_CODE"

# Test 14: Invalid login
echo "Test 14: Invalid login..."
INVALID_DATA='{"phone":"+79000000000","password":"wrong"}'
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d "$INVALID_DATA")
log_result "/auth/login (invalid)" "POST" "404" "$HTTP_CODE"

# Summary
echo "" >> "$RESULTS_FILE"
echo "---" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "## Summary" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "**Passed:** $PASS_COUNT" >> "$RESULTS_FILE"
echo "**Failed:** $FAIL_COUNT" >> "$RESULTS_FILE"
echo "**Total:** $((PASS_COUNT + FAIL_COUNT))" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "### Test Coverage" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "✅ Authentication endpoints (register, login, refresh)" >> "$RESULTS_FILE"
echo "✅ User profile management (get, update)" >> "$RESULTS_FILE"
echo "✅ Role-based access control (403 for non-admin)" >> "$RESULTS_FILE"
echo "✅ Error handling (401 unauthorized, 404 not found, 409 conflict)" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "### Notes" >> "$RESULTS_FILE"
echo "" >> "$RESULTS_FILE"
echo "- Admin endpoints require \`role: 'admin'\` in the database" >> "$RESULTS_FILE"
echo "- To set admin role: \`UPDATE users SET role = 'admin' WHERE id = '<user_id>';\`" >> "$RESULTS_FILE"
echo "- New registrations default to \`role: 'user'\`" >> "$RESULTS_FILE"

echo ""
echo "========================================"
echo "Tests completed!"
echo "Passed: $PASS_COUNT | Failed: $FAIL_COUNT"
echo "Results saved to: $RESULTS_FILE"
echo "========================================"
