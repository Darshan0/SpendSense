# SpendSense Online App Technical Requirements Document

## 1. Purpose

This document defines the technical design for moving SpendSense from a local-first Android app to an online app with authentication, encrypted cloud storage, backend analytics, and GPT-powered financial explanations.

The target product remains privacy-aware and finance-correct:

```text
App captures transactions
  -> backend stores encrypted user data
  -> deterministic analytics computes facts
  -> GPT explains compact summaries
  -> user receives grounded financial guidance
```

The backend exists to support:

- Google Sign In and Apple Sign In
- user accounts
- cloud sync
- encrypted transaction storage
- backend analytics
- GPT-powered advisor responses
- subscription entitlements
- export/delete account workflows

## 2. Core Technical Principles

### 2.1 Backend Can Analyze, GPT Cannot Invent

The backend may decrypt scoped user data only inside trusted services to compute analytics. GPT must receive compact structured context, not raw notification/SMS/email text or full unbounded transaction history.

### 2.2 Deterministic Finance Engine Is Source Of Truth

All financial calculations must happen in deterministic code:

- category totals
- merchant totals
- subcategory totals
- daily guardrail
- 7-day trends
- goal impact
- item split totals
- group split balances
- EMI
- loan affordability
- rent burden
- emergency fund

GPT may explain those results but must not become the source of financial truth.

### 2.3 Encrypt Sensitive Data At Multiple Layers

SpendSense should use:

- TLS in transit
- database/storage encryption at rest
- application-level field or record encryption
- KMS envelope encryption
- audit logging for decrypt operations

### 2.4 Minimize Raw Source Retention

Production default:

```text
raw notification retention = 0
raw SMS retention = 0
raw email body retention = 0
```

Allowed persistence:

- structured transaction fields
- source type
- source message hash
- parser confidence
- redacted account suffix
- extraction evidence flags

Raw text may be stored only for explicit user-approved debugging with short retention.

## 3. High-Level Architecture

```text
Android App
  - Google/Apple Sign In
  - notification capture
  - optional SMS/email import
  - local encrypted cache
  - offline queue
  - local UI

Backend API
  - auth token verification
  - transaction ingestion
  - encrypted persistence
  - sync API
  - profile/goals/groups API
  - export/delete API

Processing Workers
  - duplicate reconciliation
  - merchant normalization
  - category/subcategory engine
  - split prediction engine
  - group routing engine
  - analytics engine
  - insight generation

AI Service
  - compact context builder
  - deterministic planning tools
  - GPT request router
  - answer validator
  - response cache
  - usage/cost limiter

Storage
  - Postgres
  - Redis/queue
  - object storage for exports
  - KMS/HSM for key protection
  - observability stack
```

## 4. Recommended Stack

### 4.1 Backend

Recommended initial backend:

- Kotlin + Spring Boot, or Node.js + NestJS.
- Postgres as primary relational store.
- Redis for caching, locks, idempotency, queues, and rate limits.
- Background workers for analytics and AI jobs.
- Object storage for generated exports.
- Managed KMS for envelope encryption.

Kotlin/Spring is a strong fit if the Android/domain finance code is shared conceptually. Node/NestJS is faster for small-team backend iteration. Either is acceptable if clean boundaries are maintained.

### 4.2 Auth

Use a managed auth provider initially:

- Firebase Auth
- Supabase Auth
- Auth0
- Cognito

Required providers:

- Google Sign In
- Apple Sign In

Backend must verify identity tokens server-side and map them to an internal `user_id`.

### 4.3 AI

Use online GPT behind a backend service:

```text
App -> Backend AI endpoint -> deterministic tools -> GPT -> validator -> App
```

The app must never call OpenAI directly with a client-side API key.

## 5. Data Classification

### 5.1 Highly Sensitive

Encrypt at application layer:

- transaction merchant name
- transaction notes
- transaction amount
- account suffix/source account metadata
- salary/income assumptions
- goals
- transaction line-item splits
- group participants
- group balances
- chat messages
- exports

### 5.2 Sensitive Derived Data

Encrypt at rest and consider application-layer encryption:

- category totals
- merchant summaries
- trend summaries
- insight cards
- GPT compact context
- assistant responses

### 5.3 Operational Data

May be stored normally with database encryption at rest:

- user id
- auth provider id
- subscription plan
- AI usage counters
- feature flags
- device metadata
- sync cursors
- created/updated timestamps

Operational data must not include financial text or raw merchant descriptions.

## 6. Encryption Design

### 6.1 Envelope Encryption

Use envelope encryption:

