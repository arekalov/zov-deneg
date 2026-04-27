.PHONY: build clean run

BINARY_NAME := collector
CMD_PATH := ./cmd/collector
ENV_PATH := ../zov-back/env/clickhouse.env

build:
	go build -o $(BINARY_NAME) $(CMD_PATH)

clean:
	rm -f $(BINARY_NAME)

run: build
	@if [ -f $(ENV_PATH) ]; then \
		set -a; source $(ENV_PATH); set +a; \
	fi; \
	./$(BINARY_NAME)

run-docker: build
	@if [ -f $(ENV_PATH) ]; then \
		set -a; source $(ENV_PATH); set +a; \
	else \
		export CLICKHOUSE_HOST=localhost; \
		export CLICKHOUSE_PORT_NATIVE=localhost:9000; \
		export CLICKHOUSE_DB=securities; \
		export CLICKHOUSE_USER=default; \
		export CLICKHOUSE_PASSWORD=securities_pass; \
	fi; \
	./$(BINARY_NAME)
