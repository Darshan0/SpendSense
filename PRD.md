# SpendSense Product Requirements Document

## 1. PM Review Summary

SpendSense should not compete as "another expense tracker." Expense trackers already exist, and most users stop using them because they require manual work and show charts without telling the user what to do.

The product wedge should be:

```text
Automatic transaction capture
  -> clean personal spend ledger
  -> daily financial diagnosis
  -> AI advisor that answers from real user data
```

The strongest differentiated features are:

- automatic tracking from notifications, SMS, and email where policy allows
- learned merchant/category corrections
- item-level splits for repeated transactions
- personal vs household vs friend-group expense separation
- advisor-style insights that explain the next action
- finance chat powered by deterministic calculations plus GPT explanation

The first production version should focus on daily usefulness and trust. Advanced features like learned item splits, Splitwise-style group ledgers, and full AI planning should be staged carefully because wrong automation in finance breaks trust quickly.

## 2. Product Positioning

SpendSense is an AI-assisted personal finance app for users who want to understand and improve their spending without manually tracking every rupee.

The app answers:

- Where did my money go recently?
- Why did this happen?
- Is this hurting my goal?
- What should I change today?
- Can I afford this purchase, EMI, rent, or lifestyle pattern?

SpendSense should feel like a practical financial analyst, not a generic chatbot and not a chart dashboard.

## 3. Target Market

### Initial Market

- India-first Android users.
- Users with frequent UPI, card, wallet, bank, SMS, and email transaction alerts.
- Young professionals with salary income and digital spending.
- Users who want automatic tracking and simple guidance.

### Expansion Markets

- Couples and households.
- Friend groups with shared expenses.
- Freelancers and variable-income users.
- Users planning loans, emergency funds, and large purchases.

## 4. Product Principles

### 4.1 Trust Before Automation

SpendSense can suggest categories, splits, and insights, but the user must stay in control. Any low-confidence financial interpretation must be confirmable and editable.

### 4.2 Deterministic Math Before GPT

GPT should not calculate financial truth. Backend/domain engines must calculate totals, trends, affordability, EMI, goal runway, split balances, and budget pressure. GPT explains these facts conversationally.

### 4.3 Narrative Before Charts

Every major screen should answer a user question. Charts are supporting evidence, not the product.

### 4.4 Recent Spend Before Monthly Totals

Monthly totals are useful, but they often overemphasize rent or one-time bills. Home should focus on today, yesterday, last 7 days, and behavior the user can still change.

### 4.5 Privacy Is A Product Feature

The app handles sensitive financial data. Raw notifications, SMS, and emails should be minimized, redacted, encrypted, and never sent to GPT by default.

## 5. Product Scope

### Core Product

SpendSense must provide:

- automatic transaction capture
- clean categorized transaction history
- recent-spend diagnosis
- financial advisor-style insights
- goal setting and goal impact
- AI chat for financial questions
- user correction and learning

### Differentiated Product

SpendSense should uniquely support:

- repeated transaction learning
- item-level split prediction
- shared expense routing
- household/friend group balances
- deterministic financial planning tools

## 6. Jobs To Be Done

### JTBD 1: Understand Recent Spending

When I open the app, I want to know what changed recently and whether I should adjust my behavior today.

Success means the user can understand the key issue in under 10 seconds.

### JTBD 2: Stop Manual Tracking

When I spend money, I want the app to capture it automatically from available transaction signals.

Success means most transactions appear without manual entry.

### JTBD 3: Correct Once, Learn Forever

When the app gets a merchant or category wrong, I want to correct it once and have the app improve next time.

Success means correction rate drops over time.

### JTBD 4: Understand Real Spend, Not Just Payment Amount

When one payment contains multiple things, I want to split it so my spending pattern is accurate.

Example:

```text
₹25 paid to a cigarette shop
  ₹22 smoking
  ₹3 chocolate/food

Next ₹50 payment to the same shop
  app suggests ₹44 smoking
  app suggests ₹6 chocolate/food
```

Success means repeated merchant patterns become more accurate with confirmation.

### JTBD 5: Separate Personal And Shared Money

When I pay for a restaurant, mart, household item, or friend-group expense, I want the app to track my real share and who owes whom.

Success means personal analytics are not inflated by reimbursable expenses.

### JTBD 6: Ask A Finance Question

When I ask about food spend, rent, EMI, loan, emergency fund, goal progress, or affordability, I want a grounded answer based on my data.

Success means answers cite calculated facts and do not repeat generic advice.

## 7. Personas

### Salary Professional

Needs:

- automatic spend capture
- daily guardrail
- eating out/shopping/subscription control
- goal progress
- EMI and purchase affordability

### Household Planner