```text
plaintext record
  -> encrypted with DEK
  -> DEK wrapped by KMS KEK
  -> ciphertext + wrapped DEK stored in DB
```

Definitions:

- DEK: data encryption key used to encrypt the user record or user dataset.
- KEK: key encryption key stored in KMS.
- KMS: managed key service or HSM-backed vault.

Required algorithm:

- AES-256-GCM for application-layer encryption.
- Unique nonce/IV per encryption.
- Authenticated additional data containing user id, table/entity type, and schema version.

### 6.2 Key Granularity

Recommended:

- one user master data key per user for common records
- optional per-record DEK for chat/export records
- KMS-wrapped DEKs stored separately from ciphertext fields

Initial implementation can use per-user wrapped DEK if operational simplicity is needed.

### 6.3 Decrypt Policy

Backend services may decrypt only when required for:

- user sync response
- analytics job
- AI compact context generation
- export generation
- user data deletion verification

Every decrypt operation must include:

- user id
- service name
- purpose
- request id
- timestamp

No decrypt operation should be performed inside generic logging, metrics, or debugging code.

### 6.4 Local Device Encryption

Android must keep local cache encrypted:

```text
Android Keystore
  -> app database key
  -> encrypted Room database
```

If SQLCipher is used, database key material must be protected by Android Keystore. If encrypted fields are used locally, the same no-raw-text logging policy applies.

## 7. Data Flow

### 7.1 Sign In

```text
User taps Google/Apple Sign In
  -> app receives provider token
  -> app sends token to backend
  -> backend verifies token
  -> backend creates/loads user
  -> backend returns app session token
```

Requirements:

- refreshable session
- logout
- revoke/delete account
- device registration
- plan/entitlement fetch

### 7.2 Transaction Ingestion

```text
Notification/SMS/email source
  -> app sanitizer
  -> sensitive-message filter
  -> deterministic parser
  -> structured transaction candidate
  -> local encrypted queue
  -> backend ingestion API
  -> duplicate reconciliation
  -> encrypted DB write
  -> analytics job scheduled
```

Requirements:

- idempotency key per source event
- duplicate detection across sources
- no raw text upload by default
- confidence and parser evidence uploaded
- user review state supported

### 7.3 Transaction Correction

```text
User edits merchant/category/split
  -> app updates local record
  -> sync correction event
  -> backend stores correction
  -> backend updates merchant/category rule
  -> future predictions improve
```

Corrections must be append-only events or auditable changes, not silent overwrites.

### 7.4 Analytics Generation

```text
Transaction changed
  -> analytics job starts
  -> decrypt required records
  -> compute deterministic summaries
  -> store encrypted insight summaries
  -> invalidate GPT context cache
```

Analytics must compute:

- today spend
- yesterday spend
- last 7 days
- previous 7 days
- category and subcategory movement
- merchant movement
- daily guardrail
- goal impact
- split/personal share impact
- group balances

### 7.5 GPT Advisor Flow

```text
User asks question
  -> backend classifies intent
  -> deterministic tool calculates facts
  -> compact context built
  -> GPT generates answer
  -> validator checks answer against facts
  -> answer stored encrypted
  -> app renders text + visual cards
```

GPT input must be compact and structured.

Example GPT context:

```json
{
  "userProfile": {
    "currency": "INR",
    "salaryKnown": true
  },
  "recentSpend": {
    "todayMinor": 2747000,
    "last7DaysMinor": 2704200,
    "dailyGuardrailMinor": 641666
  },
  "topDrivers": [
    {
      "category": "Uncategorized",
      "amountMinor": 2704200,
      "action": "Ask user to review merchants before advising cuts"
    }
  ],
  "activeGoal": {
    "name": "New Laptop",
    "remainingMinor": 12000000,
    "recentSpendShareOfGapPercent": 22
  },
  "toolResult": {
    "type": "BUDGET_HEALTH",
    "verdict": "CAUTION",
    "facts": [
      "Today spend is above daily guardrail",
      "Top driver is uncategorized and needs review"
    ]
  }
}
```

## 8. Data Model Requirements

### 8.1 Users

Fields:

- `id`
- `auth_provider`
- `auth_provider_subject`
- `email_hash`
- `display_name_ciphertext`
- `plan`
- `created_at`
- `deleted_at`

### 8.2 Devices

Fields:

- `id`
- `user_id`
- `platform`
- `device_name`
- `push_token_ciphertext`
- `last_seen_at`
- `sync_cursor`

### 8.3 Transactions

Fields:

- `id`
- `user_id`
- `source_type`
- `source_event_hash`
- `type`
- `amount_ciphertext`
- `currency`
- `merchant_ciphertext`
- `category`
- `subcategory`
- `confidence`
- `occurred_at`
- `personal_share_ciphertext`
- `review_state`
- `created_at`
- `updated_at`

