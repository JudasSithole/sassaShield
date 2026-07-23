package com.example.myapplication

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.ManualBlock
import com.example.myapplication.data.ScamDatabase
import com.example.myapplication.data.ScamMessage
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Shield)
    object History : Screen("history", "History", Icons.Default.History)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val screens = listOf(Screen.Dashboard, Screen.History)
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) { 
                ShieldDashboard() 
            }
            composable(Screen.History.route) { 
                ScamHistoryScreen() 
            }
        }
    }
}

@Composable
fun ShieldDashboard() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECEIVE_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var hasCallRole by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            } else true
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasSmsPermission = permissions[Manifest.permission.RECEIVE_SMS] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] == true
        }
    }

    val roleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
            hasCallRole = roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Shield Logo",
            modifier = Modifier.size(100.dp),
            tint = if (hasSmsPermission) Color(0xFF4CAF50) else Color(0xFFF44336)
        )
        
        Text(
            text = "SASSA Shield",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (hasSmsPermission) "Protection Active" else "Protection Disabled",
            fontSize = 18.sp,
            color = if (hasSmsPermission) Color(0xFF4CAF50) else Color(0xFFF44336)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Link, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Check a Link", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                var linkText by remember { mutableStateOf("") }
                var checkResult by remember { mutableStateOf<ScamDetector.DetectionResult?>(null) }
                
                TextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    placeholder = { Text("Paste link here (e.g. sassa.com)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { checkResult = ScamDetector.analyzeMessage(linkText) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Check")
                }
                
                checkResult?.let { result ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (result.isScam) "⚠️ Risk Detected: ${result.reason}" else "✅ Looks Official",
                        color = if (result.isScam) Color(0xFFF44336) else Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Security Status", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasSmsPermission) {
                        "Monitoring incoming SMS for SASSA-related fraud. We will alert you if a suspicious message is detected."
                    } else {
                        "Permission required to monitor SMS. Without this, we cannot protect you from SMS scams."
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Call Protection", fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasCallRole) {
                        "Real-time call screening active. Suspicious callers will be automatically blocked."
                    } else {
                        "Enable Call Screening to automatically block known SASSA scammers and phishing calls."
                    }
                )
                if (!hasCallRole && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val roleManager = context.getSystemService(Context.ROLE_SERVICE) as RoleManager
                            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                            roleLauncher.launch(intent)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Enable Call Blocking")
                    }
                }
            }
        }

        if (!hasSmsPermission || !hasNotificationPermission) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val permissions = mutableListOf(Manifest.permission.RECEIVE_SMS)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    launcher.launch(permissions.toTypedArray())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Enable Protection")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        HorizontalDivider()
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Common Scam Phrases", fontWeight = FontWeight.SemiBold, color = Color.Gray)
        }
        
        Text(
            text = "• \"SASSA PIN has expired\"\n• \"Update your bank details\"\n• \"Collect your R350 grant here\"",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 32.dp, top = 8.dp)
        )
    }
}

@Composable
fun ScamHistoryScreen() {
    val context = LocalContext.current
    val db = remember { ScamDatabase.getDatabase(context) }
    val scams by db.scamDao().getAllScams().collectAsState(initial = emptyList())
    val calls by db.scamDao().getAllScamCalls().collectAsState(initial = emptyList())
    val manualBlocks by db.scamDao().getAllManualBlocks().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Scam History",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text(text = "Messages", modifier = Modifier.padding(12.dp))
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text(text = "Calls", modifier = Modifier.padding(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                if (scams.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No scam messages detected yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(scams) { scam ->
                            ScamItem(scam = scam, onDelete = {
                                scope.launch { db.scamDao().deleteScam(scam) }
                            })
                        }
                    }
                }
            } else {
                if (calls.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No suspicious calls detected yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(calls) { call ->
                            val isManuallyBlocked = manualBlocks.any { it.phoneNumber == call.phoneNumber }
                            CallScamItem(
                                call = call,
                                isManuallyBlocked = isManuallyBlocked,
                                onToggleBlock = {
                                    scope.launch {
                                        if (isManuallyBlocked) {
                                            db.scamDao().deleteManualBlock(ManualBlock(call.phoneNumber))
                                        } else {
                                            db.scamDao().insertManualBlock(ManualBlock(call.phoneNumber))
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallScamItem(
    call: com.example.myapplication.data.ScamCall,
    isManuallyBlocked: Boolean,
    onToggleBlock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (call.wasBlocked || isManuallyBlocked) MaterialTheme.colorScheme.errorContainer 
                             else MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (call.wasBlocked || isManuallyBlocked) "🚫 Blocked: ${call.phoneNumber}" else "⚠️ Warning: ${call.phoneNumber}",
                    fontWeight = FontWeight.Bold,
                    color = if (call.wasBlocked || isManuallyBlocked) MaterialTheme.colorScheme.onErrorContainer 
                            else MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                Button(
                    onClick = onToggleBlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isManuallyBlocked) MaterialTheme.colorScheme.primary 
                                         else MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (isManuallyBlocked) "Unblock" else "Block",
                        fontSize = 12.sp
                    )
                }
            }
            Text(text = "Carrier: ${call.carrier ?: "Unknown"}", fontSize = 14.sp)
            Text(text = "Location: ${call.location ?: "Unknown"}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Risk Level: ${call.riskLevel.uppercase()}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun ScamItem(scam: ScamMessage, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "From: ${scam.sender}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Text(text = scam.body, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Reason: ${scam.reason}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ShieldDashboardPreview() {
    MyApplicationTheme {
        ShieldDashboard()
    }
}
