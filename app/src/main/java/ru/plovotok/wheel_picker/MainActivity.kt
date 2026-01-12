package ru.plovotok.wheel_picker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.plovotok.wheel_picker.ui.components.wheel_picker.OverlayConfiguration
import ru.plovotok.wheel_picker.ui.components.wheel_picker.WheelPicker
import ru.plovotok.wheel_picker.ui.components.wheel_picker.rememberWheelPickerState
import ru.plovotok.wheel_picker.ui.theme.PickerSampleAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PickerSampleAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        val list = remember {
                            buildList {
                                repeat(10) {
                                    add("Item ${it + 1}")
                                }
                            }
                        }

                        val pickerState = rememberWheelPickerState(
                            infinite = true,
                            initialIndex = 4
                        )

                        val selectedIndex = pickerState.selectedItemIndex(list.size)
                        val debouncedIndex = selectedIndex.debounced(350)

                        Text(
                            text = "Selected: ${list[selectedIndex]}",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Debounced: ${list[debouncedIndex]}",
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        WheelPicker(
                            data = list,
                            state = pickerState,
                            overlay = OverlayConfiguration(
                                scrimColor = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                            ),
                            nonFocusedItems = 8,
                            contentAlignment = Alignment.CenterStart,
                            contentPadding = PaddingValues(horizontal = 20.dp),
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

                        val scope = rememberCoroutineScope()

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                scope.launch {
                                    pickerState.animateScrollToItem(4, list.size)
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(text = "Default")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun <T> T.debounced(
    delayMillis: Long = 500L,
    // 1. couroutine scope
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): T {
    // 2. updating state
    val state by rememberUpdatedState(this)

    var debouncedState by remember {
        mutableStateOf(this)
    }

    // 3. launching the side-effect handler
    DisposableEffect(state) {
        val job = coroutineScope.launch {
            delay(delayMillis)
            debouncedState = state
        }
        onDispose {
            job.cancel()
        }
    }
    return debouncedState
}