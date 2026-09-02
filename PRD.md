# SpendSense Product Requirements Document

## 1. Product Summary

SpendSense is a personal finance intelligence app that helps users understand where their money is going, why their spending pattern matters, and what actions can improve their financial goals.

The product starts from automatic transaction capture and grows into an AI-assisted financial advisor. The app should not behave like a generic expense tracker that only shows charts. It should build a useful narrative:

```text
What happened recently?
Why did it happen?
Is this good or bad for my goal?
What should I do next?
Can I ask follow-up questions?
```

## 2. Product Vision

SpendSense should become the user's everyday money co-pilot:

- It automatically understands spending from notifications, SMS, and email transaction messages.
- It explains financial behavior in plain language.
- It connects daily spending to goals, savings, debt, and future plans.
- It helps users make better decisions before money leaves the account.
- It becomes more useful as it learns from corrected merchants, item splits, categories, goals, income, groups, and user preferences.

## 3. Target Users

### Primary Users

- Indian digital payment users who receive bank, UPI, card, wallet, SMS, and email transaction alerts.
- Users who want automatic tracking but do not want to manually enter every spend.
- Young professionals who want to control eating out, shopping, subscriptions, rent pressure, and goal progress.
- Users who want financial clarity but are not ready to hire a financial planner.

### Secondary Users

- Couples or households managing shared expenses.
- Users planning big purchases, loans, emergency funds, or savings goals.
- Freelancers or variable-income users who need cash-flow visibility.

## 4. Core Product Principles

### 4.1 Correctness Before AI

Financial calculations must be deterministic. GPT or any LLM should explain and reason from structured facts, not invent numbers.

Examples:

- Category totals are calculated by backend analytics.
- EMI is calculated by a finance engine.
- Tax planning uses explicit jurisdiction and regime inputs.
- Goal runway uses stored goals, income, expenses, and savings assumptions.

### 4.2 Narrative Before Charts

The home screen should not be a collection of disconnected cards. It should tell a story:

- Main diagnosis.
- Today's spending decision.
- Category/merchant cause.
- Goal consequence.
- Supporting transactions.

### 4.3 User Control

The user must be able to correct merchants, categories, goals, salary, household data, and assumptions. Corrections should improve future analysis.

User corrections are not only one-time edits. SpendSense should learn repeat behavior:

- merchant-level category rules
- item-level splits
- recurring transaction patterns
- household/friend group tags
- split participants and ratios

### 4.4 Privacy By Design

Raw financial notifications are sensitive. The system should minimize retention, redact where possible, and avoid sending raw notification text to GPT.

### 4.5 Free Product Must Be Useful

The free version must create trust and daily habit. Pro should unlock deeper advisory, automation, and higher AI usage.

## 5. Product Goals

- Automatically capture and structure financial transactions.
- Support transaction tracking from notification, SMS, and email sources where user permission and platform policy allow it.
- Categorize transactions with high accuracy.
- Learn repeated transaction patterns and split them into subcategories.
- Detect restaurant, mart, and shared expenses and route them into household or friend groups.
- Show useful recent-spend insights, not only monthly totals.
- Help users understand spending impact on goals.
- Provide AI chat that can answer personal finance questions from user data.
- Support cloud sync and backend analytics for scale.
- Create a freemium monetization path that can support 100,000+ active users.

## 6. Non-Goals For Initial Production Launch

- Investment execution or stock trading.
- Guaranteed investment recommendations.
- Licensed financial advisory claims.
- Full tax filing.
- Bank account scraping without user consent.
- Lending or credit underwriting.
- Selling user financial data.

## 7. Main User Journeys

### 7.1 First-Time Setup

1. User installs SpendSense.
2. User sees a clear privacy and value explanation.
3. User grants notification, SMS, or email permissions based on the capture sources they want to enable.
4. User optionally enters salary, current savings, and first goal.
5. App starts capturing transactions.
6. App shows first insights after enough transactions are available.

### 7.2 Daily Check-In

