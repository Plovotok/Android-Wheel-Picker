package github.plovotok.wheel_picker.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import github.plovotok.wheel_picker.navigation.screens.DateAndTimeScreen
import github.plovotok.wheel_picker.navigation.screens.HomeScreen
import github.plovotok.wheel_picker.navigation.screens.MultiPickerScreen
import github.plovotok.wheel_picker.navigation.screens.SinglePickerScreen

private fun <T: NavKey> NavBackStack<T>.addUnique(dst: T){
    if (last() != dst) {
        add(dst)
    }
}

@Composable
fun AppNavigation() {

    val backStack = rememberNavBackStack(Destination.Home)

    NavDisplay(
        backStack = backStack,
        onBack = backStack::removeLastOrNull,
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith fadeOut(targetAlpha = 0.7f) + slideOutHorizontally { -it * 3 / 4 }
        },
        popTransitionSpec = {
            fadeIn(initialAlpha = 0.7f) + slideInHorizontally { -it * 3 / 4 } togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            fadeIn(initialAlpha = 0.7f) + slideInHorizontally { -it * 3 / 4 } togetherWith
                    slideOutHorizontally(targetOffsetX = { it })

        },
        entryProvider = { key ->
            when (key) {
                is Destination.Home -> NavEntry(key) {
                    HomeScreen(navAction = { backStack.addUnique(it) })
                }

                is Destination.SinglePicker -> NavEntry(key) {
                    SinglePickerScreen(onBack = backStack::removeLastOrNull)
                }

                is Destination.MultiPicker -> NavEntry(key) {
                    MultiPickerScreen(onBack = backStack::removeLastOrNull)
                }

                is Destination.DateAndTime -> NavEntry(key) {
                    DateAndTimeScreen(onBack = backStack::removeLastOrNull)
                }

                else -> error("Unknown key $key")
            }
        },
        modifier = Modifier.background(Color.Black).fillMaxSize()
    )

}