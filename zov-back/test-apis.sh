#!/usr/bin/env bash
set -euo pipefail

# =============================================================================
# ЗОВ Денег — API Test Script
# Tests both User Service (8080) and Securities Service (8081)
# =============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TESTS_PASSED=0
TESTS_FAILED=0
TESTS_TOTAL=0

# Base URLs
USER_SERVICE_URL="http://localhost:8080"
SECURITIES_SERVICE_URL="http://localhost:8081"

# Test data - use unique phone for each run to avoid conflicts
TEST_EMAIL="test${RANDOM}@example.com"
TEST_PHONE="+7999999${RANDOM:0:4}"
TEST_PASSWORD="TestPassword123!"
TEST_FIRST_NAME="Иван"
TEST_LAST_NAME="Иванов"

# Tokens (will be set after login)
ACCESS_TOKEN=""
REFRESH_TOKEN=""
USER_ID=""
SECURITY_ID=""

# =============================================================================
# Helper Functions
# =============================================================================

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((TESTS_PASSED++)) || true
    ((TESTS_TOTAL++)) || true
}

log_failure() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((TESTS_FAILED++)) || true
    ((TESTS_TOTAL++)) || true
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_section() {
    echo -e "\n${YELLOW}═══════════════════════════════════════════════════════════${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${YELLOW}═══════════════════════════════════════════════════════════${NC}\n"
}

# Make HTTP request and check status
make_request() {
    local method="$1"
    local url="$2"
    local expected_status="${3:-200}"
    local body="${4:-}"
    
    local curl_args=(-s -w "\n%{http_code}" -X "$method")
    
    if [[ -n "$body" ]]; then
        curl_args+=(-H "Content-Type: application/json")
        curl_args+=(-d "$body")
    fi
    
    if [[ -n "$ACCESS_TOKEN" ]]; then
        curl_args+=(-H "Authorization: Bearer $ACCESS_TOKEN")
    fi
    
    curl_args+=("$url")
    
    local response
    response=$(curl "${curl_args[@]}")
    
    local http_code
    http_code=$(echo "$response" | tail -n1)
    local response_body
    response_body=$(echo "$response" | sed '$d')
    
    if [[ "$http_code" == "$expected_status" ]]; then
        log_success "$method $url — Status: $http_code"
        echo "$response_body"
        return 0
    else
        log_failure "$method $url — Expected: $expected_status, Got: $http_code"
        echo "Response: $response_body"
        return 0
    fi
}

# Extract JSON field value using sed (works on macOS)
extract_json_field() {
    local json="$1"
    local field="$2"
    echo "$json" | sed -n "s/.*\"$field\"[[:space:]]*:[[:space:]]*\"\([^\"]*\)\".*/\1/p" | head -1
}

# =============================================================================
# User Service Tests (Port 8080)
# =============================================================================

