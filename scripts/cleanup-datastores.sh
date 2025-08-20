#!/usr/bin/env bash
set -euo pipefail

# Cleanup script for local development
# - Resets the two postgres databases used by the project by recreating the `public` schema
# - Flushes all keys from the Redis container
#
# Targets are the container names defined in docker-compose.yml:
#   postgres-booking (DB: booking_db, USER: booking_user, PASS: booking_pass)
#   postgres-carer   (DB: carer_db,   USER: carer_user,   PASS: carer_pass)
#   redis            (no auth, FLUSHALL)

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
POSTGRES_BOOKING_CONTAINER="postgres-booking"
POSTGRES_CARER_CONTAINER="postgres-carer"
REDIS_CONTAINER="redis"

BOOKING_DB="booking_db"
BOOKING_USER="booking_user"
BOOKING_PASS="booking_pass"

CARER_DB="carer_db"
CARER_USER="carer_user"
CARER_PASS="carer_pass"

echo "This will permanently remove all data from the two Postgres databases and flush all Redis keys."
read -p "Are you sure you want to continue? (type YES to proceed): " confirm
if [[ "$confirm" != "YES" ]]; then
  echo "Aborted. No changes made."
  exit 0
fi

command -v docker >/dev/null 2>&1 || { echo "docker is required but not installed. Aborting." >&2; exit 1; }

ensure_running() {
  local name="$1"
  if ! docker ps --format '{{.Names}}' | grep -q "^${name}$"; then
    if docker ps -a --format '{{.Names}}' | grep -q "^${name}$"; then
      echo "Container ${name} exists but is stopped. Starting..."
      docker start "${name}"
    else
      echo "Container ${name} not found. Make sure docker-compose is up and the service exists (see docker-compose.yml)." >&2
      exit 1
    fi
  fi
}

reset_postgres_db() {
  local container="$1" db="$2" user="$3" pass="$4"
  echo "Resetting database '${db}' in container '${container}'..."
  ensure_running "${container}"

  # Run psql inside container; use PGPASSWORD to authenticate non-interactively
  docker exec -i "${container}" bash -c \
    "PGPASSWORD='${pass}' psql -U ${user} -d ${db} -v ON_ERROR_STOP=1 -c \"DROP SCHEMA public CASCADE; CREATE SCHEMA public;\""

  echo "Database '${db}' reset."
}

flush_redis() {
  local container="$1"
  echo "Flushing all keys from Redis container '${container}'..."
  ensure_running "${container}"
  docker exec -i "${container}" redis-cli FLUSHALL
  echo "Redis flush complete."
}

echo "\n-- Resetting booking DB --"
reset_postgres_db "${POSTGRES_BOOKING_CONTAINER}" "${BOOKING_DB}" "${BOOKING_USER}" "${BOOKING_PASS}"

echo "\n-- Resetting carer DB --"
reset_postgres_db "${POSTGRES_CARER_CONTAINER}" "${CARER_DB}" "${CARER_USER}" "${CARER_PASS}"

echo "\n-- Flushing Redis --"
flush_redis "${REDIS_CONTAINER}"

echo "\nAll datastore cleanup actions completed successfully."
echo "Note: Docker volumes remain; this operation resets DB schemas and flushes Redis keys without removing volumes."
