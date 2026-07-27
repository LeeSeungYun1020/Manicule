package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme

@Composable
fun ManiculeCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        content = content,
    )
}

@Composable
fun ManiculeDashedCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .drawBehind {
                val dash = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = dash),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                )
            }.background(Color.Transparent, RoundedCornerShape(16.dp)),
        content = content,
    )
}

@ManiculePreview
@Composable
private fun ManiculeCardPreview() {
    ManiculeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ManiculeCard {
                Text(
                    text = "일반 카드",
                    modifier = Modifier.padding(16.dp),
                )
            }
            ManiculeDashedCard {
                Text(
                    text = "점선 카드",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