### 8.4 Transaction Line Items

Fields:

- `id`
- `transaction_id`
- `amount_ciphertext`
- `category`
- `subcategory`
- `quantity`
- `note_ciphertext`
- `confidence`
- `created_by`

Invariant:

```text
sum(line_items.amount) == transaction.amount
```

### 8.5 Learned Patterns

Fields:

- `id`
- `user_id`
- `merchant_fingerprint`
- `pattern_type`
- `encrypted_pattern_payload`
- `confidence`
- `confirmation_count`
- `last_confirmed_at`
- `paused_at`

Examples:

- merchant to category
- merchant to subcategory
- merchant amount to line-item split
- merchant to group

### 8.6 Groups

Fields:

- `id`
- `user_id`
- `type`
- `name_ciphertext`
- `created_at`

Types:

- household
- friend_group

### 8.7 Group Participants

Fields:

- `id`
- `group_id`
- `name_ciphertext`
- `contact_hash`
- `status`

### 8.8 Group Expense Shares

Fields:

- `id`
- `transaction_id`
- `group_id`
- `participant_id`
- `share_amount_ciphertext`
- `paid_amount_ciphertext`
- `settlement_state`

### 8.9 Goals

Fields:

- `id`
- `user_id`
- `name_ciphertext`
- `target_amount_ciphertext`
- `current_amount_ciphertext`
- `target_date`
- `created_at`
- `updated_at`

### 8.10 Insights

Fields:

- `id`
- `user_id`
- `type`
- `severity`
- `encrypted_payload`
- `evidence_hash`
- `created_at`
- `expires_at`

### 8.11 AI Usage

Fields:

- `id`
- `user_id`
- `model`
- `intent`
- `input_tokens`
- `output_tokens`
- `estimated_cost_minor`
- `created_at`

## 9. API Requirements

### 9.1 Auth

- `POST /auth/provider`
- `POST /auth/refresh`
- `POST /auth/logout`
- `DELETE /account`

### 9.2 Sync

- `POST /sync/push`
- `GET /sync/pull?cursor=...`
- `GET /sync/status`

### 9.3 Transactions

- `POST /transactions`
- `GET /transactions`
- `PATCH /transactions/{id}`
- `DELETE /transactions/{id}`
- `POST /transactions/{id}/split`
- `POST /transactions/{id}/group`

### 9.4 Goals

- `POST /goals`
- `GET /goals`
- `PATCH /goals/{id}`
- `DELETE /goals/{id}`

### 9.5 Insights

- `GET /insights`
- `POST /insights/refresh`

### 9.6 AI

- `POST /ai/chat`
- `GET /ai/suggestions`
- `GET /ai/usage`

### 9.7 Export

- `POST /exports`
- `GET /exports/{id}`

## 10. AI Requirements

### 10.1 Intent Routing

Supported intents:

- spend total
- category analysis
- merchant analysis
- budget health
- goal impact
- purchase affordability
- EMI planning
- loan affordability
- rent burden
- emergency fund
- shared expense balance
- transaction split explanation
- general finance coaching

### 10.2 Model Routing

Use model routing by task:

- cheap/fast model for summary rewrites and suggestions
- stronger model for complex planning chat
- deterministic fallback for exact totals and missing inputs

### 10.3 Answer Validation

Before returning a GPT answer:

- amounts mentioned must exist in tool results or compact context
- merchant/category names must exist in context
- no invented balances
- no guaranteed returns
- no tax claims without tax inputs
- no licensed-advisor claims

If validation fails:

- retry with stricter prompt once
- otherwise return deterministic answer

## 11. Security Requirements

### 11.1 Authentication

- Verify Google/Apple identity tokens server-side.
- Store auth subject IDs separately from financial data.
- Use short-lived access tokens and refresh tokens.
- Support logout from device.
- Support delete account.

### 11.2 Authorization

- Every request must be scoped to authenticated `user_id`.
- Group access must be explicit.
- Admin tools must not expose plaintext financial data by default.
- Service-to-service permissions must be least privilege.

### 11.3 Logging

Never log:

- raw notification text
- SMS/email body
- merchant names
- transaction amounts
- account suffixes
- chat prompts containing financial details
- decrypted insight payloads

Allowed logs:

- request id
- user id hash
- endpoint
- status code
- latency
- error class
- model name
- token counts
- estimated AI cost

### 11.4 Data Deletion

Account deletion must:

- revoke auth/session tokens
- delete or crypto-shred user DEK
- delete sync data
- delete exports
- delete AI history
- delete local app cache on next sign-in/logout event

