# SpendSense Spec-Driven Development

This directory is the source of truth for turning `trd.md` into implementation work.

## Spec Set

- `product.md` defines the product goals, MVP scope, non-goals, and acceptance criteria.
- `architecture.md` defines the Android-first architecture and module boundaries.
- `data-model.md` defines core domain entities and persistence rules.
- `security-privacy.md` defines privacy, retention, logging, and AI safety constraints.
- `000-mvp-vertical-slice/` is the first executable implementation spec.

## Workflow

1. Start from a numbered spec directory, beginning with `000-mvp-vertical-slice`.
2. Keep each spec split into:
   - `requirements.md`: what must be true from the user's point of view.
   - `design.md`: how the system will satisfy the requirements.
   - `tasks.md`: ordered implementation tasks with verification steps.
3. Update specs before code when scope changes.
4. Mark tasks complete only after code and verification are both done.

## Initial Implementation Focus

The first coding pass should stop at an offline, deterministic vertical slice:

```text
Notification / sample input
  -> sanitization
  -> sensitive-message filter
  -> heuristic financial classifier
  -> generic deterministic parser
  -> Room repository
  -> Compose transaction list
```

No LLM integration belongs in the first slice. The LLM interface can exist as a boundary, but the implementation should be `NoOpLanguageModel` until the deterministic pipeline is working.
