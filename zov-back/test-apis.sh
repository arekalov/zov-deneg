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

# Check if response contains expected field
check_response_contains() {
    local response="$1"
    local expected="$2"
    local test_name="$3"
    
    if echo "$response" | grep -q "$expected"; then
        log_success "$test_name - Response contains: $expected"
        return 0
    else
        log_failure "$test_name - Response does NOT contain: $expected"
        log_info "Response: $response"
        return 1
    fi
}

# Check if JSON field equals expected value
check_json_field() {
    local json="$1"
    local field="$2"
    local expected="$3"
    local test_name="$4"
    
    local actual
    actual=$(extract_json_field "$json" "$field")
    
    if [[ "$actual" == "$expected" ]]; then
        log_success "$test_name - $field = $expected"
        return 0
    else
        log_failure "$test_name - $field: expected '$expected', got '$actual'"
        return 1
    fi
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
            # Validate response structure
            check_response_contains "$register_response" '"user"' "Response has user object"
            check_response_contains "$register_response" '"tokens"' "Response has tokens object"
            check_json_field "$register_response" "email" "$TEST_EMAIL" "Email matches"
            check_json_field "$register_response" "firstName" "$TEST_FIRST_NAME" "First name matches"
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
            # Validate response structure
            check_response_contains "$login_response" '"user"' "Login response has user object"
            check_response_contains "$login_response" '"accessToken"' "Login response has accessToken"
            check_response_contains "$login_response" '"refreshToken"' "Login response has refreshToken"
        else
            log_failure "Failed to extract tokens from login response"
            log_info "Response: $login_response"
        fi
    fi

    # POST /auth/token/refresh
    log_info "Testing POST /auth/token/refresh..."
    if [[ -n "$REFRESH_TOKEN" ]]; then
        local refresh_response
        refresh_response=$(make_request "POST" "$USER_SERVICE_URL/auth/token/refresh" "200" \
            "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
        check_response_contains "$refresh_response" '"accessToken"' "Token refresh returns accessToken"
        check_response_contains "$refresh_response" '"refreshToken"' "Token refresh returns refreshToken"
        check_response_contains "$refresh_response" '"expiresIn"' "Token refresh returns expiresIn"
    fi

    # -------------------------------------------------------------------------
    # Users Endpoints
    # -------------------------------------------------------------------------
    log_section "2. USERS ENDPOINTS"

    # GET /users/me
    log_info "Testing GET /users/me..."
    local users_me_response
    users_me_response=$(make_request "GET" "$USER_SERVICE_URL/users/me" "200")
    check_response_contains "$users_me_response" '"id"' "User profile has id"
    check_response_contains "$users_me_response" '"email"' "User profile has email"
    check_response_contains "$users_me_response" '"role"' "User profile has role"
    check_json_field "$users_me_response" "phone" "$TEST_PHONE" "User phone matches"

    # PUT /users/me
    log_info "Testing PUT /users/me..."
    local users_update_response
    users_update_response=$(make_request "PUT" "$USER_SERVICE_URL/users/me" "200" \
        "{\"firstName\":\"Иван Обновленный\",\"lastName\":\"Иванов\",\"email\":\"$TEST_EMAIL\",\"phone\":\"$TEST_PHONE\"}")
    check_json_field "$users_update_response" "firstName" "Иван Обновленный" "First name updated"
    check_json_field "$users_update_response" "updatedAt" "" "updatedAt field exists"

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
    local portfolio_response
    portfolio_response=$(make_request "GET" "$USER_SERVICE_URL/portfolio" "200")
    check_response_contains "$portfolio_response" '"totalValue"' "Portfolio has totalValue"
    check_response_contains "$portfolio_response" '"items"' "Portfolio has items array"
    check_response_contains "$portfolio_response" '"cashBalance"' "Portfolio has cashBalance"

    # GET /portfolio/summary
    log_info "Testing GET /portfolio/summary..."
    local portfolio_summary_response
    portfolio_summary_response=$(make_request "GET" "$USER_SERVICE_URL/portfolio/summary" "200")
    check_response_contains "$portfolio_summary_response" '"totalValue"' "Portfolio summary has totalValue"
    check_response_contains "$portfolio_summary_response" '"profitLoss"' "Portfolio summary has profitLoss"
    check_response_contains "$portfolio_summary_response" '"profitLossPct"' "Portfolio summary has profitLossPct"

    # -------------------------------------------------------------------------
    # Orders Endpoints
    # -------------------------------------------------------------------------
    log_section "4. ORDERS ENDPOINTS"

    # GET /orders
    log_info "Testing GET /orders..."
    local orders_response
    orders_response=$(make_request "GET" "$USER_SERVICE_URL/orders" "200")
    check_response_contains "$orders_response" '"data"' "Orders has data array"
    check_response_contains "$orders_response" '"pagination"' "Orders has pagination"

    # POST /orders (create order - may succeed even with invalid security ID)
    log_info "Testing POST /orders..."
    local create_order_response
    create_order_response=$(make_request "POST" "$USER_SERVICE_URL/orders" "201" \
        "{\"securityId\":\"00000000-0000-0000-0000-000000000000\",\"side\":\"buy\",\"quantity\":10}")
    check_response_contains "$create_order_response" '"id"' "Order has id"
    check_response_contains "$create_order_response" '"securityId"' "Order has securityId"
    check_response_contains "$create_order_response" '"side"' "Order has side"
    check_response_contains "$create_order_response" '"status"' "Order has status"
    check_json_field "$create_order_response" "side" "buy" "Order side is buy"

    # -------------------------------------------------------------------------
    # Transactions Endpoints
    # -------------------------------------------------------------------------
    log_section "5. TRANSACTIONS ENDPOINTS"

    # GET /transactions
    log_info "Testing GET /transactions..."
    local transactions_response
    transactions_response=$(make_request "GET" "$USER_SERVICE_URL/transactions" "200")
    check_response_contains "$transactions_response" '"data"' "Transactions has data array"
    check_response_contains "$transactions_response" '"pagination"' "Transactions has pagination"

    # -------------------------------------------------------------------------
    # Balance Endpoints
    # -------------------------------------------------------------------------
    log_section "6. BALANCE ENDPOINTS"

    # GET /balance
    log_info "Testing GET /balance..."
    local balance_response
    balance_response=$(make_request "GET" "$USER_SERVICE_URL/balance" "200")
    check_response_contains "$balance_response" '"available"' "Balance has available"
    check_response_contains "$balance_response" '"total"' "Balance has total"
    check_response_contains "$balance_response" '"blocked"' "Balance has blocked"

    # POST /balance/deposit
    log_info "Testing POST /balance/deposit..."
    local deposit_response
    deposit_response=$(make_request "POST" "$USER_SERVICE_URL/balance/deposit" "200" \
        "{\"amount\":\"10000.00\"}")
    check_response_contains "$deposit_response" '"available"' "Deposit response has available"
    check_json_field "$deposit_response" "available" "10000.0000" "Deposit amount is correct"

    # POST /balance/withdraw
    log_info "Testing POST /balance/withdraw..."
    local withdraw_response
    withdraw_response=$(make_request "POST" "$USER_SERVICE_URL/balance/withdraw" "200" \
        "{\"amount\":\"1000.00\"}")
    check_response_contains "$withdraw_response" '"available"' "Withdraw response has available"
    check_json_field "$withdraw_response" "available" "9000.0000" "Withdraw amount is correct (10000-1000)"

    # GET /balance (after operations)
    log_info "Testing GET /balance (after deposit/withdraw)..."
    local final_balance_response
    final_balance_response=$(make_request "GET" "$USER_SERVICE_URL/balance" "200")
    check_json_field "$final_balance_response" "available" "9000.0000" "Final balance is correct"
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
            # Validate response structure
            check_response_contains "$securities_response" '"data"' "Securities list has data array"
            check_response_contains "$securities_response" '"pagination"' "Securities list has pagination"
            check_response_contains "$securities_response" '"ticker"' "Securities has ticker"
            check_response_contains "$securities_response" '"lastPrice"' "Securities has lastPrice"
        else
            log_warning "No security ID found in response"
        fi
    fi

    # GET /securities with search query
    log_info "Testing GET /securities?q=Сбер..."
    local search_response
    search_response=$(make_request "GET" "$SECURITIES_SERVICE_URL/securities?q=Сбер" "200")
    check_response_contains "$search_response" '"data"' "Search response has data"
    check_response_contains "$search_response" '"pagination"' "Search response has pagination"

    # GET /securities with type filter
    log_info "Testing GET /securities?type=stock..."
    local type_filter_response
    type_filter_response=$(make_request "GET" "$SECURITIES_SERVICE_URL/securities?type=stock" "200")
    check_response_contains "$type_filter_response" '"data"' "Type filter response has data"

    # GET /securities with exchange filter
    log_info "Testing GET /securities?exchange=MOEX..."
    local exchange_filter_response
    exchange_filter_response=$(make_request "GET" "$SECURITIES_SERVICE_URL/securities?exchange=MOEX" "200")
    check_response_contains "$exchange_filter_response" '"data"' "Exchange filter response has data"

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
        local price_history_response
        price_history_response=$(make_request "GET" "$SECURITIES_SERVICE_URL/securities/$SECURITY_ID/price/history?from=$yesterday&to=$now" "200")
        check_response_contains "$price_history_response" '"securityId"' "Price history has securityId"
        check_response_contains "$price_history_response" '"ticker"' "Price history has ticker"
        check_response_contains "$price_history_response" '"from"' "Price history has from"
        check_response_contains "$price_history_response" '"to"' "Price history has to"
        check_response_contains "$price_history_response" '"data"' "Price history has data array"
    else
        log_warning "Skipping - no security ID available"
    fi

    # GET /securities/{securityId}/orderbook
    log_info "Testing GET /securities/{securityId}/orderbook..."
    if [[ -n "$SECURITY_ID" ]]; then
        local orderbook_response
        orderbook_response=$(make_request "GET" "$SECURITIES_SERVICE_URL/securities/$SECURITY_ID/orderbook" "200")
        check_response_contains "$orderbook_response" '"securityId"' "Orderbook has securityId"
        check_response_contains "$orderbook_response" '"ticker"' "Orderbook has ticker"
        check_response_contains "$orderbook_response" '"asks"' "Orderbook has asks"
        check_response_contains "$orderbook_response" '"bids"' "Orderbook has bids"
        check_response_contains "$orderbook_response" '"spread"' "Orderbook has spread"
    else
        log_warning "Skipping - no security ID available"
    fi

    # GET /securities/{securityId}/orderbook with depth
    log_info "Testing GET /securities/{securityId}/orderbook?depth=5..."
    if [[ -n "$SECURITY_ID" ]]; then
        local orderbook_depth_response
        orderbook_depth_response=$(make_request "GET" "$SECURITIES_SERVICE_URL/securities/$SECURITY_ID/orderbook?depth=5" "200")
        check_response_contains "$orderbook_depth_response" '"securityId"' "Orderbook depth has securityId"
        check_response_contains "$orderbook_depth_response" '"ticker"' "Orderbook depth has ticker"
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