Needs:

- grocery and household classification
- spouse/family split
- recurring household bills
- monthly budget pressure

### Social Spender

Needs:

- restaurant/friend group tagging
- who owes whom
- personal share vs total paid
- reimbursable spend tracking

### Habit Controller

Needs:

- repeated micro-spend detection
- smoking/alcohol/junk-food subcategory tracking
- weekly trend warning
- practical reduction target

## 8. Feature Requirements

### 8.1 Auto Transaction Capture

Required:

- Android notification capture.
- SMS transaction parsing where policy allows.
- Email transaction import where user explicitly connects an account.
- Manual entry as fallback.
- CSV import for onboarding/history.
- Duplicate detection across sources.
- Sensitive-message filtering for OTP, CVV, login, password reset, and authentication messages.

Product notes:

- Notification capture should be the primary Android wedge.
- SMS access may be limited by Play Store policy and should not be assumed as a guaranteed production path.
- Email import should use explicit OAuth consent and should scan only transaction-related mail where possible.

### 8.2 Transaction Ledger

Required:

- Transaction list grouped by day.
- Merchant, amount, account/source, category, confidence, and timestamp.
- Category icons and color-coded tags.
- Search and filters.
- Unknown merchant/category review queue.
- Edit transaction.
- Delete transaction.
- Export transactions.

### 8.3 Categorization And Learning

Required:

- Merchant normalization.
- Category assignment.
- Subcategory assignment.
- Confidence score.
- User correction memory.
- Merchant-level rules.
- Category rule priority.
- Review before auto-apply when confidence is low.

Examples:

- Swiggy -> Food / Eating out.
- Shell Petroleum -> Fuel.
- Zerodha -> Investment.
- Unknown UPI merchant -> Needs review.

### 8.4 Item-Level Transaction Splits

Purpose:

A single payment often hides multiple real spending behaviors. SpendSense should let the user split a transaction into line items and learn repeated patterns.

Required:

- Split one transaction into multiple line items.
- Each line item has amount, category, subcategory, note, and optional quantity.
- Sum of line items must equal parent transaction amount.
- User can save a split as a merchant pattern.
- App can suggest future splits from merchant, amount, frequency, and prior confirmations.
- Low-confidence split predictions require confirmation.
- User can edit, reject, pause, or delete learned split patterns.

Example:

```text
Merchant: Cigarette shop
Observed transaction: ₹25
Confirmed split:
  ₹22 Smoking
  ₹3 Food / Chocolate

Future transaction: ₹50
Suggested split:
  ₹44 Smoking
  ₹6 Food / Chocolate
```

Product rule:

Sensitive subcategories like smoking, alcohol, gambling, and junk food should be tracked clearly but without shaming copy. The insight should focus on financial impact and practical reduction.

### 8.5 Household And Friend Group Expenses

Purpose:

Users often pay for shared meals, groceries, rent, subscriptions, trips, and household items. Personal spend analytics must use the user's actual share, not always the full transaction amount.

Required:

- Create household groups.
- Create friend groups.
- Add participants.
- Tag eligible transactions as personal, household, or friend group.
- Equal split.
- Custom amount split.
- Percentage split.
- Paid-by tracking.
- Balance calculation.
- Settlement suggestions.
- Export group ledger.
- Learn merchant-to-group routing after repeated confirmations.

Candidate trigger merchants:

- restaurants
- cafes
- bars
- supermarkets
- marts
- grocery stores
- food delivery
- travel bookings
- shared subscriptions

Acceptance rule:

If a transaction is split, goal impact and personal spend analytics must use the user's share, not the total paid amount.

### 8.6 Home

Home must build a narrative, not a widget pile.

Required order:

1. Main diagnosis: what changed recently.
2. Today's decision: today vs daily guardrail.
3. Why it happened: category/merchant/subcategory driver.
4. Goal consequence: effect on active goal.
5. Recent transactions: supporting evidence.

Home must not:

- lead with monthly rent unless it affects current decisions
- repeat the same insight in multiple cards
- show charts without an action
- treat "Other" as a real behavior to cut

### 8.7 Insights

Insights should behave like a financial advisor's action list.

Required insight types:

- rising category warning
- falling category positive reinforcement
- eating-out warning
- smoking/alcohol/gambling habit warning
- reckless spending detection
- investment momentum
- subscription review
- daily guardrail breach
- goal delay warning
- shared spend/reimbursement warning
- uncategorized transaction cleanup

Each insight must include:

- issue
- evidence
- impact
- next action
- confidence

### 8.8 Goals

Required:

- Create goal.
- Target amount.
- Current savings.
- Optional target date.
- Goal progress.
- Goal impact from recent spend.
- Suggested amount to redirect from savings opportunities.
- AI follow-up questions against the goal.

