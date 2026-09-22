# ACID Wallet JPA

A small Spring Boot + Hibernate/JPA project that demonstrates **ACID principles**
hands-on using a simplified digital wallet transfer system — built as a focused
learning exercise, not a feature-complete app.

## Stack

- Java 26
- Spring Boot 4.1.1
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- Lombok

## Core Scenario

```
transferMoney(fromWalletId, toWalletId, amount)
```

A single operation used to concretely demonstrate each ACID guarantee — and,
where relevant, to first show it **break** without the right Hibernate/JPA
mechanism, then fix it.

## Domain

- `Wallet` — id, ownerName, balance, version
- `WalletTransaction` — id, wallet, amount, type (DEPOSIT / WITHDRAWAL / TRANSFER), timestamp, status

## Stages

1. **Project Setup** — Spring Boot + JPA + PostgreSQL, minimal `Wallet` entity, verify boot + table creation
2. **Entity Mapping Basics** — `@Entity`, `@Id`, `@GeneratedValue`, column mapping
3. **Relationships** — `Wallet` ↔ `WalletTransaction`, fetch types (LAZY vs EAGER)
4. **Persistence Context & Session** — first-level cache, managed vs detached, dirty checking
5. **save() vs persist() vs merge()** — differences and correct usage
6. **Atomicity** — naive `transferMoney()` without `@Transactional`, partial failure, then the fix
7. **Consistency** — validation and invariants (balance never negative, total money conserved)
8. **Isolation** — simulated concurrent transfer race condition, dirty/lost update
9. **Optimistic Locking (`@Version`)** — applied to `Wallet.balance`, `OptimisticLockException` under race condition
10. **Pessimistic Locking (`SELECT ... FOR UPDATE`)** — contrast with optimistic, when to choose which
11. **Durability** — conceptual: DB/WAL guarantees vs Hibernate's role
12. **JPQL Basics** — simple queries for wallet lookups/history
13. **Capstone** — final `transferMoney()`: `@Transactional`, optimistic locking, validation, exception handling, concurrent-transfer demo

## Explicitly Out of Scope

Second-level cache, Criteria API, Envers auditing, multi-tenancy — noted as
"exists, not covered here."

## Status

🚧 Work in progress — built incrementally as a guided learning exercise.