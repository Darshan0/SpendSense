# Bundled Model Slot

The bundled development model is Gemma 4 E2B, split into packageable asset chunks:

```text
app/src/main/assets/models/gemma-4-E2B-it.litertlm.part00
app/src/main/assets/models/gemma-4-E2B-it.litertlm.part01
app/src/main/assets/models/gemma-4-E2B-it.litertlm.part02
app/src/main/assets/models/gemma-4-E2B-it.litertlm.part03
app/src/main/assets/models/gemma-4-E2B-it.litertlm.part04
```

Model:

- Name: Gemma 4 E2B IT LiteRT-LM
- Source: `litert-community/gemma-4-E2B-it-litert-lm`
- Format: `.litertlm`
- Reconstructed runtime filename: `gemma-4-E2B-it.litertlm`
- Total size: about 2.59 GB
- License: Apache-2.0 per the model card/docs

The app is wired to look for these chunks, stream them into one app-private `.litertlm` file on first use, and route assistant requests to LiteRT-LM when Gemini Nano is unavailable.

Production note: this should move to Play Asset Delivery or a Wi-Fi model download flow before store release.
