-include .env
export

PROFILE        = docker
CONTAINER      = ecommerce-sqlserver
SA_PASS        = YourStrong@Passw0rd
SQLCMD         = /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P '$(SA_PASS)' -No
MVN            = ./mvnw

CYAN   = \033[36m
GREEN  = \033[32m
YELLOW = \033[33m
RED    = \033[31m
BOLD   = \033[1m
RESET  = \033[0m

.DEFAULT_GOAL  := help

# ─── Ayuda ────────────────────────────────────────────────────────────────────

.PHONY: help
help:
	@printf "\n$(BOLD)  E-Commerce API — comandos Make$(RESET)\n\n"
	@printf "  $(BOLD)Flujo recomendado (primera vez):$(RESET)\n"
	@printf "    $(CYAN)make start-all$(RESET)       →  levanta Docker + inicia DB + corre la app\n"
	@printf "\n"
	@printf "  $(BOLD)Comandos individuales:$(RESET)\n"
	@printf "    $(CYAN)make start-db$(RESET)        →  levanta el contenedor de SQL Server y espera que esté listo\n"
	@printf "    $(CYAN)make init-db$(RESET)         →  crea la base de datos y el usuario (idempotente)\n"
	@printf "    $(CYAN)make start-app$(RESET)       →  corre la app con perfil docker\n"
	@printf "    $(CYAN)make stop-db$(RESET)         →  baja el contenedor\n"
	@printf "    $(RED)make wipe-db$(RESET)         →  baja el contenedor y BORRA los datos (volumen)\n"
	@printf "\n"
	@printf "  $(BOLD)Build y tests:$(RESET)\n"
	@printf "    $(CYAN)make run-tests$(RESET)       →  corre los tests con H2 (sin necesitar Docker)\n"
	@printf "    $(CYAN)make build-app$(RESET)       →  compila el proyecto sin correr tests\n"
	@printf "    $(CYAN)make clean-build$(RESET)     →  elimina los archivos compilados (target/)\n"
	@printf "\n"

# ─── Docker ───────────────────────────────────────────────────────────────────

.PHONY: start-db
start-db:
	@printf "$(CYAN)[ docker ]$(RESET) Levantando SQL Server...\n"
	docker compose up -d
	@printf "$(YELLOW)[ docker ]$(RESET) Esperando que SQL Server esté listo (puede tardar hasta 30s)..."
	@until docker inspect --format='{{.State.Health.Status}}' $(CONTAINER) 2>/dev/null | grep -q healthy; do \
		printf "."; sleep 3; \
	done
	@printf "\n$(GREEN)[ docker ]$(RESET) SQL Server listo.\n"

.PHONY: stop-db
stop-db:
	@printf "$(CYAN)[ docker ]$(RESET) Bajando el contenedor...\n"
	docker compose down
	@printf "$(GREEN)[ docker ]$(RESET) Contenedor detenido.\n"

.PHONY: wipe-db
wipe-db:
	@printf "$(RED)[ wipe   ]$(RESET) Esto borra todos los datos del volumen. Continuando...\n"
	docker compose down -v
	@printf "$(GREEN)[ wipe   ]$(RESET) Contenedor y datos eliminados.\n"

# ─── Base de datos ────────────────────────────────────────────────────────────

.PHONY: init-db
init-db:
	@printf "$(CYAN)[  db    ]$(RESET) Inicializando base de datos y usuario...\n"
	docker exec -it $(CONTAINER) $(SQLCMD) \
		-Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'E-commerce') \
		    BEGIN CREATE DATABASE [E-commerce] END; \
		    IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'appuser') \
		    BEGIN CREATE LOGIN appuser WITH PASSWORD='App12345!' END; \
		    USE [E-commerce]; \
		    IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'appuser') \
		    BEGIN CREATE USER appuser FOR LOGIN appuser END; \
		    ALTER ROLE db_owner ADD MEMBER appuser;"
	@printf "$(GREEN)[  db    ]$(RESET) Base de datos [E-commerce] y usuario appuser listos.\n"

# ─── Aplicación ───────────────────────────────────────────────────────────────

.PHONY: start-app
start-app:
	@printf "$(CYAN)[  app   ]$(RESET) Iniciando la aplicacion con perfil '$(PROFILE)'...\n"
	$(MVN) spring-boot:run -Dspring-boot.run.profiles=$(PROFILE)

.PHONY: start-all
start-all: start-db init-db start-app

# ─── Build y tests ────────────────────────────────────────────────────────────

.PHONY: build-app
build-app:
	@printf "$(CYAN)[ build  ]$(RESET) Compilando el proyecto (sin tests)...\n"
	$(MVN) package -DskipTests
	@printf "$(GREEN)[ build  ]$(RESET) Build listo en target/.\n"

.PHONY: run-tests
run-tests:
	@printf "$(CYAN)[ test   ]$(RESET) Corriendo tests con H2 (no se necesita Docker)...\n"
	$(MVN) test

.PHONY: clean-build
clean-build:
	@printf "$(CYAN)[ clean  ]$(RESET) Limpiando archivos compilados...\n"
	$(MVN) clean
	@printf "$(GREEN)[ clean  ]$(RESET) target/ eliminado.\n"
