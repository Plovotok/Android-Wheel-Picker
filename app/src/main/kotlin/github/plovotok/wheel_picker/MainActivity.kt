package github.plovotok.wheel_picker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import github.plovotok.wheel_picker.navigation.AppNavigation
import github.plovotok.wheel_picker.ui.theme.PickerSampleAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PickerSampleAppTheme {
                AppNavigation()
            }
        }
    }
}