Examples:

- Emergency fund.
- New laptop.
- Trip.
- Loan prepayment.
- Investment target.

### 8.9 AI Chat

The AI chat should answer all finance questions, but it must use structured data and deterministic tools.

Required:

- Ask anything conversational input.
- Precomputed suggested questions.
- Short default answers.
- Visual answer cards.
- Follow-up memory.
- Intent routing.
- Deterministic fallback.
- GPT-backed cloud response.
- Safety guardrails.

Supported question areas:

- how much did I spend on food?
- what changed this week?
- why am I overspending?
- can I buy this?
- can I afford this EMI?
- is my rent too high?
- how much emergency fund do I need?
- which category should I cut?
- how is my goal affected?
- who owes me money?
- why is this merchant categorized this way?

GPT should explain:

- calculated analytics
- planning results
- missing inputs
- tradeoffs
- practical next actions

GPT should not invent:

- transactions
- balances
- income
- tax rules
- investment returns
- participant balances

### 8.10 Financial Planning Engine

Required deterministic tools:

- category total calculator
- merchant total calculator
- daily budget calculator
- savings opportunity calculator
- goal runway calculator
- purchase affordability calculator
- EMI calculator
- loan affordability calculator
- rent burden calculator
- emergency fund calculator
- split balance calculator

Later:

- tax planning engine
- insurance adequacy
- net worth tracking
- credit/debt payoff strategy

## 9. Product Packaging

### Free

Free must be useful enough to build trust and habit.

- Notification transaction capture.
- Manual transaction entry.
- Basic transaction list.
- Basic categorization.
- Manual category correction.
- Today and 7-day summary.
- One active goal.
- Limited manual transaction splits.
- Limited AI answers, for example 5-10/month.
- Basic CSV export.

### Plus: Candidate ₹99/month

Plus should be the main consumer plan.

- Unlimited local/cloud transaction history.
- Advanced Home narrative.
- Multiple goals.
- Full category, merchant, and subcategory trends.
- Daily guardrails.
- Repeated merchant learning.
- Auto-suggested item splits.
- Subscription detection.
- Goal impact analysis.
- Cloud sync.
- More AI answers, for example 100/month.
- Improved CSV/Excel export.

### Pro: Candidate ₹199/month

Pro should be for users who want a fuller financial advisor.

- Higher AI usage.
- Advanced finance chat.
- Purchase what-if planning.
- EMI and loan planning.
- Emergency fund planning.
- Rent burden analysis.
- Household and friend-group ledgers.
- Auto group tagging.
- Settlement suggestions.
- PDF/Excel advisor reports.
- Priority insight refresh.
- Tax planning after compliance review.

### Enterprise / Partner

Later opportunities:

- employer financial wellness
- fintech/bank partnerships
- financial advisor dashboard
- white-label advisor reports

## 10. Pricing Hypothesis

Assume 100,000 monthly active users.

If infrastructure and GPT cost is:

- ₹4,00,000/month, cost is about ₹4 per active user.
- ₹8,00,000/month, cost is about ₹8 per active user.

At ₹99/month:

- ₹4,00,000 cost needs about 4,041 paying users.
- ₹8,00,000 cost needs about 8,081 paying users.
- This requires roughly 4%-8% paid conversion from 100,000 active users.

Cost-control principles:

- Do not give unlimited GPT to free users.
- Cache repeated insight answers.
- Use deterministic analytics before GPT.
- Use cheaper models for categorization/summarization.
- Use stronger GPT only for complex planning and chat.

## 11. Backend Requirements

### 11.1 Backend Data

Store:

- users
- devices
- source connections
- transactions
- transaction line items
- merchant corrections
- learned merchant patterns
- categories and subcategories
- goals
- households
- friend groups
- group participants
- group ledgers
- settlement balances
- insights
- chat sessions
- AI usage counters
- exports

### 11.2 Backend Processing

Required:

- transaction ingestion API
- duplicate reconciliation
- transaction enrichment
- merchant normalization
- category/subcategory engine
- split prediction engine
- group routing engine
- analytics jobs
- insight generation jobs
- GPT context builder
- GPT response cache
- usage/rate limiting
- cost tracking per user and plan

### 11.3 GPT Data Contract

GPT may receive:

- compact user profile
- aggregate spend summaries
- category/merchant/subcategory summaries
- goal summaries
- split summaries
- group balance summaries
- deterministic planning outputs

GPT should not receive by default:

- raw notification text
- raw SMS/email body
- OTP/auth content
- full unbounded transaction history
- full account identifiers
- sensitive data unrelated to the answer

## 12. Success Metrics

### Activation

