package com.example.localworkconnect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindJobsScreen(
    jobDao: JobDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var jobsList by remember { mutableStateOf<List<Job>>(emptyList()) }

    var selectedJobForApply by remember { mutableStateOf<Job?>(null) }
    var applicantName by remember { mutableStateOf("") }
    var applicantMobile by remember { mutableStateOf("") }
    var applicantSkill by remember { mutableStateOf("") }

    // TTS Engine
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        var ttsEngine: TextToSpeech? = null
        try {
            ttsEngine = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // Ready
                }
            }
        } catch (_: Exception) {}
        tts = ttsEngine
        onDispose {
            try {
                ttsEngine?.stop()
                ttsEngine?.shutdown()
            } catch (_: Exception) {}
        }
    }

    fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "FindJobsTTS")
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull() ?: ""
            searchQuery = spokenText
            speak("Searching jobs for $spokenText")
        }
    }

    fun startVoiceSearch() {
        speak("Say the job name, skill or location you are looking for")
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Search Jobs")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice input not supported", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(searchQuery) {
        coroutineScope.launch {
            jobsList = if (searchQuery.isBlank()) {
                jobDao.getAllJobs()
            } else {
                jobDao.searchJobs(searchQuery)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔍 Find & Apply for Jobs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Voice Search Input Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Job, Skill or Location") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    Row {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                        IconButton(onClick = { startVoiceSearch() }) {
                            Icon(Icons.Default.Mic, contentDescription = "Voice Search")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (jobsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No jobs found.\nEmployers can post new jobs!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(jobsList) { job ->
                        JobItemCard(
                            job = job,
                            onReadOut = {
                                speak("Job title: ${job.title}. Location: ${job.location}. Daily wage: ${job.wage}. Employer name: ${job.employerName}.")
                            },
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${job.employerMobile}")
                                }
                                context.startActivity(intent)
                            },
                            onApply = {
                                selectedJobForApply = job
                            }
                        )
                    }
                }
            }
        }
    }

    // Apply Modal Dialog
    selectedJobForApply?.let { job ->
        AlertDialog(
            onDismissRequest = { selectedJobForApply = null },
            title = { Text("Apply for ${job.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Wage: ${job.wage} | Location: ${job.location}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = applicantName,
                        onValueChange = { applicantName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = applicantMobile,
                        onValueChange = { applicantMobile = it },
                        label = { Text("Your Mobile Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = applicantSkill,
                        onValueChange = { applicantSkill = it },
                        label = { Text("Your Primary Skill") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (applicantName.isBlank() || applicantMobile.isBlank()) {
                        Toast.makeText(context, "Please enter Name & Mobile Number", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            val application = JobApplication(
                                jobId = job.id,
                                jobTitle = job.title,
                                workerName = applicantName,
                                workerMobile = applicantMobile,
                                workerSkill = applicantSkill
                            )
                            jobDao.insertApplication(application)
                            speak("Your job application has been submitted successfully!")
                            Toast.makeText(context, "Applied Successfully!", Toast.LENGTH_SHORT).show()
                            selectedJobForApply = null
                        }
                    }
                }) {
                    Text("Submit Application")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedJobForApply = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun JobItemCard(
    job: Job,
    onReadOut: () -> Unit,
    onCall: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = job.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onReadOut) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Read Details",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (job.categorySkill.isNotBlank()) {
                Text(
                    text = "🛠️ Required Skill: ${job.categorySkill}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (job.wage.isNotBlank()) {
                Text(
                    text = "💰 Daily Wage: ${job.wage}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.height(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = job.location,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (job.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = job.description,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.height(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call Employer")
                }

                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("✅ Apply Now")
                }
            }
        }
    }
}
