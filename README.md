# Core Banking Loan Settlement & Prepayment Engine

This Spring Boot application implements a loan calculator with prepayment and early settlement strategies for a retail lending engine.

## Features
- Create a loan with monthly amortization schedule.
- Process partial prepayments using three strategies:
  - Reduce EMI, keep tenor.
  - Keep EMI, shorten tenor.
  - Advance installments without schedule recalculation.
- Calculate early settlement quotes using three options:
  - True early settlement.
  - Rule of 78 settlement.
  - Discounted settlement.
- Track loan schedule entries and transaction history.

## Requirements
- Java 21+
- Maven
- MySQL (for runtime) or H2 (for local testing)

## Database setup
1. Create a MySQL database:

```sql
CREATE DATABASE loan_engine;
```

2. Update `src/main/resources/application.properties` with your MySQL credentials.
3. Run the schema script:

```bash
mysql -u root -p loan_engine < schema.sql
```

## Run the application

The app is configured to run on port `9090` by default.

```bash
cd /Users/richie/Desktop/test
mvn spring-boot:run
```

### Run with H2 for local development

Use the `test` profile to run with an in-memory H2 database instead of MySQL.

```bash
cd /Users/richie/Desktop/test
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

Then open Swagger UI at:

```text
http://localhost:9090/swagger-ui/index.html
```

## Example API calls

Create a loan:

```bash
curl -X POST http://localhost:9090/api/loans \
  -H "Content-Type: application/json" \
  -d '{"principal":1000000,"annualInterestRate":0.12,"tenorMonths":60,"startDate":"2026-01-01"}'
```



Get loan schedule:

```bash
curl http://localhost:9090/api/loans/1/schedule
```

Process a prepayment (Option A):

```bash
curl -X POST http://localhost:9090/api/loans/1/prepayments \
  -H "Content-Type: application/json" \
  -d '{"installmentNumber":24,"amount":200000,"option":"A"}'
```

Request a settlement quote (Option D):

```bash
curl -X POST http://localhost:9090/api/loans/1/settlements \
  -H "Content-Type: application/json" \
  -d '{"installmentNumber":24,"option":"D","apply":false}'
```
# Presta.Test
