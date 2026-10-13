# Silent Code Mode

One-click toggle for automatic code editor popups in JetBrains IDEs.

## Supported target

IntelliJ Platform 2025.1+ (`since-build=251`), with planned Rider, WebStorm and PyCharm support. **Compatibility is not certified until CI succeeds.** The IntelliJ-side toggle does not disable ReSharper or JetBrains AI Assistant features.

## CI

- `.github/workflows/ci.yml`: on push/PR/manual run, compiles, runs checks, builds installable ZIP, runs JetBrains Plugin Verifier on Rider/WebStorm/PyCharm 2025.1, 2025.3 and latest stable.
- `.github/workflows/publish.yml`: on GitHub Release or manual run, checks and verifies before signing and uploading to Marketplace.
- Both workflows require network access and can download several gigabytes of IDEs.

## Setup

1. Create a GitHub repository and push the **contents of this directory** to its `main` branch.
2. Open GitHub → Actions → Build and verify → Run workflow.
3. After a successful run, download artifact `silent-code-mode-installable`, unpack that GitHub artifact and install the contained ZIP in IDE Settings → Plugins → gear → Install Plugin from Disk.
4. Test manually in Rider, WebStorm, PyCharm on Windows, including toggling, restarting, and restoring previous settings.
5. Create a developer profile at JetBrains Marketplace, choose a unique plugin ID/vendor, add a proper icon, screenshots, detailed description and support link, and submit the **first plugin version manually** for approval.
6. Create plugin signing certificate/private key and Marketplace publishing token. In GitHub repository Settings → Environments create `marketplace`, enable required reviewers, and add Actions secrets `PUBLISH_TOKEN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, `CERTIFICATE_CHAIN`.
7. After first version has been accepted, publish subsequent versions with GitHub Releases. Update `version` in `build.gradle.kts` before release.

## Security

Never commit keys or tokens. Publish workflow uses protected `marketplace` environment. Verify that the first Marketplace upload and plugin signing certificate match future releases.

## Caveats

- This archive contains CI/publishing configuration, **not** a compiled plugin. Build could not be run in the authoring environment (Gradle/JetBrains dependencies not downloadable there).
- Gradle verification checks binary API compatibility, not actual behavior in every IDE.
- The project was inherited from a prototype; review and improve its functionality before Marketplace submission.
- If verification reports incompatible APIs, fix them before publishing. Do not disable the verifier.
