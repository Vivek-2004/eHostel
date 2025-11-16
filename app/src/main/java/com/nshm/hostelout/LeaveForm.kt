package com.nshm.hostelout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LeaveForm() {
    var from = remember { mutableStateOf("") }
    var to = remember { mutableStateOf("") }
    Column(Modifier
        .fillMaxSize()
        .padding(14.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Leave Form",
                fontSize = 20.sp,
                fontStyle = FontStyle.Italic
            )
        }
        Spacer(Modifier.height(4.dp))
        Row {
            Row(Modifier
                .fillMaxWidth()
                .weight(1F)
                .padding(5.dp)) {
                OutlinedTextField(
                    label = { Text("From") },
                    value = from.value,
                    onValueChange = { from.value = it })
            }
            Row(Modifier
                .fillMaxWidth()
                .weight(1F)
                .padding(5.dp)) {
                OutlinedTextField(
                    label = { Text("To") },
                    value = to.value,
                    onValueChange = { to.value = it })
            }
        }
    }
}