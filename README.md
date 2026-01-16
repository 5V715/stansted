# stansted
is a simple url-shortner service.  

## Backend
Wrtten in kotlin using ktor.
The service is fully none blocking all the way down to the database (r2dbc) and jooq.

## Frontend
The admin webfront end is written in kotlin compose wasm.

## testing locally
``` shell
./gradlew :backend:test --tests "dev.silas.AppTest.can start"
```
this will spin a local postgresql using [embedded-postgres](https://github.com/zonkyio/embedded-postgres)

the frontend resides at http://localhost:8080/admin/ 

the api resides in the root path (GET|POST)http://localhost:8080/

## configuration
the configuration uses [hoplite](https://github.com/sksamuel/hoplite) and resides in the [Config](backend/src/main/kotlin/dev/silas/Config.kt) class. 

All defaults there can be overriden using enviorment variables that match the properties path of this class seperated by 2 underscore `__`.
e.g. enabling basic auth (by setting the password) would be `AUTH__PASSWORD=mypass`
