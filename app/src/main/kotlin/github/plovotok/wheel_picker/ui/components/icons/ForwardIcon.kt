package github.plovotok.wheel_picker.ui.components.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import github.plovotok.wheel_picker.R

@Composable
fun ForwardIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = ImageVector.vectorResource(R.drawable.arrow_forward),
        contentDescription = "Forward icon",
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(24.dp)
    )
}