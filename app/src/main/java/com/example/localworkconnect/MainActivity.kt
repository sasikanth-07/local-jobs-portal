package com.example.localworkconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localworkconnect.ui.theme.LocalWorkConnectTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LocalWorkConnectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocalWorkConnectScreen()
                }
            }
        }
    }
}

@Composable
fun LocalWorkConnectScreen() {
    val context = LocalContext.current.applicationContext
    val dbState = produceState<AppDatabase?>(initialValue = null) {
        withContext(Dispatchers.IO) {
            value = AppDatabase.getDatabase(context)
        }
    }

    val database = dbState.value
    if (database == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val workerDao = remember(database) { database.workerDao() }
    val jobDao = remember(database) { database.jobDao() }

    var screen by remember { mutableStateOf("home") }

    when (screen) {
        "home" -> HomeScreen(
            onNavigateToWorker = { screen = "worker_dashboard" },
            onNavigateToEmployer = { screen = "employer_dashboard" }
        )
        "worker_dashboard" -> WorkerDashboardScreen(
            onNavigateToFindJobs = { screen = "find_jobs" },
            onNavigateToRegister = { screen = "register_worker" },
            onBack = { screen = "home" }
        )
        "employer_dashboard" -> EmployerDashboardScreen(
            onNavigateToPostJob = { screen = "post_job" },
            onNavigateToWorkerSearch = { screen = "search_workers" },
            onBack = { screen = "home" }
        )
        "find_jobs" -> FindJobsScreen(
            jobDao = jobDao,
            onBack = { screen = "worker_dashboard" }
        )
        "register_worker" -> WorkerScreen(
            workerDao = workerDao,
            onBack = { screen = "worker_dashboard" }
        )
        "post_job" -> PostJobScreen(
            jobDao = jobDao,
            onBack = { screen = "employer_dashboard" }
        )
        "search_workers" -> EmployerScreen(
            workerDao = workerDao,
            onBack = { screen = "employer_dashboard" }
        )
    }
}

@Composable
fun HomeScreen(
    onNavigateToWorker: () -> Unit,
    onNavigateToEmployer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "🤝",
            fontSize = 70.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LOCAL WORK CONNECT",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Bridge local skilled workers with employers\nwith Voice Assistant support 🎙️",
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎙️ Voice Assistant Ready",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Search workers or register profiles easily using voice speech in your preferred language.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onNavigateToWorker,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text(
                text = "👷 I AM A WORKER",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToEmployer,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
        ) {
            Text(
                text = "🏢 I AM AN EMPLOYER",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
