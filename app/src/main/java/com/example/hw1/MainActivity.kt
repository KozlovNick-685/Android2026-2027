package com.example.hw1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hw1.ui.FirstScreen
import com.example.hw1.ui.SecondScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            val generatedPassword = remember { PasswordGenerator.generate() }

            var enteredEmail by remember { mutableStateOf("") }
            var enteredPassword by remember { mutableStateOf("") }

            NavHost(navController = navController, startDestination = "first") {

                composable("first") {
                    FirstScreen(
                        onNavigateNext = { email, password ->
                            enteredEmail = email
                            enteredPassword = password
                            navController.navigate("second")
                        }
                    )
                }

                composable("second") {
                    SecondScreen(
                        email = enteredEmail,
                        enteredPassword = enteredPassword,
                        generatedPassword = generatedPassword,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}