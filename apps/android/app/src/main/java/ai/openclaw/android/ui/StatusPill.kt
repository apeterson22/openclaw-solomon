package ai.openclaw.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusPill(
  gateway: GatewayState,
  micPermissionGranted: Boolean,
  micActive: Boolean,
  talkEnabled: Boolean,
  talkListening: Boolean,
  talkSpeaking: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  activity: StatusActivity? = null,
  compact: Boolean = false,
) {
  Surface(
    onClick = onClick,
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = overlayContainerColor(),
    tonalElevation = 3.dp,
    shadowElevation = 0.dp,
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp), verticalAlignment = Alignment.CenterVertically) {
        StatusLaneDot(color = gateway.color, label = if (compact) gateway.shortTitle else gateway.title)

        val micLabel =
          when {
            !micPermissionGranted -> if (compact) "Mic off" else "Mic blocked"
            micActive -> if (compact) "Mic on" else "Mic live"
            else -> if (compact) "Mic idle" else "Mic idle"
          }
        StatusLaneIcon(
          icon = if (micPermissionGranted) Icons.Default.Mic else Icons.Default.MicOff,
          label = micLabel,
          tint = if (micActive) Color(0xFF2ECC71) else MaterialTheme.colorScheme.onSurfaceVariant,
        )

        val talkLabel =
          when {
            !talkEnabled -> if (compact) "Talk off" else "Talk off"
            talkSpeaking -> if (compact) "Speaking" else "Talk speaking"
            talkListening -> if (compact) "Listening" else "Talk listening"
            else -> if (compact) "Armed" else "Talk armed"
          }
        StatusLaneIcon(
          icon = if (talkSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.RecordVoiceOver,
          label = talkLabel,
          tint = if (talkEnabled) overlayIconColor() else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      if (!compact) activity?.let {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = activity.icon,
            contentDescription = activity.contentDescription,
            tint = activity.tint ?: overlayIconColor(),
            modifier = Modifier.size(16.dp),
          )
          Text(
            text = activity.title,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
          )
        }
      }
    }
  }
}

@Composable
private fun StatusLaneDot(color: Color, label: String) {
  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
    Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = color) {}
    Text(text = label, style = MaterialTheme.typography.labelMedium, maxLines = 1)
  }
}

@Composable
private fun StatusLaneIcon(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  tint: Color,
) {
  Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
    Text(text = label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
  }
}

data class StatusActivity(
  val title: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector,
  val contentDescription: String,
  val tint: Color? = null,
)

enum class GatewayState(val title: String, val shortTitle: String, val color: Color) {
  Connected("Gateway up", "Up", Color(0xFF2ECC71)),
  Connecting("Gateway…", "Conn", Color(0xFFF1C40F)),
  Error("Gateway err", "Err", Color(0xFFE74C3C)),
  Disconnected("Gateway off", "Off", Color(0xFF9E9E9E)),
}
