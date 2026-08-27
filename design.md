---
version: "alpha"
name: "SpendSense"
description: "A dark, advisor-grade mobile finance UI for recent spend intelligence, goals, export, and AI chat."
colors:
  primary: "#C2C1FF"
  primary-container: "#5E5CE6"
  tertiary: "#AAC7FF"
  background: "#131315"
  surface: "#1B1B1D"
  surface-high: "#2A2A2C"
  surface-highest: "#353437"
  outline: "#918FA0"
  on-surface: "#E4E2E4"
  on-surface-variant: "#C7C4D7"
  success: "#64D6A3"
  danger: "#FF6B6B"
  food: "#FFB86B"
  travel: "#79B8FF"
  lifestyle: "#E9A7FF"
  fixed-cost: "#FFD166"
  health: "#64D6A3"
typography:
  screen-title:
    fontFamily: "System"
    fontSize: "24px"
    fontWeight: "600"
    lineHeight: "30px"
    letterSpacing: "0px"
  section-title:
    fontFamily: "System"
    fontSize: "16px"
    fontWeight: "600"
    lineHeight: "22px"
    letterSpacing: "0px"
  card-title:
    fontFamily: "System"
    fontSize: "16px"
    fontWeight: "600"
    lineHeight: "22px"
    letterSpacing: "0px"
  body:
    fontFamily: "System"
    fontSize: "14px"
    fontWeight: "400"
    lineHeight: "20px"
    letterSpacing: "0px"
  label:
    fontFamily: "System"
    fontSize: "12px"
    fontWeight: "500"
    lineHeight: "16px"
    letterSpacing: "0px"
rounded:
  sm: "8px"
  md: "12px"
  lg: "16px"
  xl: "18px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "12px"
  lg: "16px"
  xl: "20px"
  screen-x: "20px"
components:
  app-background:
    backgroundColor: "{colors.background}"
    textColor: "{colors.on-surface}"
  card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.lg}"
    padding: "{spacing.lg}"
  elevated-card:
    backgroundColor: "{colors.surface-high}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.lg}"
    padding: "{spacing.lg}"
  advisor-warning-card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.lg}"
    padding: "{spacing.md}"
  metric-chip:
    backgroundColor: "{colors.surface-high}"
    textColor: "{colors.on-surface}"
    rounded: "{rounded.md}"
    padding: "{spacing.md}"
  primary-icon-button:
    backgroundColor: "{colors.primary-container}"
    textColor: "#FFFFFF"
    rounded: "{rounded.md}"
    size: "48px"
  bottom-navigation:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.on-surface-variant}"
    height: "80px"
---

## Overview

SpendSense should feel like a quiet financial advisor, not a banking dashboard overloaded with charts. The visual language is dark, compact, and information-dense, with clear hierarchy around what changed recently and what the user should do next.

The product is built for repeated daily checking. Screens should prioritize current spend pressure, goal impact, and actionable signals over broad monthly summaries. The UI should be calm enough for financial trust but direct enough to call out reckless spending, rising eating-out costs, tobacco-like merchants, weak investment momentum, and daily budget breaks.

## Colors

The palette is based on near-black surfaces, lavender-blue interaction color, and category accents that carry meaning.

