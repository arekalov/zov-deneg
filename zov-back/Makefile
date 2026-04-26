build-user-service:
	cd user-service && ./gradlew installDist

build-securities-service:
	cd securities-service && ./gradlew installDist

build: build-user-service build-securities-service

run: build
	docker compose up -d --build

down:
	docker compose down -v --remove-orphans

clean-user-service:
	cd user-service && ./gradlew clean

clean-securities-service:
	cd securities-service && ./gradlew clean

clean: down clean-user-service clean-securities-service

test: run
	sh test-apis.sh

restart: clean run
