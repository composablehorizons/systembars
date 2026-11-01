# Compose System UI

Control system bar icon appearance from Compose Multiplatform.

```kotlin
import com.composables.systemui.SystemBarIcons
import com.composables.systemui.SystemBars

@Composable
fun App() {
    SystemBars(SystemBarIcons.Dark)
    // Your UI
}
```

A single argument applies to both bars. Name the arguments to style the bars separately:

```kotlin
SystemBars(statusBar = SystemBarIcons.Light, navigationBar = SystemBarIcons.Dark)
```

`Light` means light-colored icons; `Dark` means dark-colored icons. `Unspecified` has no opinion for that bar: the most recent request that specifies it wins, and when none does the platform setting stays as it is. On Android the composition must be hosted in an Activity. The library makes the navigation bar transparent, disables navigation-bar contrast enforcement where available, and restores previous settings when the effect leaves composition.

Android requests share an owner per window. Closing an outgoing screen or modal keeps any remaining appearance request active; the original icon, navigation-bar color and contrast settings return after the last request leaves. Native dialogs use their own window.

## iOS host

UIKit reads the status-bar style from a view controller. Host the app with the library's controller:

```kotlin
import com.composables.systemui.SystemUiViewController

fun MainViewController() = SystemUiViewController {
    App()
}
```

To use your own controller instead, create its state with `SystemBarsState(this)`, return `statusBarStyle` and provide the state to Compose. The state updates the controller's status bar when requests change:

```kotlin
class AppViewController : UIViewController(null, null) {
    private val systemBarsState = SystemBarsState(this)

    override fun preferredStatusBarStyle() = systemBarsState.statusBarStyle

    override fun viewDidLoad() {
        super.viewDidLoad()
        val compose = ComposeUIViewController {
            ProvideSystemBars(systemBarsState) { App() }
        }
        // Add compose as a child view controller.
    }
}
```

Without either host, `SystemBars` has no effect on iOS and logs a warning once. iOS applies the status-bar appearance; it does not customize navigation-bar icons. JVM and Wasm implementations are no-ops because they have no native mobile system bars.

## Custom hosts

`ProvideSystemBars` sends requests to a `SystemBarsState` instead of the platform, so a host can draw or apply them itself, for example in previews or tests:

```kotlin
val systemBarsState = remember { SystemBarsState() }
ProvideSystemBars(systemBarsState) {
    App()
}
FakeStatusBar(icons = systemBarsState.appearance.statusBar)
```

## Installation

Add Maven Central to your dependency repositories and add the library to `commonMain`:

```kotlin
repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.composables:system-ui:1.1.1")
        }
    }
}
```

## Build locally

Use JDK 17, Xcode for iOS, and Android SDK platform 37. From this directory:

```sh
./gradlew checkKotlinAbi jvmTest testAndroidHostTest iosSimulatorArm64Test publishToMavenLocal
```

Standalone builds include Android, iOS arm64/simulator arm64, JVM, and Wasm.

## Releasing

Run **Release** in GitHub Actions and enter a version such as `0.1.1`. You can also dispatch it from the CLI:

```sh
gh workflow run publish.yml --ref main --field version=0.1.1
```

The workflow checks out `main`, updates `package.yml` using KPM, promotes `CHANGELOG.md`'s Unreleased notes to the requested version when needed, and updates the README dependency. It runs tests and builds all platform publications before pushing the release commit and tag. Then `kpm publish` publishes signed artifacts to Maven Central. No local versioning, tag management, or release PR is needed.

You can write release notes under `## [Unreleased]`, or prepare a dated section for the requested version. When no notes are available, the workflow adds a link to the changes since the previous release tag. After upload succeeds, the workflow creates a draft GitHub release from those notes without waiting for Maven Central validation or download availability. Review and publish that draft separately. Dispatching an existing version retries publication from its original tag without creating another commit or changing the tag. Pushing a tag does not trigger publishing.

Publishing uses the `maven-central` environment and the organization secrets `OSSRH_USERNAME`, `OSSRH_PASSWORD`, `SIGNING_KEY_ID`, `SIGNING_PASSWORD`, and `SIGNING_KEY`.
