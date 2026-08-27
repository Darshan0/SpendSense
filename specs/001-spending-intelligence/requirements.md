# Spending Intelligence Requirements

## Goal

Move SpendSense from transaction tracking toward financial intelligence:

```text
local transactions
  -> deterministic analytics
  -> goals
  -> insights
  -> assistant-ready context
  -> local LLM explanation when available
```

## Requirements

- Compute spending summaries locally without an LLM.
- Show total expense, income, net cash flow, top categories, and top merchants.
- Store user goals locally.
- Show goal progress alongside spending context.
- Generate deterministic insights from transaction and goal data.
- Define a `LocalLanguageModel` abstraction.
- Keep a deterministic assistant fallback when no local model is available.
- Never let the LLM write directly to the database.

## Acceptance Criteria

- Insights and assistant answers render without network access.
- A `NoOpLanguageModel` keeps the app functional on devices without local LLM support.
- Goals are persisted in Room.
- Assistant output is derived from structured local analytics, not raw transaction history prompts.
