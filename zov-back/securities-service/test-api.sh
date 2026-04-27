#!/bin/bash

# Securities Service API Test Script
# This script tests all API endpoints and saves results to a file

set -e

BASE_URL="${BASE_URL:-http://localhost:58234}"
RESULTS_FILE="${RESULTS_FILE:-test-results.md}"
JWT_TOKEN="${JWT_TOKEN:-}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Initialize results file
init_results() {
    cat > "$RESULTS_FILE" << 'EOF'
# Securities Service API Test Results

## Test Summary

EOF
    echo "**Test Date:** $(date -Iseconds)" >> "$RESULTS_FILE"
    echo "**Base URL:** $BASE_URL" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    echo "| Test | Endpoint | Method | Status | Response Time |" >> "$RESULTS_FILE"
    echo "|------|----------|--------|--------|---------------|" >> "$RESULTS_FILE"
}

# Log test result
log_result() {
    local test_name="$1"
    local endpoint="$2"
    local method="$3"
    local status="$4"
    local response_time="$5"
    local details="$6"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$status" = "PASS" ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo -e "${GREEN}✓ PASS${NC}: $test_name (${response_time}ms)"
        echo "| $test_name | $endpoint | $method | ${GREEN}PASS${NC} | ${response_time}ms |" >> "$RESULTS_FILE"
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo -e "${RED}✗ FAIL${NC}: $test_name - $details"
        echo "| $test_name | $endpoint | $method | ${RED}FAIL${NC} ($details) | - |" >> "$RESULTS_FILE"
    fi
}

# Make HTTP request and measure response time
make_request() {
    local method="$1"
    local endpoint="$2"
    local expected_status="$3"
    local test_name="$4"
    local headers="${5:-}"
    local data="${6:-}"
    
    local start_time=$(date +%s%N)
    
    if [ "$method" = "GET" ]; then
        if [ -n "$headers" ]; then
            response=$(curl -s -w "\n%{http_code}" -o /tmp/response_body.txt "$BASE_URL$endpoint" $headers 2>/dev/null)
        else
            response=$(curl -s -w "\n%{http_code}" -o /tmp/response_body.txt "$BASE_URL$endpoint" 2>/dev/null)
        fi
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -o /tmp/response_body.txt -X POST "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            -d "$data" 2>/dev/null)
    fi
    
    local end_time=$(date +%s%N)
    local response_time=$(( (end_time - start_time) / 1000000 ))
    local http_status=$(echo "$response" | tail -n1)
    
    # Convert escaped pipe to regex pipe
    local regex_pattern=$(echo "$expected_status" | sed 's/\\|/|/g')
    
    if [ "$http_status" = "$expected_status" ]; then
        log_result "$test_name" "$endpoint" "$method" "PASS" "$response_time"
    elif echo "$http_status" | grep -qE "$regex_pattern"; then
        log_result "$test_name" "$endpoint" "$method" "PASS" "$response_time"
    else
        local body=$(cat /tmp/response_body.txt 2>/dev/null | head -c 200)
        log_result "$test_name" "$endpoint" "$method" "FAIL" "Expected $expected_status, got $http_status. Body: $body"
    fi
}

# Check if service is available
wait_for_service() {
    echo "Waiting for service to be available..."
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/" 2>/dev/null | grep -q "200\|401\|404"; then
            echo -e "${GREEN}Service is available!${NC}"
            return 0
        fi
        echo "Attempt $attempt/$max_attempts - Service not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}Service is not available after $max_attempts attempts${NC}"
    return 1
}

# Generate JWT token for testing (simplified - in real scenario use proper JWT)
generate_test_token() {
    # For testing purposes, we'll use a mock token
    # In production, this should be obtained from the auth service
    echo "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJhdWQiOiJ6b3YtZGVuZWctc2VjdXJpdGllcyIsImlzcyI6Inpvdi1kZW5lZy1zZWN1cml0aWVzLXNlcnZpY2UiLCJleHAiOjk5OTk5OTk5OTl9.test-signature"
}

# =============================================================================
# TEST SCENARIOS
# =============================================================================

test_health_endpoint() {
    echo -e "\n${YELLOW}=== Health Check Tests ===${NC}"
    echo "" >> "$RESULTS_FILE"
    echo "## Health Check Tests" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    
    make_request "GET" "/" "200" "Root endpoint returns OK"
}

