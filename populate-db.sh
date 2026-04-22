#!/usr/bin/env bash
# Popula la base con datos de prueba vía la API REST.
# Requiere que la app esté corriendo (make start-all) y que el seed-db ya
# haya creado los usuarios admin y seller.
#
# Idempotente: si detecta que ya hay suficientes productos, no hace nada.
# Los registros de buyers toleran 409 (ya existe) y siguen con el login.

set -u

BASE="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="admin@mail.com"
ADMIN_PASS="Test1234!"
SELLER_EMAIL="seller_test@test.com"
SELLER_PASS="Test1234!"

CYAN='\033[36m'; GREEN='\033[32m'; YELLOW='\033[33m'; RED='\033[31m'; BOLD='\033[1m'; RESET='\033[0m'

log()  { printf "${CYAN}[ seed ]${RESET} %s\n" "$*"; }
ok()   { printf "${GREEN}[ seed ]${RESET} %s\n" "$*"; }
warn() { printf "${YELLOW}[ seed ]${RESET} %s\n" "$*"; }
die()  { printf "${RED}[ seed ]${RESET} %s\n" "$*" >&2; exit 1; }

# Chequeo previo: app responde? (cualquier respuesta HTTP vale, incluido 401)
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/actuator/health" || echo "000")
if [[ "$HTTP_CODE" == "000" ]]; then
  die "La app no responde en $BASE. Corré 'make start-all' primero."
fi

# ─── Helpers ──────────────────────────────────────────────────────────────────

login() {
  local email="$1" pass="$2"
  curl -s -X POST "$BASE/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"$pass\"}" \
  | jq -r '.token // empty'
}

# POST tolerando 409 (ya existe). Imprime el body de respuesta.
post() {
  local path="$1" token="$2" body="$3"
  curl -s -X POST "$BASE$path" \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d "$body"
}

# ─── 0. Tokens y detección de estado previo ──────────────────────────────────

log "Logueando admin y seller..."
ADMIN_TOKEN=$(login "$ADMIN_EMAIL" "$ADMIN_PASS")
SELLER_TOKEN=$(login "$SELLER_EMAIL" "$SELLER_PASS")
[[ -z "$ADMIN_TOKEN"  ]] && die "No pude loguear admin. ¿Corriste 'make seed-db'?"
[[ -z "$SELLER_TOKEN" ]] && die "No pude loguear seller. ¿Corriste 'make seed-db'?"

PRODUCT_COUNT=$(curl -s -H "Authorization: Bearer $ADMIN_TOKEN" "$BASE/products" | jq 'length // 0')
if [[ "${PRODUCT_COUNT:-0}" -gt 5 ]]; then
  ok "Ya hay $PRODUCT_COUNT productos — la base parece poblada. Nada para hacer."
  exit 0
fi

# ─── 1. Categorías (admin) ────────────────────────────────────────────────────

log "Creando categorías..."
CAT_SMART=$(post /categories     "$ADMIN_TOKEN" '{"description":"Smartphones"}'    | jq -r '.id')
CAT_NOTE=$(post /categories      "$ADMIN_TOKEN" '{"description":"Notebooks"}'      | jq -r '.id')
CAT_AUDIO=$(post /categories     "$ADMIN_TOKEN" '{"description":"Auriculares"}'    | jq -r '.id')
CAT_TV=$(post /categories        "$ADMIN_TOKEN" '{"description":"Televisores"}'    | jq -r '.id')
CAT_ACC=$(post /categories       "$ADMIN_TOKEN" '{"description":"Accesorios"}'     | jq -r '.id')

# ─── 2. Depósitos (admin) ─────────────────────────────────────────────────────

log "Creando depósitos..."
WH_CENTRAL=$(post /warehouses "$ADMIN_TOKEN" '{"name":"Depósito Central","location":"Av. Constituyentes 1234, CABA","contactPhone":"+54 11 9876-5432"}' | jq -r '.id')
WH_SUR=$(post /warehouses     "$ADMIN_TOKEN" '{"name":"Depósito Sur","location":"Av. Hipólito Yrigoyen 5000, Avellaneda","contactPhone":"+54 11 4444-5555"}' | jq -r '.id')

# ─── 3. Productos + variantes + inventario (seller) ──────────────────────────

log "Creando productos, variantes e inventario..."

# Helper: crea producto + 1 variante + stock en WH_CENTRAL
seed_item() {
  local name="$1" desc="$2" brand="$3" cat="$4" sku="$5" attrs="$6" price="$7" stock="$8"
  local pid vid
  pid=$(post /products "$SELLER_TOKEN" "{\"name\":\"$name\",\"description\":\"$desc\",\"brand\":\"$brand\",\"categoryId\":$cat}" | jq -r '.id')
  vid=$(post /variants "$SELLER_TOKEN" "{\"productId\":$pid,\"sku\":\"$sku\",\"attributes\":\"$attrs\",\"basePrice\":$price}" | jq -r '.id')
  post /inventory "$SELLER_TOKEN" "{\"variantId\":$vid,\"warehouseId\":$WH_CENTRAL,\"stockQuantity\":$stock}" > /dev/null
  printf "%s" "$vid"
}

