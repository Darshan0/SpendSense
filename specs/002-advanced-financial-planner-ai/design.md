# Advanced Financial Planner AI Design

## Architecture

The assistant becomes a planner-backed advisor:

```text
User message
  -> AssistantIntentClassifier
  -> FinancialPlanningEngine
  -> PlanningResult
  -> AdvisorResponsePlanner
  -> Local/cloud LLM
  -> Fact validator
  -> Chat response
```

The deterministic finance engine owns numbers. The LLM owns explanation, tone, and conversational continuity.

## Package Layout

```text
features/finance/domain
  FinancialAnalyticsEngine.kt
  FinancialPlanningEngine.kt
  AnalyticsUseCases.kt
  FinanceModels.kt
```

Later refactor target:

```text
features/finance/domain/analytics
features/finance/domain/planning
features/finance/domain/assistant
```

For this slice, keep files small enough to move later without changing behavior.

## Planning Domain Model

```kotlin
enum class PlanningType {
    PURCHASE_AFFORDABILITY,
    LOAN_AFFORDABILITY,
    EMERGENCY_FUND,
    RENT_BURDEN,
    GOAL_RUNWAY,
    TAX_READINESS,
    GENERAL_ADVISOR
}

enum class PlanningVerdict {
    AFFORDABLE,
    CAUTION,
    DELAY,
    NEEDS_MORE_INFO,
    NOT_SUPPORTED_YET
}

data class PlanningResult(
    val type: PlanningType,
    val verdict: PlanningVerdict,
    val confidence: PlanningConfidence,
    val summary: String,
    val facts: List<PlanningFact>,
    val risks: List<PlanningRisk>,
    val recommendations: List<ActionStep>,
    val assumptions: List<String>,
    val missingInputs: List<String>
)
```

## Intent Mapping

```text
Can I buy/spend/purchase ₹X? -> WHAT_IF -> PURCHASE_AFFORDABILITY
loan/emi/borrow -> LOAN_AFFORDABILITY
rent too high -> RENT_BURDEN
emergency fund/runway -> EMERGENCY_FUND
tax -> TAX_READINESS
goal/save for -> GOAL_RUNWAY
I feel/worried/confused -> CASUAL_REFLECTION -> GENERAL_ADVISOR
```

## Calculators

### EMI

Formula:

```text
monthlyRate = annualRate / 12 / 100
emi = P * r * (1 + r)^n / ((1 + r)^n - 1)
```

If rate or tenure is missing, do not guess. Return missing inputs.

### Purchase Affordability

Inputs:

- Proposed amount.
- Net cash flow.
- Savings rate.
- Active goal gap.
- Best savings opportunity.

Rules:

```text
amount <= 25% monthly surplus and savings rate >= 20 -> AFFORDABLE
amount <= monthly surplus and savings rate >= 10 -> CAUTION
otherwise -> DELAY
```

### Loan Affordability

Inputs:

- Principal.
- Annual interest rate.
- Tenure months.
- Income.
- Existing expense ratios.

Rules:

```text
emi burden <= 20% income and post-EMI savings >= 20% -> AFFORDABLE
emi burden <= 35% income and post-EMI savings >= 10% -> CAUTION
otherwise -> DELAY
```

### Rent Burden

Rules:

```text
rent <= 30% income -> AFFORDABLE
rent <= 40% income -> CAUTION
rent > 40% income -> DELAY / high burden
```

### Emergency Fund

Core monthly expense:

```text
fixed needs + variable needs
```

Target:

```text
3 months minimum
6 months strong
```

If liquid savings is unknown, return needed target and ask user for liquid savings.

### Tax Readiness

This slice only detects missing inputs:

- Country/state.
- Tax regime.
- Gross annual income.
- Deductions.
- Filing status.

Verdict: `NOT_SUPPORTED_YET`.

## LLM Prompt Strategy

The prompt must include:

- intent
- planning result
- recent conversation
- analytics summary
- response style rules

The LLM should not receive raw transactions unless required and anonymized. For now it receives aggregated analytics only.

## Validation Strategy

For exact totals:

- answer must include amount/category/merchant.

For planning:

- answer must mention at least one planning fact, verdict, risk, recommendation, or exact calculated amount.

For missing-input results:

- answer must mention at least one missing input.

If validation fails, show deterministic planner summary.

## Edge Cases

- Missing income: cannot calculate ratios confidently.
- Unknown rent category: rent burden returns missing input.
- Investment outflows: treated as savings behavior, not lifestyle expense.
- Transfers: excluded from lifestyle-spend advice.
- Huge one-time expense: should be flagged as unusual, not assumed monthly.
- Tax questions: never fabricate law.
- Loan questions without rate/tenure: ask for details.
- User emotional wording: respond calmly but ground in facts.
- Exact category questions: still answer exact totals directly.

## Future Extension

Cloud mode can use the same `PlanningResult` payload. The backend should receive structured, anonymized facts rather than raw notifications.
