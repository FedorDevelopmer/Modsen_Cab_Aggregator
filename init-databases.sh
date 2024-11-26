#!/bin/bash
set -e

for db in passengerdb ratingdb ridesdb; do
  echo "Creating database '$db'"
  psql -U "user" -d "driverdb" -c "CREATE DATABASE $db;"
done

echo "Multiple databases created."