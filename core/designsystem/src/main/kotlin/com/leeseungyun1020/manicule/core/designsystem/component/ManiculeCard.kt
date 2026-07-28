package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
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
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeBorder
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.maniculeColors

@Composable
fun ManiculeCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        content = content,
    )
}

@Composable
fun ManiculeDashedCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.maniculeColors.dashedBorder,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .drawBehind {
                    val dashOn = ManiculeBorder.dashOn.toPx()
                    val dashOff = ManiculeBorder.dashOff.toPx()
                    val dash = PathEffect.dashPathEffect(floatArrayOf(dashOn, dashOff), 0f)
                    drawRoundRect(
                        color = borderColor,
                        style = Stroke(width = ManiculeBorder.dashed.toPx(), pathEffect = dash),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                    )
                }.background(Color.Transparent, MaterialTheme.shapes.large),
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
