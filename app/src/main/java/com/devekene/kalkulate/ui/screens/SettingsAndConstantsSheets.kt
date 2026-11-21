package com.devekene.kalkulate.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devekene.kalkulate.R
import com.devekene.kalkulate.math.UnitConverterEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstantsBottomSheet(
  onDismiss: () -> Unit,
  onInsertConstant: (String) -> Unit,
  onCopyConstant: (String) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = stringResource(R.string.constants_title),
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = "Tap any constant to insert it into your calculation.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      UnitConverterEngine.physicalConstants.forEach { constant ->
        ConstantRowCard(
          constant = constant,
          onInsert = {
            onInsertConstant(constant.insertToken)
            onDismiss()
          },
          onCopy = { onCopyConstant(constant.insertToken) },
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
