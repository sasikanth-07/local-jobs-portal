package com.example.localworkconnect

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostJobScreen(
    jobDao: JobDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categorySkill by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var employerName by remember { mutableStateOf("") }
    var employerMobile by remember { mutableStateOf("") }

    var currentVoiceField by remember { mutableStateOf<String?>(null) }

    // Text To Speech
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        var ttsEngine: TextToSpeech? = null
        try {
            ttsEngine = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // TTS ready
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
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PostJobTTS")
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull() ?: ""

            when (currentVoiceField) {
                "title" -> title = spokenText
                "description" -> description = spokenText
                "categorySkill" -> categorySkill = spokenText
                "wage" -> wage = spokenText
                "location" -> location = spokenText
                "employerName" -> employerName = spokenText
                "employerMobile" -> employerMobile = spokenText
            }
        }
    }

    fun listenVoice(field: String, prompt: String) {
        currentVoiceField = field
        speak(prompt)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📢 Post a New Job") },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎙️ Tap mic icons to speak details easily!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        speak("Fill in the job title, required skill, wage, location and your mobile number to post a job.")
                    }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen Guidance")
                    }
                }
            }

            // Job Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Job Title (e.g. Masonry, Painting)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { listenVoice("title", "Speak the job title") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Title")
                    }
                }
            )

            // Category / Required Skill
            OutlinedTextField(
                value = categorySkill,
                onValueChange = { categorySkill = it },
                label = { Text("Required Skill (e.g. Painter, Carpenter)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { listenVoice("categorySkill", "Speak required skill") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Skill")
                    }
                }
            )

            // Daily Wage
            OutlinedTextField(
                value = wage,
                onValueChange = { wage = it },
                label = { Text("Daily Wage (e.g. ₹600/day)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { listenVoice("wage", "Speak daily wage amount") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Wage")
                    }
                }
            )

            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Job Location / Village / City") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { listenVoice("location", "Speak job location") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Location")
                    }
                }
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Job Description / Work details") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { listenVoice("description", "Speak job description") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Description")
                    }
                }
            )

            // Employer Name
            OutlinedTextField(
                value = employerName,
                onValueChange = { employerName = it },
                label = { Text("Your Name (Employer)") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { listenVoice("employerName", "Speak your name") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Employer Name")
                    }
                }
            )

            // Mobile Number
            OutlinedTextField(
                value = employerMobile,
                onValueChange = { employerMobile = it },
                label = { Text("Mobile Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { listenVoice("employerMobile", "Speak mobile number") }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice Mobile")
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (title.isBlank() || location.isBlank() || employerMobile.isBlank()) {
                        Toast.makeText(context, "Please fill in Title, Location, and Mobile Number", Toast.LENGTH_SHORT).show()
                    } else {
                        coroutineScope.launch {
                            val newJob = Job(
                                title = title,
                                description = description,
                                categorySkill = categorySkill,
                                wage = wage,
                                location = location,
                                employerName = employerName,
                                employerMobile = employerMobile
                            )
                            jobDao.insertJob(newJob)
                            speak("Job posted successfully!")
                            Toast.makeText(context, "Job Posted Successfully!", Toast.LENGTH_SHORT).show()
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "📢 POST JOB NOW",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
