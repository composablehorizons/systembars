# Changelog

## [Unreleased]

The library is now published as `com.composables:systembars` in the `com.composables.systembars` package. Earlier releases below were published as `com.composables:system-ui`.

### Added

- `SystemBars(all)` applies the same content to the status and navigation bars.
- `SystemBarsState(viewController)` and `SystemBarsState.statusBarStyle` let your own iOS view controller apply system bar requests.

### Changed

- Rename `SystemBarIcons` to `SystemBarContent`, and `SystemBarIcons.Default` to `SystemBarContent.Unspecified`.
- Rename `SystemUiState` to `SystemBarsState`, `ProvideSystemUi` to `ProvideSystemBars`, and `SystemUiViewController` to `SystemBarsViewController`.
- `SystemBarAppearance` is a regular class instead of a data class.
- `SystemBars` on iOS without a host has no effect and logs a warning instead of throwing.
- `SystemBars` on Android without an Activity or dialog window, such as in Android Studio previews, has no effect instead of throwing.
- Leave the deprecated navigation bar color alone where Android enforces edge-to-edge and ignores it.
- `SystemBarsState.appearance` resolves each bar from the most recent request that specifies it, matching Android. An overlay that leaves a bar unspecified no longer resets it on iOS or in custom hosts.

## [1.1.1] - 2026-10-21

- [Changes since v0.1.0](https://github.com/composablehorizons/systembars/compare/v0.1.0...v1.1.1).

## [0.1.1] - 2026-10-21

### Fixed

- Keep Android system bar icons correct when outgoing screens leave during navigation, and restore the original appearance after the last override closes.
- Apply Android dialog icon changes to the dialog window without changing the screen underneath.

## [0.1.0] - 2026-10-16

### Added

- Control status-bar and navigation-bar icon appearance on Android.
- Control iOS status-bar appearance with `SystemUiViewController`.
- Expose appearance state for custom hosts and restore underlying requests when overrides leave composition.
- Support JVM and Wasm with no-op native system-bar implementations.