- **Background (#131315):** app foundation. Use for full-screen areas.
- **Surface (#1B1B1D):** primary card surface. Use for repeated panels, chat messages, and grouped transaction blocks.
- **Surface High (#2A2A2C):** input fields, selected controls, and secondary cards.
- **Surface Highest (#353437):** progress tracks, inactive buttons, and low-emphasis containers.
- **Primary (#C2C1FF):** labels, selected navigation text, and subtle emphasis.
- **Primary Container (#5E5CE6):** selected navigation pills, active icon buttons, strong progress bars.
- **Tertiary (#AAC7FF):** secondary graph bars and cool supporting accents.
- **Danger (#FF6B6B):** overspending, rising harmful categories, weak investment warnings, and over-budget states.
- **Success (#64D6A3):** income, investment improvement, positive cash flow, and cooling discretionary spend.
- **Food (#FFB86B):** food, eating out, and grocery-related category marks.
- **Travel (#79B8FF):** fuel, transport, and travel category marks.
- **Lifestyle (#E9A7FF):** shopping, entertainment, and lifestyle category marks.
- **Fixed Cost (#FFD166):** rent, utilities, and unavoidable recurring obligations.

Do not let a single hue dominate the interface. The dark theme should be neutral, with category colors used as small semantic marks rather than large decorative fills.

## Typography

Use the platform system font. Typography should feel native to Android Compose and optimized for scan speed.

- Screen-level headlines use semibold weight and concise text.
- Section titles are small and direct, usually 1-4 words.
- Card titles should fit in one line whenever possible.
- Body copy should be short, plain, and action-oriented.
- Labels and metadata use subdued color, never tiny low-contrast text.
- Letter spacing is always `0px`; do not use condensed or negative tracking.

## Layout

Mobile screens use a single-column layout with `20px` horizontal padding and compact vertical rhythm. The main content should be visible immediately; avoid landing-page hero patterns.

Cards should be stacked with `14px` to `16px` vertical spacing. Use rows for dense comparisons such as Today vs Yesterday, Saved Salary vs Tracked Income, and current week vs previous week. Use progress bars only when they clarify proportion or trend; avoid adding graphs that do not change user behavior.

Home should start with the most important recent-spend answer. Insights should start with the advisor pulse and then show the few highest-priority actions. Profile should group identity, income, export, goals, settings, and account controls in that order.

## Elevation & Depth

The interface is mostly flat. Depth comes from tonal contrast, thin borders, and occasional restrained gradients.

- Primary cards use dark surfaces with low-alpha white borders.
- Advisor pulse cards may use a subtle two-stop gradient to draw attention.
- Avoid heavy drop shadows, glass blur, floating page sections, decorative blobs, and ornamental backgrounds.
- System overlays such as export share sheets and photo pickers should remain native.

## Shapes

Use restrained rounding:

- `8px` for compact tags and small controls.
- `12px` for category icons, metric chips, and icon buttons.
- `16px` for standard cards.
- `18px` for primary summary panels.
- Full circles only for profile avatars and progress-track clipping.

Do not use overly pill-shaped cards for major content. Repeated cards should feel practical and financial, not playful.

## Components

**Advisor Pulse Panel:** The first Insights card. It summarizes the strongest recent financial signal using one title, one short explanation, two metrics, and one progress bar. It should never become a generic analytics summary.

**Advisor Action Card:** A compact card with category icon, title, short body, metric, red or green arrow, and progress bar. Use no more than four on a screen.

**Category Movement Row:** Shows category, current 7-day spend, previous 7-day spend, trend direction, and a single progress bar. It is for movement, not static ranking.

**Transaction Group:** Transactions are grouped by relative date. Each row uses category icon, merchant, color-coded category tag, confidence, and amount. Avoid single-card-per-transaction layouts that waste vertical space.

**Chat Visual Card:** Chat responses should include a small structured visual card when answering finance questions. Long text-only answers should be avoided.

**Profile Header:** Includes avatar, name, photo action, editable name, and save action. It should feel like account setup, not a marketing surface.

**Income Profile:** Captures salary as a core planning input and compares saved salary with tracked income.

**Export Analysis:** Uses a download icon button and exports an Excel-compatible CSV with transaction and analysis fields.

**Bottom Navigation:** Five tabs: Home, History, AI, Insights, Profile. Selected tab uses the primary container fill with high-contrast icon treatment.

## Do's and Don'ts

Do:

- Lead with recent changes, not static monthly totals.
- Use category colors as compact semantic markers.
- Make every insight answer “what should I do next?”
- Keep financial language concrete and grounded in visible numbers.
- Keep Home operational, Insights advisory, Chat conversational, and Profile administrative.
- Keep export and settings discoverable in Profile.

Don't:

- Do not title sections “Local analytics” in user-facing UI.
- Do not show top categories without explaining action or trend.
- Do not add many charts just because data exists.
- Do not use text-only AI messages for finance answers when a visual metric card would help.
- Do not publish bundled local model binaries in the public repo.
- Do not hide goal management inside Home; Home may summarize goal impact, but Profile owns goal editing.

## Review Board

![SpendSense design review board](screenshots/design-review/spendsense_design_review_board.png)

## Captured Screens

| Screen | Scenario | File |
| --- | --- | --- |
| Home | Summary of recent spend and weekly category pressure | `screenshots/design-review/01_home_top.png` |
| Home | Daily trend, monthly context, grouped recent transactions | `screenshots/design-review/02_home_scrolled.png` |
| History | Transaction pipeline and current-day transactions | `screenshots/design-review/03_history_top.png` |
| History | Older grouped transaction history | `screenshots/design-review/04_history_scrolled.png` |
| AI Chat | Starting state with suggested questions | `screenshots/design-review/05_ai_chat_start.png` |
| AI Chat | Precomputed answer with visual finance card | `screenshots/design-review/06_ai_chat_answered.png` |
| Insights | Advisor summary and category movement | `screenshots/design-review/07_insights_top.png` |
| Insights | Prioritized advisor action cards | `screenshots/design-review/08_insights_scrolled.png` |
| Profile | Personal profile, salary, and export entry point | `screenshots/design-review/09_profile_top.png` |
| Profile | Goal creation, goal progress, and settings | `screenshots/design-review/10_profile_goals_settings.png` |
| Profile | Export share sheet for Excel-compatible CSV | `screenshots/design-review/11_profile_export_share_sheet.png` |
| Profile | Profile photo picker flow | `screenshots/design-review/12_profile_photo_picker.png` |
