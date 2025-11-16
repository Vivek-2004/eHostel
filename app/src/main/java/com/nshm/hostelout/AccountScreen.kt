package com.nshm.hostelout

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AccountScreen(navController: NavHostController) {

    val context = LocalContext.current

    var name = remember { mutableStateOf("Vivek Ghosh") }

    Scaffold(
        topBar = { TopBar(name.value) },

        floatingActionButton = {
            FloatingActionButton(onClick = {
                Toast.makeText(context, "Create a New Leave Request", Toast.LENGTH_LONG).show()
                navController.navigate("Form")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },

        content = {
            Contents(Modifier.padding(it))
        },
        bottomBar = { BottomNavigation() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(name: String) {
    val context = LocalContext.current
    TopAppBar(
        title = {
            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
        },
        navigationIcon = {
            IconButton(onClick = { Toast.makeText(context, "Account", Toast.LENGTH_LONG).show() }) {
                Icon(Icons.Default.Person, contentDescription = "Account")
            }
        },
        actions = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Info, contentDescription = "More Info")
            }
        }
    )
}

@Composable
fun BottomNavigation() {
    BottomAppBar(Modifier.height(56.dp)) {
        Row(
            Modifier
                .fillMaxSize()
                .weight(1F),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Home, contentDescription = "Home")
            }
        }
        Row(
            Modifier
                .fillMaxSize()
                .weight(1F),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Menu, contentDescription = "All Requests")
            }
        }
        Row(
            Modifier
                .fillMaxSize()
                .weight(1F),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.AddCircle, contentDescription = "Status")
            }
        }
    }
}

@Composable
fun Contents(modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(15) {
            CardView()
        }
    }
}

@Composable
fun CardView() {
    Card(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(Modifier.padding(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text("From : 06/10/2024")
                Spacer(Modifier.width(45.dp))
                Text("To : 17/10/2024")
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Approval Status : ")
                Spacer(Modifier.width(40.dp))
                Text("Approved")
            }
        }
    }
}