1. User opens Home.
2. User sees the main recent-spend diagnosis.
3. User sees today's spend against a daily guardrail.
4. User sees what caused the pressure.
5. User sees how it affects a goal.
6. User can ask AI a follow-up.

### 7.3 Transaction Correction

1. User opens History.
2. User sees grouped transactions by day.
3. Unknown merchant/category is highlighted.
4. User corrects merchant/category.
5. App remembers the correction.
6. Future insights improve.

### 7.4 Repeated Transaction Learning

1. User pays a merchant where the single payment includes multiple real items.
2. SpendSense asks what the transaction contained.
3. User splits the amount into subcategories.
4. SpendSense stores a learned merchant pattern.
5. When a similar amount appears again, SpendSense predicts the split.
6. User confirms or corrects the split.
7. Repeated confirmations increase confidence and reduce future questions.

Example:

```text
Transaction: ₹25 paid to cigarette shop
User correction: ₹22 smoking, ₹3 chocolate/food
Future ₹50 transaction: app predicts ₹44 smoking, ₹6 chocolate/food
```

### 7.5 Group And Household Expense Routing

1. User pays at a restaurant, supermarket, delivery app, or mart.
2. SpendSense asks whether the spend was personal, household, or with friends.
3. User selects a group and participants.
4. App records the split and each person's share.
5. Future similar merchant transactions can be auto-tagged to the same group.
6. The app shows who owes whom and can export or sync settlement data.

### 7.6 AI Financial Advisor Chat

1. User asks a question such as "Can I buy a phone for ₹50,000?"
2. Backend classifies intent.
3. Deterministic planning engine calculates affordability, goal delay, or missing inputs.
4. GPT explains the result conversationally.
5. User gets a concise answer with visual cards and next actions.

### 7.7 Goal Planning

1. User creates a goal with target amount and current savings.
2. App tracks spend pressure against the goal.
3. App suggests savings opportunities.
4. AI answers what-if questions against the goal.

### 7.8 Export And Review

1. User opens Profile.
2. User exports transactions and analysis.
3. Export includes transactions, categories, merchant cleanup state, monthly totals, recent trends, and advisor notes.

## 8. Feature Inventory

### 8.1 Transaction Capture

- Notification access onboarding.
- SMS permission onboarding where supported and policy-compliant.
- Email connection onboarding for transaction emails.
- Bank, UPI, wallet, card, SMS, and email transaction parsing.
- Sensitive-message filtering for OTP, CVV, password reset, and authentication messages.
- Duplicate detection.
- Refund/reversal detection.
- Offline local capture queue.
- Manual transaction entry.
- Bulk import from CSV.

### 8.2 Transaction Source Connectors

- Android notification listener.
- SMS transaction reader.
- Email transaction connector.
- Source-level permission controls.
- Source health status.
- Duplicate reconciliation across sources.
- Message redaction before storage or GPT usage.

### 8.3 Transaction Intelligence

- Merchant extraction.
- Merchant normalization.
- Category assignment.
- Subcategory assignment.
- Confidence scoring.
- User correction memory.
- Unknown merchant review queue.
- Category color and icon system.
- Grouped transactions by day.
- Search and filters.

### 8.4 Repeated Transaction Learning

SpendSense must support learned transaction patterns for merchants where the same payment often contains multiple item types.

Required behavior:

- Ask the user to explain ambiguous repeated transactions.
- Allow a transaction to be split into multiple line items.
- Store line items with category, subcategory, amount, and optional note.
- Learn merchant templates from confirmed splits.
- Predict future splits from merchant, amount, time, frequency, and prior confirmations.
- Show low-confidence predictions for confirmation.
- Auto-apply high-confidence predictions.
- Support proportional prediction for repeated quantities.

Example pattern:

```text
Merchant: Cigarette shop
Observed amount: ₹25
Confirmed split:
  ₹22 smoking
  ₹3 food/chocolate

Future amount: ₹50
Predicted split:
  ₹44 smoking
  ₹6 food/chocolate
```

Acceptance requirements:

- The parent transaction amount must always equal the sum of split line items.
- Split suggestions must never silently overwrite user corrections.
- The user must be able to edit, reject, or delete a learned pattern.
- Smoking, alcohol, gambling, and other sensitive categories must be handled without moralizing, but insights can clearly warn about financial and health-adjacent harm.

### 8.5 Group, Household, And Splitwise-Style Expenses

SpendSense should identify transactions that may belong to a household or friend group.

Candidate merchants:

- restaurants
- cafes
- bars
- grocery stores
- supermarkets
- marts
- food delivery
- travel bookings
- shared subscriptions

Required behavior:

- Ask whether eligible transactions are personal, household, or group expenses.
- Let user create household and friend groups.
- Let user assign participants.
- Support equal split, custom amount split, percentage split, and paid-by-user tracking.
- Calculate balances by participant.
- Show settlement suggestions.
- Learn merchant-to-group routing after repeated confirmations.
- Export group expense ledger.
- Keep personal analytics separate from reimbursable group spend.

Acceptance requirements:

- Personal spend, household spend, and reimbursable friend-group spend must be distinguishable in analytics.
- Goal impact should use the user's final personal share, not the full transaction amount when split is enabled.
- The app must make it clear when a spend is waiting for split confirmation.

### 8.6 Analytics Engine

- Today spend.
- Yesterday spend.
- Last 7 days spend.
- Previous 7 days comparison.
- Daily spend guardrail.
- Category trend up/down.
- Merchant trend up/down.
- Monthly context.
- Income, expense, and net cash flow.
- Fixed vs variable spend.
- Discretionary spend ratio.
- Personal vs shared spend.
- Reimbursable spend.
- Subcategory trend, for example smoking, chocolate, eating out, groceries.
- Savings opportunity calculation.
- Budget health score.

### 8.7 Home

- Narrative diagnosis card.
- Today's decision card.
- Why it happened card.
- Daily rhythm visualization.
- Goal impact card.
- Recent grouped transactions.
- Empty states that explain what data is needed.
- No repeated disconnected insight cards.

### 8.8 Insights

- Advisor-style actionable insights.
- Rising category warnings.
- Eating-out warnings.
- Tobacco/cigarette warning when detected.
- Reckless spending warnings.
- Investment momentum tracking.
- Subcategory habit warnings.
- Shared-spend/reimbursement warnings.
- Savings opportunity ranking.
- Positive reinforcement when spend improves.
- Simple visualization per insight.

### 8.9 AI Chat

- Personal finance chat from structured user data.
- Precomputed common question suggestions.
- Visual answer cards.
- Short answers by default.
- Follow-up context.
- Budget, category, merchant, goal, loan, EMI, rent, and emergency fund questions.
- Questions about repeated merchants and item splits.
- Questions about household/friend group balances.
- Missing-input detection.
- Safety guardrails.
- GPT-backed cloud answers for production.
- Deterministic fallback when GPT is unavailable.

### 8.10 Financial Planning

- Purchase affordability.
- EMI calculator.
- Loan affordability.
- Rent burden analysis.
- Emergency fund planning.
- Goal runway.
- Cash-flow planning.
- Budget health analysis.
- Subscription review.
- Tax planning placeholder with explicit missing inputs.

### 8.11 Profile

- Profile photo.
- Display name.
- Salary.
- Household members.
- Friend groups.
- Goals.
- Notification access settings.
- SMS/email connector settings.
- Export CSV.
- Logout.
- Data/privacy settings.
- Subscription plan management.

### 8.12 Cloud Backend

- User accounts.
- Secure transaction sync.
- SMS/email source sync metadata.
- Backend analytics.
- GPT insight generation.
- Insight caching.
- Chat history.
- Device sync.
- Push notifications.
- Background jobs.
- Admin dashboard.
- Abuse/rate-limit controls.

## 9. Free Vs Pro Candidates

This is a starting point. Final packaging should be decided after usage analytics and cost testing.

### Free