- Permission onboarding completion.
- First transaction captured.
- First category correction.
- First goal created.
- First useful insight viewed.

### Engagement

- Daily active users.
- Weekly active users.
- Home visits per user.
- AI questions per user.
- Insight actions taken.
- Transaction corrections.
- Split confirmations.
- Group tags.

### Quality

- Parser precision.
- Duplicate rate.
- Category accuracy.
- Subcategory accuracy.
- Split prediction acceptance rate.
- Group routing acceptance rate.
- GPT answer helpfulness.
- Crash-free sessions.

### Monetization

- Free-to-paid conversion.
- Paid retention.
- AI cost per active user.
- AI cost per paying user.
- Gross margin by plan.
- Churn by plan.

### Financial Outcome

- Users reducing discretionary spend.
- Users reducing habit spend.
- Users progressing goals.
- Users staying inside daily guardrail.
- Users recovering reimbursable group expenses.

## 13. Risks And Constraints

- SMS access may be restricted by platform/store policy.
- Email import requires OAuth consent and security review.
- Notification formats vary by bank/app.
- Wrong transaction capture breaks trust.
- Wrong auto-splits break trust faster than wrong categories.
- Shared expenses can distort analytics if personal share is wrong.
- GPT costs can grow quickly without limits.
- Financial advice language needs compliance review.
- Privacy expectations are high because financial data is sensitive.

## 14. Safety And Compliance

SpendSense must:

- avoid claiming to be a licensed financial advisor unless legally reviewed
- avoid guaranteed returns
- avoid tax evasion advice
- avoid loan fraud or hiding liability advice
- distinguish educational guidance from regulated advice
- ask for missing inputs before tax/loan planning
- encrypt sensitive data in transit and at rest
- allow export and deletion
- disclose when AI is used

## 15. Roadmap

### Phase 1: Trustworthy Tracker

Goal: create a reliable transaction ledger and daily narrative.

- Notification capture.
- Manual entry.
- Deterministic parsing.
- Room/local DB.
- Basic categorization.
- User corrections.
- Home narrative.
- Recent transactions.
- One goal.
- Basic insights.

### Phase 2: Intelligence Layer

Goal: make the product feel smarter than an expense tracker.

- Advanced analytics engine.
- Category and merchant trends.
- Daily guardrail.
- Goal impact.
- Unknown merchant cleanup.
- AI chat with deterministic context.
- Basic GPT backend.
- Cloud sync.

### Phase 3: Learning System

Goal: reduce manual effort through user-confirmed learning.

- Merchant correction memory.
- Subcategory rules.
- Manual item splits.
- Learned repeated transaction patterns.
- Auto-suggested splits.
- Confidence and review workflows.

### Phase 4: Shared Money

Goal: separate personal, household, and friend-group money.

- Household groups.
- Friend groups.
- Split ledger.
- Personal share analytics.
- Settlement suggestions.
- Group export.
- Auto group tagging.

### Phase 5: Financial Advisor

Goal: make AI planning good enough to pay for.

- Purchase affordability.
- EMI planning.
- Loan affordability.
- Emergency fund.
- Rent burden.
- Subscription review.
- Advanced advisor chat.
- PDF/Excel reports.
- Tax module only after compliance review.

## 16. MVP Recommendation

The first production MVP should not include everything.

Recommended MVP:

- notification capture
- manual transaction entry
- transaction list
- merchant/category correction
- Home narrative
- 7-day insights
- one goal and goal impact
- AI chat for common spend questions
- export

Recommended Beta after MVP:

- manual item splits
- repeated merchant learning
- cloud sync
- GPT-backed advisor

Recommended Pro after Beta:

- advanced AI finance planning
- auto split suggestions
- household/friend split ledger
- reports

## 17. Open Decisions

- Should production be offline-first with optional cloud sync, or cloud-first?
- Can SMS tracking pass target-store policy?
- Which email provider should launch first?
- What is the minimum accuracy required before auto-categorization?
- What is the minimum confidence required before auto-splitting?
- Should repeated transaction learning be Plus or Pro?
- Should friend-group ledgers be Pro-only?
- Should salary be entered manually, inferred, or both?
- What AI limits should Free, Plus, and Pro include?
- Should uncategorized transactions block advisor insights?
- Should the first market be India-only?

## 18. Production Acceptance Criteria

- User can capture or manually add transactions.
- User can see a coherent Home narrative.
- User can correct merchants and categories.
- User can create and track at least one goal.
- User can ask common finance questions and receive grounded answers.
- All money calculations are deterministic.
- GPT answers use compact structured context.
- User can export data.
- User can delete data.
- AI usage limits are enforced.
- Admin can monitor AI cost per user and plan.