test_user_service() {
    log_section "USER SERVICE TESTS (localhost:8080)"

    # -------------------------------------------------------------------------
    # Auth Endpoints
    # -------------------------------------------------------------------------
    log_section "1. AUTH ENDPOINTS"

    # POST /auth/register
    log_info "Testing POST /auth/register..."
    local register_response
    register_response=$(make_request "POST" "$USER_SERVICE_URL/auth/register" "201" \
        "{\"firstName\":\"$TEST_FIRST_NAME\",\"lastName\":\"$TEST_LAST_NAME\",\"email\":\"$TEST_EMAIL\",\"phone\":\"$TEST_PHONE\",\"password\":\"$TEST_PASSWORD\"}")

    if [[ -n "$register_response" ]]; then
        # Extract tokens from nested JSON structure: { "user": {...}, "tokens": {...} }
        ACCESS_TOKEN=$(extract_json_field "$register_response" "accessToken")
        REFRESH_TOKEN=$(extract_json_field "$register_response" "refreshToken")
        USER_ID=$(extract_json_field "$register_response" "id")
        
        if [[ -n "$ACCESS_TOKEN" && -n "$USER_ID" ]]; then
            log_success "Registration successful, tokens received"
        else
            log_failure "Failed to extract tokens from response"
            log_info "Response: $register_response"
        fi
    fi

    # POST /auth/login - always do login to ensure we have valid tokens
    log_info "Testing POST /auth/login..."
    local login_response
    login_response=$(make_request "POST" "$USER_SERVICE_URL/auth/login" "200" \
        "{\"phone\":\"$TEST_PHONE\",\"password\":\"$TEST_PASSWORD\"}")

    if [[ -n "$login_response" ]]; then
        ACCESS_TOKEN=$(extract_json_field "$login_response" "accessToken")
        REFRESH_TOKEN=$(extract_json_field "$login_response" "refreshToken")
        USER_ID=$(extract_json_field "$login_response" "id")
        if [[ -n "$ACCESS_TOKEN" ]]; then
            log_success "Login successful, tokens received"
        else
            log_failure "Failed to extract tokens from login response"
            log_info "Response: $login_response"
        fi
    fi

    # POST /auth/token/refresh
    log_info "Testing POST /auth/token/refresh..."
    if [[ -n "$REFRESH_TOKEN" ]]; then
        make_request "POST" "$USER_SERVICE_URL/auth/token/refresh" "200" \
            "{\"refreshToken\":\"$REFRESH_TOKEN\"}"
    fi

    # -------------------------------------------------------------------------
    # Users Endpoints
    # -------------------------------------------------------------------------
    log_section "2. USERS ENDPOINTS"

    # GET /users/me
    log_info "Testing GET /users/me..."
    make_request "GET" "$USER_SERVICE_URL/users/me" "200"

    # PUT /users/me
    log_info "Testing PUT /users/me..."
    make_request "PUT" "$USER_SERVICE_URL/users/me" "200" \
        "{\"firstName\":\"Иван Обновленный\",\"lastName\":\"Иванов\",\"email\":\"$TEST_EMAIL\",\"phone\":\"$TEST_PHONE\"}"

    # GET /users (admin only - will return 403 without admin role)
    log_info "Testing GET /users (admin endpoint)..."
    make_request "GET" "$USER_SERVICE_URL/users" "403"

    # GET /users/{userId} (admin only - will return 403 without admin role)
    log_info "Testing GET /users/{userId} (admin endpoint)..."
    if [[ -n "$USER_ID" ]]; then
        make_request "GET" "$USER_SERVICE_URL/users/$USER_ID" "403"
    fi

    # -------------------------------------------------------------------------
    # Portfolio Endpoints
    # -------------------------------------------------------------------------
    log_section "3. PORTFOLIO ENDPOINTS"

    # GET /portfolio
    log_info "Testing GET /portfolio..."
    make_request "GET" "$USER_SERVICE_URL/portfolio" "200"

    # GET /portfolio/summary
    log_info "Testing GET /portfolio/summary..."
    make_request "GET" "$USER_SERVICE_URL/portfolio/summary" "200"

    # -------------------------------------------------------------------------
    # Orders Endpoints
    # -------------------------------------------------------------------------
    log_section "4. ORDERS ENDPOINTS"

    # GET /orders
    log_info "Testing GET /orders..."
    make_request "GET" "$USER_SERVICE_URL/orders" "200"

    # POST /orders (create order - may succeed even with invalid security ID)
    log_info "Testing POST /orders..."
    make_request "POST" "$USER_SERVICE_URL/orders" "201" \
        "{\"securityId\":\"00000000-0000-0000-0000-000000000000\",\"side\":\"buy\",\"quantity\":10}" || true

    # -------------------------------------------------------------------------
    # Transactions Endpoints
    # -------------------------------------------------------------------------
    log_section "5. TRANSACTIONS ENDPOINTS"

    # GET /transactions
    log_info "Testing GET /transactions..."
    make_request "GET" "$USER_SERVICE_URL/transactions" "200"

    # -------------------------------------------------------------------------
    # Balance Endpoints
    # -------------------------------------------------------------------------
    log_section "6. BALANCE ENDPOINTS"

    # GET /balance
    log_info "Testing GET /balance..."
    make_request "GET" "$USER_SERVICE_URL/balance" "200"

    # POST /balance/deposit
    log_info "Testing POST /balance/deposit..."
    make_request "POST" "$USER_SERVICE_URL/balance/deposit" "200" \
        "{\"amount\":\"10000.00\"}"

    # POST /balance/withdraw
    log_info "Testing POST /balance/withdraw..."
    make_request "POST" "$USER_SERVICE_URL/balance/withdraw" "200" \
        "{\"amount\":\"1000.00\"}"

    # GET /balance (after operations)
    log_info "Testing GET /balance (after deposit/withdraw)..."
    make_request "GET" "$USER_SERVICE_URL/balance" "200"
}

# =============================================================================
# Securities Service Tests (Port 8081)
# =============================================================================

