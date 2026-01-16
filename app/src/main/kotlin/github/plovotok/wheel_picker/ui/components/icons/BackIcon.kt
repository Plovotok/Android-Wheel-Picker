package github.plovotok.wheel_picker.ui.components.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import github.plovotok.wheel_picker.R

@Composable
fun BackIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.arrow_back),
        contentDescription = "Back icon",
        modifier = modifier.size(24.dp)
    )
}