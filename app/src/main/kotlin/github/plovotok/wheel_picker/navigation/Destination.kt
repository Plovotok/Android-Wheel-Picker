package github.plovotok.wheel_picker.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class Destination: NavKey {

    @Serializable
    data object Home: Destination()

    @Serializable
    data object SinglePicker: Destination()

    @Serializable
    data object MultiPicker: Destination()

    @Serializable
    data object DateAndTime: Destination()
}