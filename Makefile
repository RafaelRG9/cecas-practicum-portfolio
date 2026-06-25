seed:
	docker compose run --rm \
		-e SPRING_PROFILES_ACTIVE=seed \
		backend

reset-db:
	docker compose down -v
	docker compose up --build -d