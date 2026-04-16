package com.example.myappmobile.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myappmobile.R
import com.example.myappmobile.core.theme.FloraDivider
import com.example.myappmobile.core.theme.FloraText
import com.example.myappmobile.core.theme.FloraTextMuted
import com.example.myappmobile.core.theme.FloraWhite

@Composable
fun SocialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    iconTint: Color = FloraText,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, FloraDivider),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = FloraWhite.copy(alpha = 0.92f),
            contentColor = FloraText,
            disabledContainerColor = FloraWhite.copy(alpha = 0.6f),
            disabledContentColor = FloraTextMuted,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            when {
                icon != null -> Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = iconTint,
                )

                else -> Icon(
                    painter = iconPainter ?: painterResource(id = R.drawable.flora_logo_vectorized),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Unspecified,
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleSmall.copy(color = FloraText),
            )
        }
    }
}
