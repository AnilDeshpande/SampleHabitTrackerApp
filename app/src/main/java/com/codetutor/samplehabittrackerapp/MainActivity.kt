package com.codetutor.samplehabittrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.codetutor.samplehabittrackerapp.navigation.HabitTrackerNavHost
import com.codetutor.samplehabittrackerapp.ui.theme.SampleHabitTrackerAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleHabitTrackerAppTheme {
                HabitTrackerNavHost()
            }
        }
    }
}
