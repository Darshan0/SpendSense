# Advanced Financial Planner AI Tasks

## Phase 1: Spec

- [x] Write requirements for planner-backed advisor.
- [x] Write design with engine, calculators, validation, and edge cases.
- [x] Define acceptance criteria.

## Phase 2: Domain Model

- [x] Add planning result models.
- [x] Add planner input parser for amount, rate, and tenure.
- [x] Add deterministic financial planning engine.

## Phase 3: Assistant Integration

- [x] Route loan, EMI, tax, rent, emergency fund, and purchase questions to planner.
- [x] Add planner result to LLM context.
- [x] Add deterministic fallback text for planning results.
- [x] Make answer validation planning-aware.

## Phase 4: Tests

- [x] Test EMI calculation.
- [x] Test purchase affordability verdict.
- [x] Test missing input handling for loan questions.
- [x] Test tax readiness does not fabricate advice.
- [x] Test assistant uses planning path for loan/purchase/rent questions.
- [x] Verify existing exact spend questions still pass.

## Phase 5: Build

- [x] Run unit tests.
- [x] Run debug build.
- [ ] Install on connected Android device when available.
