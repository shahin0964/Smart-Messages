package com.smartmessages.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartmessages.app.data.repository.SmsRepository
import com.smartmessages.app.ui.screens.ChatScreen
import com.smartmessages.app.ui.screens.ConversationListScreen
import com.smartmessages.app.ui.screens.NewMessageScreen
import com.smartmessages.app.ui.theme.SmartMessagesTheme
import com.smartmessages.app.util.PermissionHelper

class MainActivity : ComponentActivity() {

    private lateinit var repository: SmsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = SmsRepository(applicationContext)

        val initialAddress = extractAddressFromIntent(intent)

        setContent {
            SmartMessagesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SmartMessagesAppNav(
                        repository = repository,
                        initialAddress = initialAddress,
                        onRequestPermissions = {
                            // Permission handling inside composable
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractAddressFromIntent(intent: Intent?): String? {
        val data = intent?.data ?: return null
        val scheme = data.scheme
        return if (scheme == "sms" || scheme == "smsto" || scheme == "mms" || scheme == "mmsto") {
            data.schemeSpecificPart?.substringBefore('?')
        } else {
            null
        }
    }
}

@Composable
fun SmartMessagesAppNav(
    repository: SmsRepository,
    initialAddress: String?,
    onRequestPermissions: () -> Unit
) {
    val navController = rememberNavController()

    // Request Runtime Permissions on Launch
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // Permissions granted/denied, reload conversations
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(PermissionHelper.REQUIRED_PERMISSIONS)
    }

    NavHost(
        navController = navController,
        startDestination = "inbox"
    ) {
        composable("inbox") {
            ConversationListScreen(
                repository = repository,
                onOpenChat = { threadId, address ->
                    val encodedAddress = Uri.encode(address)
                    navController.navigate("chat/$threadId/$encodedAddress")
                },
                onComposeNew = {
                    navController.navigate("new_message")
                },
                onOpenSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("settings") {
            com.smartmessages.app.ui.screens.SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "chat/{threadId}/{address}",
            arguments = listOf(
                navArgument("threadId") { type = NavType.LongType },
                navArgument("address") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
            val rawAddress = backStackEntry.arguments?.getString("address") ?: ""
            val address = Uri.decode(rawAddress)

            ChatScreen(
                threadId = threadId,
                address = address,
                repository = repository,
                onBack = { navController.popBackStack() }
            )
        }

        composable("new_message") {
            NewMessageScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onSent = { address ->
                    navController.popBackStack()
                }
            )
        }
    }
}