Crypto-shredding a user key can make encrypted data unrecoverable quickly, but retention policy must still define physical deletion timing.

## 12. Privacy Requirements

User controls:

- export my data
- delete my data
- disable cloud sync
- disable AI personalization
- disable email import
- disable SMS import
- clear learned patterns
- clear chat history

Transparency:

- explain what is uploaded
- explain what GPT sees
- show source health
- show AI usage limits

## 13. Reliability Requirements

- App must queue writes offline.
- Backend APIs must be idempotent.
- Sync must handle duplicate/out-of-order events.
- Analytics jobs must be retryable.
- AI failures must not block transaction viewing.
- GPT outage must fall back to deterministic answers.
- Data migrations must be backwards-compatible.

## 14. Performance Requirements

App:

- home cached render under 500 ms after local data is available
- transaction write accepted locally under 200 ms
- sync runs in background

Backend:

- transaction ingestion p95 under 300 ms excluding cold starts
- sync pull p95 under 500 ms for normal page sizes
- common AI answer p95 under 5 seconds
- analytics refresh within 60 seconds after transaction changes

## 15. Cost Requirements

The backend must track:

- AI calls per user
- AI cost per user
- AI cost per plan
- cache hit rate
- analytics job volume
- storage per user

Cost controls:

- cache common insight answers
- precompute daily insight summaries
- limit free AI usage
- use compact GPT context
- route simple questions to deterministic answers
- use cheaper model for low-risk rewrite tasks

## 16. Migration From Current App

Current app:

```text
local notification capture
local Room DB
local analytics
local LLM/deterministic fallback
```

Target migration:

1. Add auth UI and session management.
2. Add local user profile linked to remote user id.
3. Add encrypted sync queue.
4. Add backend ingestion API.
5. Add cloud transaction mirror.
6. Move analytics to shared deterministic engine or backend equivalent.
7. Add GPT-backed AI endpoint.
8. Add plan/usage limits.
9. Keep local fallback for offline mode.

## 17. Implementation Phases

### Phase 1: Online Identity

- Google Sign In.
- Apple Sign In.
- Backend auth verification.
- User profile API.
- Session refresh/logout/delete.

### Phase 2: Encrypted Cloud Ledger

- Transaction ingestion API.
- Encrypted DB fields.
- KMS envelope encryption.
- Local sync queue.
- Conflict/idempotency handling.

### Phase 3: Backend Analytics

- Category/merchant analytics jobs.
- Home narrative payload.
- Insight payloads.
- Goal impact payload.
- Encrypted derived summaries.

### Phase 4: GPT Advisor

- AI chat endpoint.
- Intent router.
- Deterministic tool results.
- Compact GPT context.
- Answer validator.
- Usage limits.
- Response cache.

### Phase 5: Learning And Shared Expenses

- Transaction line-item splits.
- Learned split patterns.
- Household groups.
- Friend groups.
- Split balances.
- Group analytics.

## 18. Open Technical Decisions

- Backend language: Kotlin/Spring or Node/NestJS.
- Auth provider: Firebase, Supabase, Auth0, Cognito, or custom.
- Cloud provider: AWS, GCP, or Supabase-managed stack.
- Encryption granularity: per-user DEK or per-record DEK.
- Local DB encryption library.
- Email provider priority: Gmail first or generic IMAP later.
- SMS policy approach for Play Store.
- Whether analytics engine should be shared Kotlin code or independently implemented backend code.
- Whether cloud sync is mandatory after sign-in or user-toggleable.
- Data retention period for encrypted deleted records.

## 19. Acceptance Criteria

- User can sign in with Google or Apple.
- Backend verifies identity token and issues app session.
- App can upload structured transactions.
- Backend stores sensitive transaction fields encrypted.
- KMS envelope encryption is implemented.
- Raw notification/SMS/email text is not stored by default.
- Backend can compute deterministic analytics from decrypted scoped data.
- GPT receives only compact structured context.
- AI answers are validated against deterministic facts.
- User can export data.
- User can delete account and data.
- AI usage and cost are tracked per user.
- App remains usable with local cached data when offline.

## 20. References

- Google Cloud KMS envelope encryption guidance: https://docs.cloud.google.com/kms/docs/envelope-encryption
- AWS KMS envelope encryption guidance: https://docs.aws.amazon.com/kms/latest/developerguide/kms-cryptography.html
- OWASP Cryptographic Storage Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html
- OWASP Key Management Cheat Sheet: https://cheatsheetseries.owasp.org/cheatsheets/Key_Management_Cheat_Sheet.html