test_securities_service() {
    log_section "SECURITIES SERVICE TESTS (localhost:8081)"

    # -------------------------------------------------------------------------
    # Securities Endpoints
    # -------------------------------------------------------------------------
    log_section "1. SECURITIES ENDPOINTS"

    # GET /securities (list all)
    log_info "Testing GET /securities..."
    local securities_response
    securities_response=$(make_request "GET" "$SECURITIES_SERVICE_URL/securities" "200")

    # Try to extract a security ID for further tests
    if [[ -n "$securities_response" ]]; then
        SECURITY_ID=$(extract_json_field "$securities_response" "id")
        if [[ -n "$SECURITY_ID" ]]; then
            log_success "Found security ID: $SECURITY_ID"
        else
            log_warning "No security ID found in response"
        fi
    fi

    # GET /securities with search query
    log_info "Testing GET /securities?q=Сбер..."
    make_request "GET" "$SECURITIES_SERVICE_URL/securities?q=Сбер" "200"

    # GET /securities with type filter
    log_info "Testing GET /securities?type=stock..."
    make_request "GET" "$SECURITIES_SERVICE_URL/securities?type=stock" "200"

    # GET /securities with exchange filter
    log_info "Testing GET /securities?exchange=MOEX..."
    make_request "GET" "$SECURITIES_SERVICE_URL/securities?exchange=MOEX" "200"

    # GET /securities/{securityId}
    log_info "Testing GET /securities/{securityId}..."
    if [[ -n "$SECURITY_ID" ]]; then
        # May return 500 due to division by zero if day_open_price is 0 (known issue)
        make_request "GET" "$SECURITIES_SERVICE_URL/securities/$SECURITY_ID" "200" || log_warning "Known issue: Division by zero in price change calculation"
    else
        log_warning "Skipping - no security ID available"
    fi

    # GET /securities/{securityId}/price/history
    log_info "Testing GET /securities/{securityId}/price/history..."
    if [[ -n "$SECURITY_ID" ]]; then
        local now
        now=$(date +%s)
        local yesterday=$((now - 86400))
        make_request "GET" "$SECURITIES_SERVICE_URL/securities/$SECURITY_ID/price/history?from=$yesterday&to=$now" "200"
    else
        log_warning "Skipping - no security ID available"
    fi

    # GET /securities/{securityId}/orderbook
    log_info "Testing GET /securities/{securityId}/orderbook..."
    if [[ -n "$SECURITY_ID" ]]; then
        make_request "GET" "$SECURITIES_SERVICE_URL/securities/$SECURITY_ID/orderbook" "200"
    else
        log_warning "Skipping - no security ID available"
    fi

    # GET /securities/{securityId}/orderbook with depth
    log_info "Testing GET /securities/{securityId}/orderbook?depth=5..."
    if [[ -n "$SECURITY_ID" ]]; then
        make_request "GET" "$SECURITIES_SERVICE_URL/securities/$SECURITY_ID/orderbook?depth=5" "200"
    else
        log_warning "Skipping - no security ID available"
    fi
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    echo -e "${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║         ЗОВ Денег — API Test Suite                       ║${NC}"
    echo -e "${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    log_info "User Service: $USER_SERVICE_URL"
    log_info "Securities Service: $SECURITIES_SERVICE_URL"
    log_info "Test Email: $TEST_EMAIL"
    log_info "Test Phone: $TEST_PHONE"
    echo ""

    # Check if services are running
    log_info "Checking if services are running..."

    if ! curl -s -o /dev/null -w "%{http_code}" "$USER_SERVICE_URL" > /dev/null 2>&1; then
        log_warning "User Service may not be running on $USER_SERVICE_URL"
    fi

    if ! curl -s -o /dev/null -w "%{http_code}" "$SECURITIES_SERVICE_URL" > /dev/null 2>&1; then
        log_warning "Securities Service may not be running on $SECURITIES_SERVICE_URL"
    fi

    # Run tests
    test_user_service
    test_securities_service

    # Summary
    log_section "TEST SUMMARY"
    echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
    echo -e "${RED}Failed: $TESTS_FAILED${NC}"
    echo -e "${BLUE}Total:  $TESTS_TOTAL${NC}"

    if [[ $TESTS_FAILED -eq 0 ]]; then
        echo -e "\n${GREEN}All tests passed! ✓${NC}"
        exit 0
    else
        echo -e "\n${RED}Some tests failed! ✗${NC}"
        exit 1
    fi
}

main "$@"