- Notification-based transaction capture.
- Basic SMS/email transaction import where supported.
- Basic transaction list.
- Basic category totals.
- Today and 7-day spend summary.
- Limited goal tracking, for example 1 goal.
- Limited AI suggestions, for example 5-10 AI answers/month.
- Manual category correction.
- Manual transaction splitting.
- Basic CSV export.

### Plus: Suggested ₹99/month

- Unlimited transaction history.
- Advanced Home narrative.
- Multiple goals.
- Full category and merchant trend insights.
- Repeated merchant learning.
- Auto-suggested transaction splits.
- Daily guardrails.
- More AI chat usage, for example 100 answers/month.
- Goal impact analysis.
- Subscription detection.
- Improved exports.
- Cloud sync across devices.

### Pro: Suggested ₹199/month

- Higher AI usage.
- Advanced financial advisor chat.
- Loan/EMI planning.
- Purchase what-if planning.
- Emergency fund planning.
- Household/shared planning.
- Friend group split tracking.
- Auto group tagging for restaurants and marts.
- Priority insight refresh.
- Advanced reports.
- Tax planning when implemented.
- PDF/Excel advisor reports.

### Enterprise Or Partner Later

- Employer financial wellness.
- Bank/fintech white-label.
- Financial advisor dashboard.
- Aggregated anonymized analytics only with explicit consent.

## 10. Pricing Hypothesis

The app should not rely on every active user paying. A realistic consumer model assumes a free user base and paid conversion.

At 100,000 monthly active users:

- If monthly operating cost is ₹4,00,000, cost is about ₹4 per active user.
- If monthly operating cost is ₹8,00,000, cost is about ₹8 per active user.
- At ₹99/month, 4,000-8,100 paying users can roughly cover ₹4L-₹8L infra/GPT cost.
- That implies a 4%-8% paid conversion target from 100,000 active users.

Pricing should protect GPT usage:

- Free users get strict AI limits.
- Paid users get larger limits.
- Expensive GPT calls should be cached.
- Deterministic analytics should handle common questions before GPT.

## 11. Backend Product Requirements

### 11.1 Data Storage

The backend should store:

- Users.
- Devices.
- Transactions.
- Transaction line-item splits.
- Learned merchant split patterns.
- Merchant corrections.
- Category rules.
- Subcategory rules.
- Groups and participants.
- Group expense ledgers.
- Settlement balances.
- Goals.
- Profile and salary assumptions.
- Insights.
- Chat sessions.
- AI usage counters.
- Export jobs.

### 11.2 Processing

The backend should:

- Accept sanitized transactions from app.
- Reconcile duplicate transactions across notification, SMS, and email.
- Learn and apply repeated transaction split patterns.
- Compute personal share vs shared/reimbursable share.
- Run analytics jobs.
- Precompute insight summaries.
- Generate GPT-ready compact context.
- Cache common answers.
- Enforce AI rate limits.
- Track model cost per user.

### 11.3 GPT Usage

GPT should receive:

- Structured summaries.
- Relevant aggregates.
- Selected recent merchants/categories.
- Subcategory and split summaries.
- Group balance summaries.
- Goal data.
- Deterministic planning results.

GPT should not receive by default:

- Raw notification text.
- OTP/auth messages.
- Full unbounded transaction history.
- Sensitive account identifiers beyond redacted suffixes.

## 12. Success Metrics

### Activation

- Notification access completion rate.
- First transaction captured.
- First correction completed.
- First split transaction confirmed.
- First group expense created.
- First goal created.
- First useful insight viewed.

### Engagement

- Daily active users.
- Weekly active users.
- Home screen visits.
- AI questions asked.
- Insight card taps.
- Category corrections.
- Split confirmations.
- Group expense tagging.

### Financial Outcome

- Users who reduce rising discretionary categories.
- Users who create and progress goals.
- Users who follow daily guardrail.
- Users who reduce harmful or low-value subcategory habits.
- Users who recover/reconcile reimbursable group expenses.
- Savings opportunity accepted.

