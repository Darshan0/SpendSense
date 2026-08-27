# Advanced Financial Planner AI Requirements

## Problem

SpendSense currently answers spend questions from local transaction analytics, but casual finance conversation can still feel repetitive because the assistant has limited planning capability. The next product milestone is to make the assistant behave more like a financial advisor by routing user questions to deterministic planning tools before the LLM explains the result.

The LLM must not be trusted to calculate EMI, affordability, emergency fund runway, rent burden, taxation, or goal impact on its own.

## Product Goal

Build an advanced, local-first financial planning layer that:

- Understands broad finance intents, not only category totals.
- Calculates planning facts deterministically.
- Detects missing inputs instead of guessing.
- Gives the LLM structured facts, assumptions, risks, and next actions.
- Validates generated answers against tool output.
- Keeps private transaction details local by default.

## In Scope For This Slice

This slice implements the first complete planning engine:

- What-if purchase affordability.
- Loan EMI calculation.
- Loan affordability.
- Emergency fund runway.
- Rent burden.
- Goal runway.
- Budget health linkage.
- Structured planning output for the assistant.
- Advisor-style responses for casual, budget, loan, affordability, and goal questions.
- Tests for calculations and assistant routing.

## Out Of Scope For This Slice

- Real tax-law implementation.
- Investment recommendation engine.
- Insurance planning.
- Credit score integration.
- Bank account linking.
- Cloud LLM backend.
- Model fine-tuning.
- Full regulatory compliance review.

Tax questions must return a missing-input / future capability response rather than fabricated tax advice.

## User Stories

### Financial Reflection

As a user, when I say:

```text
I feel like I am spending too much
```

SpendSense should respond conversationally, but ground the answer in budget health, discretionary ratio, top pressure points, or savings opportunity.

### Purchase What-If

As a user, when I ask:

```text
Can I buy a phone for ₹50,000?
```

SpendSense should:

- Parse the proposed amount.
- Compare it with current surplus.
- Show impact on savings rate.
- Show whether it delays a goal.
- Give a verdict: affordable, caution, delay, or missing information.

### Loan / EMI

As a user, when I ask:

```text
Can I afford a ₹10 lakh loan at 10% for 5 years?
```

SpendSense should:

- Parse principal, interest rate, and tenure when present.
- Calculate EMI deterministically.
- Compare EMI with income and existing fixed/discretionary burden.
- Check post-EMI savings rate.
- Warn if inputs are missing.

### Rent Burden

As a user, when I ask:

```text
Is my rent too high?
```

SpendSense should compare rent against tracked income and provide a status based on configured thresholds.

### Emergency Fund

As a user, when I ask:

```text
How much emergency fund do I need?
```

SpendSense should estimate required emergency fund using core monthly expenses and show missing inputs if current liquid savings are unknown.

### Tax

As a user, when I ask:

```text
How can I save tax?
```

SpendSense should not invent tax advice. It should ask for jurisdiction, tax regime, gross income, deductions, and filing status, and mark tax planning as not implemented in this slice.

## Functional Requirements

- The app must classify planning intents separately from generic chat.
- The app must produce a structured `PlanningResult`.
- Every planning result must include:
  - `type`
  - `verdict`
  - `confidence`
  - `summary`
  - `facts`
  - `risks`
  - `recommendations`
  - `assumptions`
  - `missingInputs`
- EMI must use the standard reducing-balance formula.
- Money must remain integer minor units except temporary calculation internals.
- If key inputs are missing, result confidence must drop and missing inputs must be explicit.
- LLM prompts must include planning results when relevant.
- For planning intents, generated answers must mention at least one grounded planning fact or fallback to deterministic advisor text.

## Non-Functional Requirements

- All calculations must run locally.
- No raw notification text should be sent to an LLM.
- The planning engine must live in the finance domain layer.
- The data layer must not contain financial-planning rules.
- Tests must cover core math and assistant routing.
- The implementation must be extensible for cloud model use later.

## Safety Requirements

- The assistant must not present itself as a licensed financial advisor.
- The assistant must not guarantee investment returns.
- The assistant must not help with tax evasion, loan fraud, or hiding liabilities.
- Tax answers must ask for required inputs and avoid jurisdiction-specific claims until a tax engine is implemented.
- Loan answers must include affordability caveats when income, interest rate, tenure, or existing debt is missing.

## Acceptance Criteria

- A purchase question produces a what-if planning result instead of generic cutback advice.
- A loan question with principal/rate/tenure produces an EMI amount.
- A loan question missing rate or tenure asks for missing inputs.
- A rent question references rent burden.
- Casual reflection uses advisor facts and does not repeat the same answer as exact category questions.
- Existing exact amount questions still return exact deterministic totals.
- Unit tests pass.
- `assembleDebug` passes.
