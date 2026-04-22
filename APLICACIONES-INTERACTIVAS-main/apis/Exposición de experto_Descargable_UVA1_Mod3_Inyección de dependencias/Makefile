-include .env
export

# ─── Selector de motor (mssql | mysql) ────────────────────────────────────────
# Uso:
#   make start-all             → SQL Server (default)
#   make start-all DB=mysql    → MySQL
DB ?= mssql

MVN = ./mvnw

ifeq ($(DB),mysql)
  PROFILE         = docker-mysql
  CONTAINER       = ecommerce-mysql
  COMPOSE_PROFILE = mysql
  DB_NAME         = ecommerce
  SEED_FILE       = seed-users-mysql.sql
  DB_ROOT_PASS    = YourStrong@Passw0rd
  DB_LABEL        = MySQL
  # Forzamos los valores de conexion MySQL para ignorar cualquier DB_URL
  # heredado de .env (que puede estar apuntando a SQL Server).
  override DB_URL  := jdbc:mysql://localhost:3306/ecommerce?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
  override DB_USER := appuser
  override DB_PASS := App12345!
else ifeq ($(DB),mssql)
  PROFILE         = docker
  CONTAINER       = ecommerce-sqlserver
  COMPOSE_PROFILE = mssql
  DB_NAME         = E-commerce
  SEED_FILE       = seed-users.sql
  SA_PASS         = YourStrong@Passw0rd
  SQLCMD          = /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P '$(SA_PASS)' -No
  DB_LABEL        = SQL Server
else
  $(error DB debe ser 'mssql' o 'mysql', recibí '$(DB)')
endif