VAR_S24=$(seed_item "Samsung Galaxy S24"       "Smartphone Android flagship"       "Samsung" "$CAT_SMART" "SAM-S24-256-BLK"  "256GB / Negro"      899.99  30)
VAR_IP15=$(seed_item "iPhone 15"               "Smartphone Apple"                  "Apple"   "$CAT_SMART" "APL-IP15-128-BLU" "128GB / Azul"       1199.00 20)
VAR_PIX=$(seed_item "Google Pixel 8"           "Smartphone con cámara avanzada"    "Google"  "$CAT_SMART" "GOO-PIX8-256-OBS" "256GB / Obsidiana"  749.00  15)
VAR_MAC=$(seed_item "MacBook Air M3"           "Notebook Apple con chip M3"        "Apple"   "$CAT_NOTE"  "APL-MBA-M3-512"   "13'' / 512GB"       1499.00 10)
VAR_DELL=$(seed_item "Dell XPS 13"             "Ultrabook premium de Dell"         "Dell"    "$CAT_NOTE"  "DEL-XPS13-i7-16"  "i7 / 16GB / 512GB"  1299.99 12)
VAR_AIR=$(seed_item "AirPods Pro 2"            "Auriculares inalámbricos con ANC"  "Apple"   "$CAT_AUDIO" "APL-APP2"         "USB-C"              249.00  50)
VAR_SONY=$(seed_item "Sony WH-1000XM5"         "Auriculares over-ear con ANC"      "Sony"    "$CAT_AUDIO" "SON-WH1000XM5"    "Negro"              399.00  25)
VAR_LG=$(seed_item "LG OLED C3 55\""           "Smart TV 4K OLED 55 pulgadas"      "LG"      "$CAT_TV"    "LG-OLED55C3"      "55'' / 4K"          1799.00  8)
VAR_CAB=$(seed_item "Cable USB-C 2m"           "Cable USB-C a USB-C de 2 metros"   "Anker"   "$CAT_ACC"   "ANK-USBC-2M"      "2 metros"           19.99  200)
VAR_FUND=$(seed_item "Funda silicona Galaxy S24" "Funda protectora de silicona"    "Spigen"  "$CAT_ACC"   "SPI-S24-CASE-BLK" "Negro"              24.99  100)

# ─── 4. Compradores (registro + login) ────────────────────────────────────────

register_buyer() {
  local username="$1" email="$2" first="$3" last="$4"
  curl -s -X POST "$BASE/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$username\",\"email\":\"$email\",\"password\":\"Password1!\",\"firstName\":\"$first\",\"lastName\":\"$last\",\"phone\":\"+54 11 0000-0000\"}" \
    > /dev/null
  login "$email" "Password1!"
}

log "Registrando compradores..."
ANA_TOKEN=$(register_buyer "ana_g"    "ana@mail.com"    "Ana"    "Gómez")
BRUNO_TOKEN=$(register_buyer "bruno_m" "bruno@mail.com" "Bruno"  "Martínez")
CARLA_TOKEN=$(register_buyer "carla_r" "carla@mail.com" "Carla"  "Rodríguez")

ANA_ID=$(curl -s "$BASE/users/me"   -H "Authorization: Bearer $ANA_TOKEN"   | jq -r '.id')
BRUNO_ID=$(curl -s "$BASE/users/me" -H "Authorization: Bearer $BRUNO_TOKEN" | jq -r '.id')
CARLA_ID=$(curl -s "$BASE/users/me" -H "Authorization: Bearer $CARLA_TOKEN" | jq -r '.id')

# ─── 5. Direcciones ───────────────────────────────────────────────────────────

log "Creando direcciones..."
ANA_ADDR=$(post /addresses "$ANA_TOKEN"   "{\"userId\":$ANA_ID,\"street\":\"Av. Corrientes 3400\",\"city\":\"Buenos Aires\",\"state\":\"CABA\",\"zipCode\":\"1193\",\"referenceNote\":\"Piso 5 A\"}" | jq -r '.id')
BRUNO_ADDR=$(post /addresses "$BRUNO_TOKEN" "{\"userId\":$BRUNO_ID,\"street\":\"Rivadavia 8100\",\"city\":\"Buenos Aires\",\"state\":\"CABA\",\"zipCode\":\"1407\",\"referenceNote\":\"Casa\"}" | jq -r '.id')
CARLA_ADDR=$(post /addresses "$CARLA_TOKEN" "{\"userId\":$CARLA_ID,\"street\":\"9 de Julio 200\",\"city\":\"Córdoba\",\"state\":\"Córdoba\",\"zipCode\":\"5000\",\"referenceNote\":\"Depto 12\"}" | jq -r '.id')

