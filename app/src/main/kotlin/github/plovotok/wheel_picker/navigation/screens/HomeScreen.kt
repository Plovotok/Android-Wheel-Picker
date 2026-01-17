package github.plovotok.wheel_picker.navigation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import github.plovotok.wheel_picker.navigation.BasicScreen
import github.plovotok.wheel_picker.navigation.Destination
import github.plovotok.wheel_picker.ui.components.icons.ForwardIcon

@Composable
fun HomeScreen(
    navAction: (Destination) -> Unit
) {
    BasicScreen(
        title = "Android Wheel Picker",
        isLarge = true
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = "Single Wheel Picker"
                )
            },
            trailingContent = {
                ForwardIcon()
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navAction(Destination.SinglePicker)
                }
        )
        ListItem(
            headlineContent = {
                Text(
                    text = "Multi Wheel Picker"
                )
            },
            trailingContent = {
                ForwardIcon()
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navAction(Destination.MultiPicker)
                }
        )
        ListItem(
            headlineContent = {
                Text(
                    text = "Date and time"
                )
            },
            trailingContent = {
                ForwardIcon()
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navAction(Destination.DateAndTime)
                }
        )
    }
}