CYAN   = \033[36m
GREEN  = \033[32m
YELLOW = \033[33m
RED    = \033[31m
BOLD   = \033[1m
RESET  = \033[0m

.DEFAULT_GOAL := help

# ─── Ayuda ────────────────────────────────────────────────────────────────────

.PHONY: help
help:
	@printf "\n$(BOLD)  E-Commerce API — comandos Make$(RESET)\n\n"
	@printf "  $(BOLD)Motor activo:$(RESET) $(YELLOW)$(DB_LABEL)$(RESET)  (cambiar con $(CYAN)DB=mysql$(RESET) o $(CYAN)DB=mssql$(RESET))\n\n"
	@printf "  $(BOLD)Flujo recomendado (primera vez):$(RESET)\n"
	@printf "    1. $(CYAN)make start-all$(RESET)    →  levanta Docker + inicia DB + corre la app\n"
	@printf "    2. (en otra terminal, una vez que veas $(BOLD)\"Started DemoApplication\"$(RESET))\n"
	@printf "       $(CYAN)make seed-db$(RESET)      →  carga admin + seller de prueba\n\n"
	@printf "  $(BOLD)Ejemplos con MySQL:$(RESET)\n"
	@printf "    $(CYAN)make start-all DB=mysql$(RESET)\n"
	@printf "    $(CYAN)make seed-db DB=mysql$(RESET)\n\n"
	@printf "  $(BOLD)Comandos individuales:$(RESET)\n"
	@printf "    $(CYAN)make start-db$(RESET)        →  levanta el contenedor y espera que esté listo\n"
	@printf "    $(CYAN)make init-db$(RESET)         →  crea la base y el usuario (idempotente)\n"
	@printf "    $(CYAN)make seed-db$(RESET)         →  carga admin + seller de prueba (idempotente)\n"
	@printf "    $(CYAN)make start-app$(RESET)       →  corre la app con el perfil correcto\n"
	@printf "    $(CYAN)make stop-db$(RESET)         →  baja el contenedor\n"
	@printf "    $(RED)make wipe-db$(RESET)         →  baja el contenedor y BORRA los datos (volumen)\n\n"
	@printf "  $(BOLD)Build y tests:$(RESET)\n"
	@printf "    $(CYAN)make run-tests$(RESET)       →  corre los tests con H2 (sin necesitar Docker)\n"
	@printf "    $(CYAN)make build-app$(RESET)       →  compila el proyecto sin correr tests\n"
	@printf "    $(CYAN)make clean-build$(RESET)     →  elimina los archivos compilados (target/)\n\n"

# ─── Docker ───────────────────────────────────────────────────────────────────

.PHONY: start-db
start-db:
	@printf "$(CYAN)[ docker ]$(RESET) Levantando $(DB_LABEL)...\n"
	docker compose --profile $(COMPOSE_PROFILE) up -d
	@printf "$(YELLOW)[ docker ]$(RESET) Esperando que $(DB_LABEL) esté listo..."
	@until docker inspect --format='{{.State.Health.Status}}' $(CONTAINER) 2>/dev/null | grep -q healthy; do \
		printf "."; sleep 3; \
	done
	@printf "\n$(GREEN)[ docker ]$(RESET) $(DB_LABEL) listo.\n"

.PHONY: stop-db
stop-db:
	@printf "$(CYAN)[ docker ]$(RESET) Bajando $(DB_LABEL)...\n"
	docker compose --profile $(COMPOSE_PROFILE) down
	@printf "$(GREEN)[ docker ]$(RESET) Contenedor detenido.\n"

.PHONY: wipe-db
wipe-db:
	@printf "$(RED)[ wipe   ]$(RESET) Borrando datos del volumen de $(DB_LABEL)...\n"
	docker compose --profile $(COMPOSE_PROFILE) down -v
	@printf "$(GREEN)[ wipe   ]$(RESET) Contenedor y datos eliminados.\n"

# ─── Base de datos ────────────────────────────────────────────────────────────

.PHONY: init-db
init-db:
ifeq ($(DB),mssql)
	@printf "$(CYAN)[  db    ]$(RESET) Inicializando base de datos y usuario (SQL Server)...\n"
	docker exec -it $(CONTAINER) $(SQLCMD) \
		-Q "IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'E-commerce') \
		    BEGIN CREATE DATABASE [E-commerce] END; \
		    IF NOT EXISTS (SELECT name FROM sys.server_principals WHERE name = 'appuser') \
		    BEGIN CREATE LOGIN appuser WITH PASSWORD='App12345!' END; \
		    USE [E-commerce]; \
		    IF NOT EXISTS (SELECT name FROM sys.database_principals WHERE name = 'appuser') \
		    BEGIN CREATE USER appuser FOR LOGIN appuser END; \
		    ALTER ROLE db_owner ADD MEMBER appuser;"
	@printf "$(GREEN)[  db    ]$(RESET) Base [E-commerce] y usuario appuser listos.\n"
else
	@printf "$(GREEN)[  db    ]$(RESET) MySQL crea la base y el usuario automáticamente (MYSQL_DATABASE + MYSQL_USER). Nada que hacer.\n"
endif

.PHONY: seed-db
seed-db:
	@printf "$(CYAN)[  db    ]$(RESET) Cargando seed ($(DB_LABEL))...\n"
ifeq ($(DB),mssql)
	docker exec -i $(CONTAINER) $(SQLCMD) -d "$(DB_NAME)" < $(SEED_FILE)
else
	docker exec -i $(CONTAINER) mysql -uroot -p$(DB_ROOT_PASS) $(DB_NAME) < $(SEED_FILE)
endif
	@printf "$(GREEN)[  db    ]$(RESET) Seed listo. Credenciales de desarrollo:\n"
	@printf "    $(BOLD)admin$(RESET)   →  email: $(YELLOW)admin@mail.com$(RESET)        password: $(YELLOW)Test1234!$(RESET)\n"
	@printf "    $(BOLD)seller$(RESET)  →  email: $(YELLOW)seller_test@test.com$(RESET)  password: $(YELLOW)Test1234!$(RESET)\n"
	@printf "    $(RED)⚠  Solo para desarrollo. Cambiar antes de producción.$(RESET)\n"

# ─── Aplicación ───────────────────────────────────────────────────────────────

.PHONY: start-app
start-app:
	@printf "$(CYAN)[  app   ]$(RESET) Iniciando la app con perfil '$(PROFILE)' ($(DB_LABEL))...\n"
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
