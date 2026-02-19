package ai.openclaw.android.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TalkOrbOverlay(
  seamColor: Color,
  statusText: String,
  isListening: Boolean,
  isSpeaking: Boolean,
  micPowerDb: Float,
  talkVoiceOutputEnabled: Boolean,
  modifier: Modifier = Modifier,
) {

  val trimmed = statusText.trim()
  val showStatus = trimmed.isNotEmpty() && trimmed != "Off"
  val phase =
    when {
      isSpeaking -> "Speaking"
      isListening -> "Listening"
      else -> "Thinking"
    }

  val targetAmplitude = ((micPowerDb + 60f) / 60f).coerceIn(0f, 1f)
  val smoothAmplitude = remember { Animatable(0f) }
  LaunchedEffect(targetAmplitude) {
    val duration = if (targetAmplitude > smoothAmplitude.value) 90 else 220
    smoothAmplitude.animateTo(targetAmplitude, animationSpec = tween(duration))
  }

  Column(
    modifier = modifier.padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    Box(contentAlignment = Alignment.Center) {
      val amplitude = smoothAmplitude.value
      val infinite = rememberInfiniteTransition(label = "electric-pulse")
      val pulse by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1400), repeatMode = RepeatMode.Restart),
        label = "pulse",
      )

      Canvas(modifier = Modifier.size(360.dp)) {
        val center = this.center
        val maxRadius = size.minDimension / 2
        val strokeWidth = 2.6.dp.toPx()

        val baseAlpha = 0.10f + (pulse * 0.12f)
        val baseScale = 0.86f + (pulse * 0.14f)

        drawCircle(
          color = seamColor.copy(alpha = baseAlpha),
          radius = maxRadius * baseScale,
          center = center,
          style = Stroke(width = strokeWidth),
        )

        // Electric spikes synced to mic amplitude
        val spikes = 48
        val spikeRadius = maxRadius * (0.62f + amplitude * 0.18f)
        val spikeAmp = maxRadius * (0.05f + amplitude * 0.18f)
        for (i in 0 until spikes) {
          val theta = (i / spikes.toFloat()) * (2f * Math.PI).toFloat() + pulse * 1.8f
          val jitter = sin(theta * 6f + pulse * 10f) * 0.35f
          val inner = spikeRadius * (0.85f + jitter * 0.08f)
          val outer = inner + spikeAmp * (0.8f + sin(theta * 3f + pulse * 4f) * 0.4f)
          val x1 = center.x + cos(theta) * inner
          val y1 = center.y + sin(theta) * inner
          val x2 = center.x + cos(theta) * outer
          val y2 = center.y + sin(theta) * outer
          val alpha = (0.25f + amplitude * 0.6f).coerceIn(0.15f, 0.9f)
          drawLine(
            color = seamColor.copy(alpha = alpha),
            start = androidx.compose.ui.geometry.Offset(x1, y1),
            end = androidx.compose.ui.geometry.Offset(x2, y2),
            strokeWidth = strokeWidth,
          )
        }

        // Dual arc rings
        repeat(3) { idx ->
          val phase = (pulse + idx * 0.2f) % 1f
          val r = maxRadius * (0.55f + phase * (0.35f + idx * 0.08f))
          val a = (1f - phase) * (0.22f - idx * 0.04f) + amplitude * 0.22f
          drawArc(
            color = seamColor.copy(alpha = a.coerceAtLeast(0f)),
            startAngle = (pulse * 360f + idx * 42f) % 360f,
            sweepAngle = 110f + amplitude * 80f,
            useCenter = false,
            topLeft = center - androidx.compose.ui.geometry.Offset(r, r),
            size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
            style = Stroke(width = strokeWidth + amplitude * 2f),
          )
        }

        // Core glow
        drawCircle(
          brush =
            Brush.radialGradient(
              colors =
                listOf(
                  seamColor.copy(alpha = 0.15f + amplitude * 0.75f),
                  seamColor.copy(alpha = 0.08f + amplitude * 0.25f),
                  Color.Black.copy(alpha = 0.2f),
                ),
              center = center,
              radius = maxRadius * 0.45f * (1f + amplitude * 0.35f),
            ),
          radius = maxRadius * 0.32f,
          center = center,
        )
      }
      if (!talkVoiceOutputEnabled || showStatus) {
        Surface(
          color = Color.Black.copy(alpha = 0.64f),
          shape = CircleShape,
        ) {
          Text(
            text = if (!talkVoiceOutputEnabled) "Voice output disabled" else trimmed,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = Color.White.copy(alpha = 0.92f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
          )
        }
      } else {
        Text(
          text = phase,
          color = Color.White.copy(alpha = 0.80f),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}