# ─── 6. Pedidos + pagos ───────────────────────────────────────────────────────

log "Creando pedidos y pagos..."
ORDER_ANA=$(post /orders "$ANA_TOKEN" "{\"userId\":$ANA_ID,\"shippingAddressId\":$ANA_ADDR,\"items\":[{\"variantId\":$VAR_S24,\"quantity\":1},{\"variantId\":$VAR_FUND,\"quantity\":1}]}" | jq -r '.id')
post /payments?simulateFailure=false "$ANA_TOKEN" "{\"orderId\":$ORDER_ANA,\"paymentMethod\":\"CREDIT_CARD\"}" > /dev/null

ORDER_BRUNO=$(post /orders "$BRUNO_TOKEN" "{\"userId\":$BRUNO_ID,\"shippingAddressId\":$BRUNO_ADDR,\"items\":[{\"variantId\":$VAR_MAC,\"quantity\":1},{\"variantId\":$VAR_AIR,\"quantity\":1}]}" | jq -r '.id')
post /payments?simulateFailure=false "$BRUNO_TOKEN" "{\"orderId\":$ORDER_BRUNO,\"paymentMethod\":\"DEBIT_CARD\"}" > /dev/null

# Un pedido pendiente sin pagar (útil para testear flujos PENDING)
ORDER_CARLA=$(post /orders "$CARLA_TOKEN" "{\"userId\":$CARLA_ID,\"shippingAddressId\":$CARLA_ADDR,\"items\":[{\"variantId\":$VAR_LG,\"quantity\":1}]}" | jq -r '.id')

# ─── 7. Reseñas (solo sobre productos comprados) ──────────────────────────────

log "Creando reseñas..."
# Ana compró S24 y funda
post /reviews "$ANA_TOKEN" "{\"userId\":$ANA_ID,\"productId\":1,\"rating\":5,\"comment\":\"Excelente teléfono, cámara espectacular.\"}" > /dev/null
# Bruno compró MacBook y AirPods
post /reviews "$BRUNO_TOKEN" "{\"userId\":$BRUNO_ID,\"productId\":4,\"rating\":5,\"comment\":\"Velocidad impresionante, batería que dura todo el día.\"}" > /dev/null
post /reviews "$BRUNO_TOKEN" "{\"userId\":$BRUNO_ID,\"productId\":6,\"rating\":4,\"comment\":\"Muy buenos, aunque caros.\"}" > /dev/null

# ─── 8. Descuento + cupón ─────────────────────────────────────────────────────

log "Creando descuento y cupón..."
DISC_ID=$(post /discounts "$ADMIN_TOKEN" "{\"name\":\"10% en Accesorios\",\"discountType\":\"PERCENTAGE\",\"value\":10.0,\"appliesTo\":\"CATEGORY\",\"categoryId\":$CAT_ACC,\"minPrice\":0}" | jq -r '.id')
post /coupons "$ADMIN_TOKEN" "{\"discountId\":$DISC_ID,\"code\":\"BIENVENIDA10\",\"usageLimit\":100}" > /dev/null

# ─── 9. Ticket de soporte ─────────────────────────────────────────────────────

log "Creando ticket de soporte..."
TICKET_ID=$(post /support/tickets "$CARLA_TOKEN" "{\"userId\":$CARLA_ID,\"subject\":\"Consulta sobre tiempos de envío\",\"status\":\"OPEN\"}" | jq -r '.id')
post "/support/tickets/$TICKET_ID/messages" "$CARLA_TOKEN" "{\"senderId\":$CARLA_ID,\"content\":\"Hola, ¿cuánto tarda el envío a Córdoba?\"}" > /dev/null

# ─── Resumen ──────────────────────────────────────────────────────────────────

ok "Listo. La base tiene ahora:"
printf "    %b5%b categorías · %b2%b depósitos · %b10%b productos\n" "$BOLD" "$RESET" "$BOLD" "$RESET" "$BOLD" "$RESET"
printf "    %b3%b compradores (ana@mail.com, bruno@mail.com, carla@mail.com — pass %bPassword1!%b)\n" "$BOLD" "$RESET" "$YELLOW" "$RESET"
printf "    %b2%b pedidos pagados · %b1%b pendiente · %b3%b reseñas · %b1%b cupón (%bBIENVENIDA10%b) · %b1%b ticket\n" "$BOLD" "$RESET" "$BOLD" "$RESET" "$BOLD" "$RESET" "$BOLD" "$RESET" "$YELLOW" "$RESET" "$BOLD" "$RESET"
