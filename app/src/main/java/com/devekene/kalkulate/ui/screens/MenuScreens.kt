package com.devekene.kalkulate.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.sharp.OpenInNew
import androidx.compose.material.icons.sharp.Code
import androidx.compose.material.icons.sharp.DeleteSweep
import androidx.compose.material.icons.sharp.Groups
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material.icons.sharp.Language
import androidx.compose.material.icons.sharp.Person
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material.icons.sharp.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.devekene.kalkulate.BuildConfig
import com.devekene.kalkulate.R
import com.devekene.kalkulate.data.UserPreferences
import com.devekene.kalkulate.math.AngleMode
import com.devekene.kalkulate.ui.theme.JetBrainsMonoFontFamily
import kotlin.math.roundToInt

enum class MenuDestination(val title: String) {
  DISPLAY_PRECISION("Display & Precision"),
  ABOUT_DEVELOPER("About Developer"),
  SETTINGS("Settings"),
  COMMUNITY("Join our Community"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuModalSheet(
  onDismiss: () -> Unit,
  onNavigate: (MenuDestination) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = "Kalkulate Menu",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "Choose a section to configure or explore",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(2.dp))

      MenuActionTile(
        title = "Display and Precision",
        subtitle = "Default angle unit, decimal digits & haptics",
        icon = Icons.Sharp.Tune,
        testTag = "menu_item_display_precision",
        onClick = {
          onDismiss()
          onNavigate(MenuDestination.DISPLAY_PRECISION)
        },
      )

      MenuActionTile(
        title = "About Developer",
        subtitle = "Built by Ekene • Contact & portfolio",
        icon = Icons.Sharp.Person,
        testTag = "menu_item_about_developer",
        onClick = {
          onDismiss()
          onNavigate(MenuDestination.ABOUT_DEVELOPER)
        },
      )

      MenuActionTile(
        title = "Settings",
        subtitle = "Startup mode, grapher angle & data controls",
        icon = Icons.Sharp.Settings,
        testTag = "menu_item_settings",
        onClick = {
          onDismiss()
          onNavigate(MenuDestination.SETTINGS)
        },
      )

      MenuActionTile(
        title = "Join our Community",
        subtitle = "Join us on System One",
        icon = Icons.Sharp.Groups,
        testTag = "menu_item_community",
        onClick = {
          onDismiss()
          onNavigate(MenuDestination.COMMUNITY)
        },
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun MenuActionTile(
  title: String,
  subtitle: String,
  icon: ImageVector,
  testTag: String,
  onClick: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surfaceContainerLow,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(testTag),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(42.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
          )
        }
      }

      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Sharp.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun SubScreenTopBar(
  title: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier.size(48.dp).testTag("subscreen_back_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onBackground,
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onBackground,
      )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
  }
}

@Composable
fun DisplayAndPrecisionScreen(
  preferences: UserPreferences,
  onSelectAngleMode: (AngleMode) -> Unit,
  onPrecisionChange: (Int) -> Unit,
  onHapticChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    Text(
      text = stringResource(R.string.settings_title),
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )

    // Angle Mode selector
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "Default Angle Unit",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          AngleMode.entries.forEach { mode ->
            FilterChip(
              selected = preferences.angleMode == mode,
              onClick = { onSelectAngleMode(mode) },
              label = { Text("${mode.shortLabel} (${mode.fullName})") },
              modifier = Modifier.testTag("settings_angle_${mode.name}"),
            )
          }
        }
      }
    }

    // Decimal Precision Slider
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Decimal Precision",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${preferences.decimalPrecision} digits",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        Slider(
          value = preferences.decimalPrecision.toFloat(),
          onValueChange = { onPrecisionChange(it.roundToInt()) },
          valueRange = 2f..12f,
          steps = 9,
          modifier = Modifier.testTag("precision_slider"),
        )
      }
    }

    // Haptic Feedback Switch
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Key Haptic Feedback",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Subtle tactile vibration when tapping keys",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Switch(
          checked = preferences.hapticEnabled,
          onCheckedChange = onHapticChange,
          modifier = Modifier.testTag("haptic_switch"),
        )
      }
    }
  }
}

