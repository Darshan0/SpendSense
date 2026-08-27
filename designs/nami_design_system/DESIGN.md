---
name: Nami Design System
colors:
  surface: '#131315'
  surface-dim: '#131315'
  surface-bright: '#39393b'
  surface-container-lowest: '#0e0e10'
  surface-container-low: '#1b1b1d'
  surface-container: '#1f1f21'
  surface-container-high: '#2a2a2c'
  surface-container-highest: '#353437'
  on-surface: '#e4e2e4'
  on-surface-variant: '#c7c4d7'
  inverse-surface: '#e4e2e4'
  inverse-on-surface: '#303032'
  outline: '#918fa0'
  outline-variant: '#464554'
  surface-tint: '#c2c1ff'
  primary: '#c2c1ff'
  on-primary: '#1800a7'
  primary-container: '#5e5ce6'
  on-primary-container: '#f4f1ff'
  inverse-primary: '#4d4ad5'
  secondary: '#c8c6c8'
  on-secondary: '#303032'
  secondary-container: '#474649'
  on-secondary-container: '#b6b4b7'
  tertiary: '#aac7ff'
  on-tertiary: '#003064'
  tertiary-container: '#006dd6'
  on-tertiary-container: '#f0f3ff'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#e2dfff'
  primary-fixed-dim: '#c2c1ff'
  on-primary-fixed: '#0c006b'
  on-primary-fixed-variant: '#332dbc'
  secondary-fixed: '#e4e2e4'
  secondary-fixed-dim: '#c8c6c8'
  on-secondary-fixed: '#1b1b1d'
  on-secondary-fixed-variant: '#474649'
  tertiary-fixed: '#d6e3ff'
  tertiary-fixed-dim: '#aac7ff'
  on-tertiary-fixed: '#001b3e'
  on-tertiary-fixed-variant: '#00468d'
  background: '#131315'
  on-background: '#e4e2e4'
  surface-variant: '#353437'
typography:
  display-lg:
    fontFamily: Hanken Grotesk
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
    letterSpacing: -0.01em
  headline-lg-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  title-md:
    fontFamily: Hanken Grotesk
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-sm:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  container-padding: 20px
  stack-gap-sm: 8px
  stack-gap-md: 16px
  stack-gap-lg: 24px
  grid-gutter: 12px
---

## Brand & Style

The design system is anchored in the persona of a "Silent Guardian"—an AI-powered financial assistant that is intelligent, calm, and hyper-private. The aesthetic marries the structured utility of **Material 3** with the high-end, editorial precision of **CRED** and the functional minimalism of **Notion AI**.

The visual language follows a **Modern-Corporate** path with **Glassmorphic** accents. It prioritizes clarity and high-signal data visualization over decorative clutter. The emotional response should be one of "financial zen": the user feels in control, secure, and unburdened by the complexity of their data. On-device AI processing is signaled through subtle shimmer effects and soft, breathing transitions rather than aggressive "robot" motifs.

## Colors

The palette is rooted in deep, sophisticated tones to evoke a sense of privacy and "bank-grade" security. 

- **Primary (Indigo):** A vibrant but refined indigo (`#5E5CE6`) serves as the primary action color, used for key CTAs and AI-active states.
- **Surface & Background:** We utilize a "Pure Black" (`#000000`) background for OLED efficiency and premium contrast, with surfaces stepping up to "Deep Charcoal" (`#1C1C1E`).
- **Accent (Teal/Blue):** Tertiary accents (`#0A84FF`) are reserved for positive financial trends or informational highlights.
- **Neutral:** Off-whites (`#F2F2F7`) are used for primary text to reduce harsh contrast and eye strain.

## Typography

This design system uses a tri-font strategy to balance character with technical precision. 

1. **Hanken Grotesk** is the voice of the brand, used for large headings and financial totals. Its sharp, contemporary geometry feels premium and modern.
2. **Inter** handles the heavy lifting of body text and transactional data, ensuring maximum readability at small sizes.
3. **Geist** is used sparingly for labels, metadata, and "AI-generated" insights, providing a subtle "developer-tool" aesthetic that reinforces the intelligence of the platform.

Text should follow a strict hierarchy: use weight (Medium to Bold) rather than size to differentiate importance in tight mobile layouts.

## Layout & Spacing

Following the **Fluid Grid** model optimized for Android, the layout uses a 4-column structure for mobile. 

- **Safe Zones:** A standard 20px horizontal margin is applied to the main viewport.
- **Rhythm:** An 8px base grid governs all spatial relationships. 
- **Density:** The design is purposefully "airy." Avoid cramming multiple cards into the viewport; allow vertical scrolling to provide focus on one financial insight at a time.
- **Adaptation:** On larger foldable devices, the 4-column grid expands to a 12-column layout, moving navigation to a side-rail rather than a bottom bar.

## Elevation & Depth

Hierarchy is established through **Tonal Layers** and **Subtle Glassmorphism** rather than traditional heavy shadows.

- **Level 0 (Background):** Pure black `#000000`.
- **Level 1 (Cards):** Deep charcoal `#1C1C1E`.
- **Level 2 (Modals/Overlays):** A semi-transparent blur (Backdrop Filter: 20px) with a 1px inner stroke of white at 10% opacity.
- **Shadows:** Only used on the primary action button and floating "AI Insight" chips. Shadows are ultra-diffused: `0px 10px 30px rgba(0, 0, 0, 0.5)`.

## Shapes

The shape language is defined by **Pill-shaped** and extremely rounded containers. 

- **Primary Cards:** Use a 24px corner radius to create a soft, friendly container for complex data.
- **Buttons:** Fully rounded (pill) for primary actions; 16px radius for secondary.
- **Inputs:** 12px radius to maintain a distinction between clickable cards and interactive fields.
- **Icons:** Use a rounded corner family (e.g., Material Symbols Rounded) to match the container language.

## Components

- **Buttons:** Primary buttons are high-contrast Indigo with white text. Secondary buttons use a tonal "Surface + 1" approach with no border.
- **Cards:** Financial cards (spending, balance) should have no external border. Instead, use a subtle 1px top-light highlight to give them a "milled" look.
- **AI Insights:** Represented as a special card type with a very subtle mesh-gradient border (Indigo to Teal) and a Geist-font label.
- **Inputs:** Focused states should use a 2px Indigo border with no outer glow. Background should be slightly darker than the card surface.
- **Bottom Bar:** A glassmorphic bar with centered primary action. Active states are indicated by a small dot underneath the icon, avoiding the heavy "pill" indicator of standard Material 3 to keep it more elegant.
- **Progress Bars:** Thin (4px), rounded caps, using the Primary color for progress and Surface 2 for the track.