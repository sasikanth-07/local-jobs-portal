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
fun WorkerScreen(
    workerDao: WorkerDao,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    var currentVoiceField by remember { mutableStateOf<String?>(null) }

    // Text To Speech initialization
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(context) {
        var ttsEngine: TextToSpeech? = null
        try {
            ttsEngine = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // Initialize default language
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
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "WorkerScreenTTS")
    }

    // Voice Assistant speech launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull() ?: ""

            when (currentVoiceField) {
                "name" -> name = spokenText
                "mobile" -> mobile = spokenText.filter { it.isDigit() }
                "skill" -> skill = spokenText
                "experience" -> experience = spokenText
                "location" -> location = spokenText
                "wage" -> wage = spokenText.filter { it.isDigit() }
            }
            speak("Entered $spokenText for $currentVoiceField")
        }
    }

    fun launchVoiceInput(fieldName: String) {
        currentVoiceField = fieldName
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak $fieldName")
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Voice input not supported on this device", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Worker Registration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎙️ Voice Assistant Enabled\nTap mic icons to dictate fields",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = {
                        speak("Welcome to worker registration. You can tap the mic icons next to any field to dictate your information by voice.")
                    }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Listen Instructions")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Name Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { launchVoiceInput("name") }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input Name", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mobile Number Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = mobile,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() }) {
                            mobile = it
                        }
                    },
                    label = { Text("Mobile Number (10 digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { launchVoiceInput("mobile") }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input Mobile", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skill Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = skill,
                    onValueChange = { skill = it },
                    label = { Text("Skill (e.g. Carpenter, Electrician)") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { launchVoiceInput("skill") }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input Skill", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Experience Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = experience,
                    onValueChange = { experience = it },
                    label = { Text("Experience (e.g. 3 years)") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { launchVoiceInput("experience") }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input Experience", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Village / Town Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Village / Town Location") },
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { launchVoiceInput("location") }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input Location", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Expected Daily Wage Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = wage,
                    onValueChange = { wage = it },
                    label = { Text("Expected Daily Wage (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { launchVoiceInput("wage") }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Input Wage", tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Profile Button
            Button(
                onClick = {
                    if (name.isBlank() || mobile.length != 10 || skill.isBlank() || location.isBlank()) {
                        message = "Please fill all required fields correctly (10-digit mobile number)."
                        speak(message)
                    } else {
                        coroutineScope.launch {
                            val newWorker = Worker(
                                name = name.trim(),
                                mobile = mobile.trim(),
                                skill = skill.trim(),
                                experience = experience.trim(),
                                location = location.trim(),
                                wage = wage.trim()
                            )
                            workerDao.insertWorker(newWorker)

                            val successText = "Worker profile for $name saved successfully!"
                            message = successText
                            speak(successText)

                            // Reset fields
                            name = ""
                            mobile = ""
                            skill = ""
                            experience = ""
                            location = ""
                            wage = ""
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text(
                    text = "SAVE PROFILE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
