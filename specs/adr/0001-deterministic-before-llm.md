# ADR 0001: Deterministic Before LLM

## Status

Accepted

## Context

Financial transaction extraction requires correctness, privacy, low latency, and low battery usage. On-device LLM availability varies by device and runtime.

## Decision

SpendSense will use deterministic parsing before any LLM fallback.

```text
Notification
  -> classifier
  -> rules parser
  -> confidence evaluation
  -> LLM fallback only when needed and available
  -> validation
  -> repository
```

## Consequences

- Core tracking works on devices without Gemini Nano, LiteRT-LM, or similar runtimes.
- Most notifications avoid expensive model startup and inference.
- Rules, validators, and user corrections remain source of truth.
- More upfront engineering is required for parsers and test corpus management.
