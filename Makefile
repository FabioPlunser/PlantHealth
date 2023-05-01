
all: debug

prod:
	@ cd backend && gradle assemble
	@ docker compose up --build

debug:
	@ cd backend && gradle assemble
	@ docker compose --profile debug up --build

test: 
	@ cd backend && gradle assemble
	@ docker compose --profile test up --build