### Monetization

- Free-to-paid conversion.
- Paid retention.
- AI cost per paying user.
- Gross margin per plan.
- Churn by plan.

### Trust

- Parser accuracy.
- Category accuracy.
- Subcategory split accuracy.
- Group routing accuracy.
- Correction rate.
- Crash-free sessions.
- Export/download usage.
- Data deletion requests.

## 13. Key Risks

- Notification parsing accuracy varies across banks and wallets.
- SMS access can be restricted by Android and Google Play policy, so notification/email import may need to be the primary production path.
- Email import requires explicit user consent and provider-specific OAuth/security review.
- Unknown merchant/category quality can make insights weak.
- Wrong learned split patterns can damage trust if applied without confirmation.
- Group expenses can distort personal analytics if reimbursements are not modeled correctly.
- GPT costs can grow quickly without caching and limits.
- Users may not trust financial advice unless it cites data clearly.
- Financial advice safety and compliance must be handled carefully.
- Cloud sync increases privacy expectations.
- Too many cards can confuse the user instead of helping.

## 14. Safety And Compliance Requirements

- The app must not claim to be a licensed financial advisor unless legally reviewed.
- The app must not guarantee returns.
- The app must not encourage tax evasion, loan fraud, or hiding liabilities.
- The app must distinguish educational guidance from regulated advice.
- Tax advice must require jurisdiction, tax regime, income, deductions, and filing status.
- Users must be able to export and delete their data.
- Sensitive data must be encrypted in transit and at rest.

## 15. Roadmap

### Phase 1: Strong Local Product

- Reliable notification capture.
- SMS/email import feasibility spike.
- Deterministic parser.
- Local DB.
- Home narrative.
- Insights.
- AI chat with local/deterministic fallback.
- Profile, goals, export.
- Manual transaction split.

### Phase 2: Cloud MVP

- User auth.
- Backend transaction sync.
- Email transaction connector.
- Managed Postgres.
- Backend analytics engine.
- GPT-backed advisor.
- Usage limits.
- Insight caching.
- Basic paid plan.

### Phase 3: Advisor Depth

- Advanced planning engine.
- Loan/EMI.
- Emergency fund.
- Rent burden.
- Purchase what-if.
- Repeated transaction learning.
- Item-level subcategory splits.
- Household planning.
- Friend group split ledger.
- Better merchant learning.

### Phase 4: Monetization And Scale

- Plus and Pro plans.
- Billing.
- Cost dashboard.
- Model routing by plan.
- Referral loop.
- Push notification nudges.
- Retention experiments.

### Phase 5: Financial Platform

- Bank integrations where viable.
- Advisor reports.
- Tax module after compliance review.
- Partner/enterprise distribution.

## 16. Open Decisions

- Should default production architecture remain offline-first with optional cloud sync, or become cloud-first?
- Can SMS tracking pass target-store policy, or should SMS be limited to local/import/manual flows?
- Which email providers should be supported first?
- Should GPT answers be available in Free, or only precomputed insight suggestions?
- Should Pro include household planning at launch or later?
- Should repeated transaction learning be Plus or Pro?
- Should friend-group Splitwise-style tracking be Pro only?
- Should salary be manually entered, inferred, or both?
- How much transaction history should be sent to backend by default?
- What exact AI monthly limits should Plus and Pro include?
- Should uncategorized transactions block some insights until reviewed?
- Should the first market be India-only?

## 17. MVP Production Acceptance Criteria

- User can capture transactions from notifications.
- User can see a coherent Home narrative.
- User can correct unknown merchants/categories.
- User can split a transaction into subcategories.
- App can learn a repeated merchant split after confirmation.
- User can tag eligible transactions as personal, household, or friend group.
- User can create and track at least one goal.
- User can ask common finance questions and get grounded answers.
- Backend stores transactions and insights securely.
- GPT answers are generated from compact structured context.
- Free and paid usage limits are enforced.
- Admin can monitor GPT cost per user and per plan.
- User can export data.
- User can delete account data.