test_securities_list() {
    echo -e "\n${YELLOW}=== Securities List Tests ===${NC}"
    echo "" >> "$RESULTS_FILE"
    echo "## Securities List Tests" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    
    # Test without auth (should work for public endpoints)
    make_request "GET" "/securities" "200" "Get securities list (no filters)"
    make_request "GET" "/securities?page=1&pageSize=10" "200" "Get securities with pagination"
    make_request "GET" "/securities?q=SBER" "200" "Search securities by ticker"
    make_request "GET" "/securities?type=stock" "200" "Filter by type (stock)"
    make_request "GET" "/securities?exchange=MOEX" "200" "Filter by exchange"
    make_request "GET" "/securities?sector=Финансы" "200" "Filter by sector"
    make_request "GET" "/securities?pageSize=200" "200" "Invalid page size (max 100)"
}

test_security_by_id() {
    echo -e "\n${YELLOW}=== Security By ID Tests ===${NC}"
    echo "" >> "$RESULTS_FILE"
    echo "## Security By ID Tests" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    
    # Test with valid UUID format
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11" "200\|404" "Get security by valid ID"
    make_request "GET" "/securities/invalid-uuid" "400" "Get security by invalid UUID"
    make_request "GET" "/securities/" "404\|405" "Get security with missing ID"
}

test_price_history() {
    echo -e "\n${YELLOW}=== Price History Tests ===${NC}"
    echo "" >> "$RESULTS_FILE"
    echo "## Price History Tests" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    
    local current_ts=$(date +%s)
    local day_ago=$((current_ts - 86400))
    
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/price/history?from=$day_ago&to=$current_ts" "200\|404" "Get price history (valid range)"
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/price/history?from=$current_ts&to=$day_ago" "400" "Get price history (invalid range - from > to)"
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/price/history" "400" "Get price history (missing parameters)"
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/price/history?from=invalid" "400" "Get price history (invalid timestamp format)"
}

test_orderbook() {
    echo -e "\n${YELLOW}=== Order Book Tests ===${NC}"
    echo "" >> "$RESULTS_FILE"
    echo "## Order Book Tests" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/orderbook" "200\|404" "Get orderbook (default depth)"
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/orderbook?depth=5" "200\|404" "Get orderbook (depth=5)"
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/orderbook?depth=50" "200\|404" "Get orderbook (max depth)"
    make_request "GET" "/securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/orderbook?depth=100" "200\|404" "Get orderbook (depth > max)"
}

test_auth_required() {
    echo -e "\n${YELLOW}=== Authentication Tests ===${NC}"
    echo "" >> "$RESULTS_FILE"
    echo "## Authentication Tests" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    
    # Note: Currently endpoints don't require auth, but we test the structure
    make_request "GET" "/securities" "200" "Public endpoint without token"
    
    # If auth is enabled, these would return 401
    # make_request "GET" "/securities" "401" "Endpoint without JWT token" " -H \"Authorization: Bearer invalid-token\""
}

test_error_handling() {
    echo -e "\n${YELLOW}=== Error Handling Tests ===${NC}"
    echo "" >> "$RESULTS_FILE"
    echo "## Error Handling Tests" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    
    make_request "GET" "/nonexistent" "404" "Non-existent endpoint"
    make_request "GET" "/securities/not-a-uuid/price/history?from=1&to=2" "400" "Invalid UUID format"
}

# =============================================================================
# MAIN
# =============================================================================

main() {
    echo "========================================"
    echo "  Securities Service API Test Suite"
    echo "========================================"
    echo ""
    echo "Base URL: $BASE_URL"
    echo "Results file: $RESULTS_FILE"
    echo ""
    
    # Wait for service
    if ! wait_for_service; then
        echo "Tests cannot proceed without service"
        exit 1
    fi
    
    # Initialize results
    init_results
    
    # Run test scenarios
    test_health_endpoint
    test_securities_list
    test_security_by_id
    test_price_history
    test_orderbook
    test_auth_required
    test_error_handling
    
    # Write summary
    echo "" >> "$RESULTS_FILE"
    echo "## Summary" >> "$RESULTS_FILE"
    echo "" >> "$RESULTS_FILE"
    echo "- **Total Tests:** $TOTAL_TESTS" >> "$RESULTS_FILE"
    echo "- **Passed:** $PASSED_TESTS" >> "$RESULTS_FILE"
    echo "- **Failed:** $FAILED_TESTS" >> "$RESULTS_FILE"
    echo "- **Success Rate:** $(( (PASSED_TESTS * 100) / (TOTAL_TESTS > 0 ? TOTAL_TESTS : 1) ))%" >> "$RESULTS_FILE"
    
    echo ""
    echo "========================================"
    echo "  Test Summary"
    echo "========================================"
    echo "Total:  $TOTAL_TESTS"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    echo ""
    echo "Results saved to: $RESULTS_FILE"
    
    # Return exit code based on failures
    if [ $FAILED_TESTS -gt 0 ]; then
        return 1
    fi
    return 0
}

# Run main
main
