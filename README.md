# Android WheelPicker
Example of an iOS implementation UIPickerView in JetpackCompose

## Preview
<img src="/assets/preview_screenshot.png" width="300" /> <img src="/assets/preview.gif" width="300" /> <img src="/assets/date_time.gif" width="300" />


## Features
- iOS like behavior - smooth scrolling with inertia and snapping the selected element
- Customization - colors, visible items
- Infinite list support
- Observable state
- Programmatically selectable index

## Latest Release
[![Latest Release](https://maven-badges.sml.io/sonatype-central/io.github.plovotok/android-wheel-picker/badge.svg?subject=Latest%20Release&color=blue)](https://maven-badges.sml.io/sonatype-central/io.github.plovotok/android-wheel-picker/)

## Usage
`libs.versions.toml` file:
```toml
[versions]
#...
wheel = "$latest"

[libraries]
#...
wheel-picker = { module = "io.github.plovotok:android-wheel-picker", version.ref = "wheel" }
```

`build.gradle.kts` file:
```kotlin
dependencies {
    implementation(libs.wheel.picker)
}
```


## License

```
Copyright 2026 Plovotok

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