@Composable
fun AboutDeveloperScreen(modifier: Modifier = Modifier) {
  val uriHandler = LocalUriHandler.current
  val contactUrl = "https://ekenedilichukwu.com/contact"

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(22.dp),
      color = MaterialTheme.colorScheme.surfaceContainer,
      border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "E",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
              )
            }
          }
          Column {
            Text(
              text = "Ekene",
              style = MaterialTheme.typography.headlineSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Creator & Android Developer",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }

        Text(
          text =
            "Ekene built Kalkulate to provide a fast, accurate, and thoughtfully crafted mobile scientific calculator with advanced trigonometry, function plotting, unit conversion, and persistent calculation history.",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Sharp.Language,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = "Get in Touch",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
        Text(
          text = contactUrl,
          fontFamily = JetBrainsMonoFontFamily,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.primary,
        )
        Button(
          onClick = { uriHandler.openUri(contactUrl) },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("developer_contact_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Sharp.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Visit Contact Page")
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Icon(
          imageVector = Icons.Sharp.Code,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Column {
          Text(
            text = "Kalkulate",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Package: ${BuildConfig.APPLICATION_ID} • v${BuildConfig.VERSION_NAME}",
            fontFamily = JetBrainsMonoFontFamily,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun AppSettingsScreen(
  preferences: UserPreferences,
  graphAngleMode: AngleMode,
  historyCount: Int,
  onSetScientificMode: (Boolean) -> Unit,
  onToggleGraphAngleMode: () -> Unit,
  onClearMemory: () -> Unit,
  onClearHistory: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showClearDialog by remember { mutableStateOf(false) }

  if (showClearDialog) {
    AlertDialog(
      onDismissRequest = { showClearDialog = false },
      title = { Text(stringResource(R.string.history_clear_confirm_title)) },
      text = { Text(stringResource(R.string.history_clear_confirm_body)) },
      confirmButton = {
        TextButton(
          onClick = {
            onClearHistory()
            showClearDialog = false
          }
        ) {
          Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.error)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearDialog = false }) {
          Text(stringResource(R.string.cancel))
        }
      },
    )
  }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Scientific Keypad Mode",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Keep advanced trigonometric & log functions expanded",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Switch(
          checked = preferences.isScientificMode,
          onCheckedChange = onSetScientificMode,
          modifier = Modifier.testTag("settings_scientific_switch"),
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Function Grapher Angle Unit",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Currently plotting trig curves in ${graphAngleMode.fullName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        FilterChip(
          selected = true,
          onClick = onToggleGraphAngleMode,
          label = { Text(graphAngleMode.shortLabel) },
          modifier = Modifier.testTag("settings_graph_angle_chip"),
        )
      }
    }

    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "Data & Registers",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Memory Register (M)",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text =
                if (preferences.memoryValue == 0.0) "Empty (0)"
                else "Stored: ${preferences.memoryValue}",
              fontFamily = JetBrainsMonoFontFamily,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          OutlinedButton(
            onClick = onClearMemory,
            enabled = preferences.memoryValue != 0.0,
            shape = RoundedCornerShape(12.dp),
          ) {
            Text("Reset M")
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Calculation History",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "$historyCount saved item${if (historyCount == 1) "" else "s"} on device",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          OutlinedButton(
            onClick = { showClearDialog = true },
            enabled = historyCount > 0,
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(
              imageVector = Icons.Sharp.DeleteSweep,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Clear")
          }
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(18.dp),
      color = MaterialTheme.colorScheme.surfaceContainer,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Sharp.Info,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = "All calculations, preferences, and history remain stored privately on your device.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
fun CommunityScreen(modifier: Modifier = Modifier) {
  var isLoginMode by remember { mutableStateOf(true) }
  var nameInput by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }

  Column(
    modifier =
      modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 18.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surfaceContainer,
      border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(48.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Sharp.Groups,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(26.dp),
            )
          }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = "Join us on System One",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "Connect with fellow Kalkulate users, share formulas, and explore updates.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    // Toggle between Log In and Sign Up
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        FilterChip(
          selected = isLoginMode,
          onClick = { isLoginMode = true },
          label = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
              Text("Log In", fontWeight = FontWeight.SemiBold)
            }
          },
          modifier = Modifier.weight(1f).height(48.dp).testTag("community_toggle_login"),
        )
        FilterChip(
          selected = !isLoginMode,
          onClick = { isLoginMode = false },
          label = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
              Text("Sign Up", fontWeight = FontWeight.SemiBold)
            }
          },
          modifier = Modifier.weight(1f).height(48.dp).testTag("community_toggle_signup"),
        )
      }
    }

    // Form Card
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.surfaceContainerLow,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text =
            if (isLoginMode) "Log in to System One" else "Create a System One account",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )

        if (!isLoginMode) {
          OutlinedTextField(
            value = nameInput,
            onValueChange = { nameInput = it },
            label = { Text("Full Name") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("community_name_input"),
          )
        }

        OutlinedTextField(
          value = emailInput,
          onValueChange = { emailInput = it },
          label = { Text("Email Address") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("community_email_input"),
        )

        OutlinedTextField(
          value = passwordInput,
          onValueChange = { passwordInput = it },
          label = { Text("Password") },
          singleLine = true,
          visualTransformation = PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().testTag("community_password_input"),
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (isLoginMode) {
          Button(
            onClick = {},
            enabled = false,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("community_login_button"),
          ) {
            Text("Log In (Coming Soon)")
          }
        } else {
          Button(
            onClick = {},
            enabled = false,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("community_signup_button"),
          ) {
            Text("Sign Up (Coming Soon)")
          }
        }
      }
    }
  }
}
