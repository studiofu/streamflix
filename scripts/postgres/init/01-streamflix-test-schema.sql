-- JPA tests use schema streamflix_test inside streamflix_db (see user-service application-test.yml).
-- Runs only on first Postgres container init. Existing volumes: run once as superuser:
--   docker exec -i streamflix-postgres psql -U streamflix_user -d streamflix_db -c 'CREATE SCHEMA IF NOT EXISTS streamflix_test;'

CREATE SCHEMA IF NOT EXISTS streamflix_test;
GRANT ALL ON SCHEMA streamflix_test TO streamflix_user;
