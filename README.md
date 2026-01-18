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

## Implementation
Implementation steps are described in my [article](https://habr.com/ru/articles/986270/).

## Latest Release
[![Maven Central](https://img.shields.io/maven-central/v/io.github.plovotok/android-wheel-picker?color=green)](https://search.maven.org/search?q=g:io.github.plovotok)

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

## Examples

### Single picker

```kotlin
val list = remember {
    buildList {
        repeat(10) {
            add("Item ${it + 1}")
        }
    }
}
val pickerState = rememberWheelPickerState(
    initialIndex = 4,
    infinite = false
)

WheelPicker(
    data = list,
    state = pickerState,
    overlay = OverlayConfiguration(
        scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
    ),
    itemContent = {
        Text(
            text = list[it],
            fontSize = 18.sp,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                textMotion = TextMotion.Animated
            ),
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
)
```

### Multi picker

```kotlin
val list = buildList {
            repeat(10) {
                add("Item ${(it + 1)}")
            }
        }
        val state1 = rememberWheelPickerState(2)
        val state2 = rememberWheelPickerState(3)
        val state3 = rememberWheelPickerState(4)

MultiWheelPicker(
    wheelCount = 3,
    state = {
        when (it) {
            0 -> state1
            1 -> state2
            else -> state3
        }
    },
    overlay = OverlayConfiguration(
        scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
    ),
    itemHeightDp = 38.dp,
    data = { list },
    itemContent = { _, listIdex ->
        Text(
            text = list[listIdex],
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp
        )
    },
)
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
