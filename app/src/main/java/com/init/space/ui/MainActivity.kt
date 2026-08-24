package com.init.space.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.init.space.theme.DarkBackground
import com.init.space.theme.InitTheme
import com.init.space.ui.components.BottomNavDestination
import com.init.space.ui.components.InitBottomBar
import com.init.space.ui.detail.DetailScreen
import com.init.space.ui.detail.DetailViewModel
import com.init.space.ui.disclosure.AccessibilityDisclosureScreen
import com.init.space.ui.home.HomeScreen
import com.init.space.ui.home.HomeViewModel
import com.init.space.ui.settings.SettingsScreen
import com.init.space.ui.startup.StartupSplashScreen
import com.init.space.utils.PrefsManager

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val detailViewModel: DetailViewModel by viewModels()

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    private var targetEntryIdState = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestPermissions()

        val initialEntryId = intent?.let {
            val id = it.getLongExtra(EXTRA_OPEN_ENTRY_ID, -1L)
            if (id != -1L) id else null
        }
        targetEntryIdState.value = initialEntryId

        setContent {
            InitSpaceAppRoot()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val entryId = intent.getLongExtra(EXTRA_OPEN_ENTRY_ID, -1L)
        if (entryId != -1L) {
            targetEntryIdState.value = entryId
        }
    }

    @Composable
    private fun InitSpaceAppRoot() {
        InitTheme {
            var showStartupSplash by remember { mutableStateOf(true) }
            var isDisclosureAccepted by remember {
                mutableStateOf(PrefsManager.isDisclosureAccepted(this@MainActivity))
            }
            var currentDestination by remember { mutableStateOf(BottomNavDestination.CAPTURES) }
            var activeDetailId by remember { mutableStateOf<Long?>(targetEntryIdState.value) }

            // Sync with external launch target
            val pendingTargetId = targetEntryIdState.value
            LaunchedEffect(pendingTargetId) {
                if (pendingTargetId != null) {
                    activeDetailId = pendingTargetId
                    targetEntryIdState.value = null
                }
            }

            AnimatedContent(
                targetState = showStartupSplash,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(400, easing = FastOutLinearInEasing))
                },
                label = "startupSplashTransition"
            ) { isSplash ->
                if (isSplash) {
                    StartupSplashScreen(
                        onFinished = {
                            showStartupSplash = false
                        }
                    )
                } else {
                    AnimatedContent(
                        targetState = !isDisclosureAccepted,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(260)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                        },
                        label = "disclosureTransition"
                    ) { showDisclosure ->
                        if (showDisclosure) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .navigationBarsPadding(),
                                color = DarkBackground
                            ) {
                                AccessibilityDisclosureScreen(
                                    onDismiss = {
                                        isDisclosureAccepted = true
                                    }
                                )
                            }
                        } else {
                            AnimatedContent(
                                targetState = activeDetailId,
                                transitionSpec = {
                                    if (targetState != null) {
                                        (slideInVertically(
                                            animationSpec = tween(260, easing = FastOutSlowInEasing),
                                            initialOffsetY = { it / 4 }
                                        ) + fadeIn(animationSpec = tween(240))) togetherWith
                                                (fadeOut(animationSpec = tween(180)))
                                    } else {
                                        (fadeIn(animationSpec = tween(200))) togetherWith
                                                (slideOutVertically(
                                                    animationSpec = tween(220, easing = FastOutLinearInEasing),
                                                    targetOffsetY = { it / 4 }
                                                ) + fadeOut(animationSpec = tween(200)))
                                    }
                                },
                                label = "detailTransition"
                            ) { detailId ->
                                if (detailId != null) {
                                    BackHandler {
                                        activeDetailId = null
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .statusBarsPadding()
                                            .navigationBarsPadding(),
                                        color = DarkBackground
                                    ) {
                                        DetailScreen(
                                            entryId = detailId,
                                            viewModel = detailViewModel,
                                            onBack = {
                                                activeDetailId = null
                                            }
                                        )
                                    }
                                } else {
                                    BackHandler(enabled = currentDestination != BottomNavDestination.CAPTURES) {
                                        currentDestination = BottomNavDestination.CAPTURES
                                        homeViewModel.setFilter(HomeViewModel.FilterMode.ALL)
                                    }

                                    Scaffold(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .statusBarsPadding(),
                                        containerColor = DarkBackground,
                                        bottomBar = {
                                            InitBottomBar(
                                                currentDestination = currentDestination,
                                                onNavigate = { destination ->
                                                    currentDestination = destination
                                                    when (destination) {
                                                        BottomNavDestination.CAPTURES -> {
                                                            homeViewModel.setFilter(HomeViewModel.FilterMode.ALL)
                                                        }
                                                        BottomNavDestination.STARRED -> {
                                                            homeViewModel.setFilter(HomeViewModel.FilterMode.STARRED)
                                                        }
                                                        BottomNavDestination.REMINDERS -> {
                                                            homeViewModel.setFilter(HomeViewModel.FilterMode.REMINDERS)
                                                        }
                                                        BottomNavDestination.SETTINGS -> {
                                                            // Navigates to Settings view
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    ) { innerPadding ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(bottom = innerPadding.calculateBottomPadding())
                                        ) {
                                            AnimatedContent(
                                                targetState = currentDestination,
                                                transitionSpec = {
                                                    val isForward = targetState.ordinal > initialState.ordinal
                                                    if (isForward) {
                                                        (slideInHorizontally(
                                                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                                                            initialOffsetX = { width -> width / 4 }
                                                        ) + fadeIn(animationSpec = tween(220))) togetherWith
                                                                (slideOutHorizontally(
                                                                    animationSpec = tween(180, easing = FastOutLinearInEasing),
                                                                    targetOffsetX = { width -> -width / 4 }
                                                                ) + fadeOut(animationSpec = tween(180)))
                                                    } else {
                                                        (slideInHorizontally(
                                                            animationSpec = tween(220, easing = FastOutSlowInEasing),
                                                            initialOffsetX = { width -> -width / 4 }
                                                        ) + fadeIn(animationSpec = tween(220))) togetherWith
                                                                (slideOutHorizontally(
                                                                    animationSpec = tween(180, easing = FastOutLinearInEasing),
                                                                    targetOffsetX = { width -> width / 4 }
                                                                ) + fadeOut(animationSpec = tween(180)))
                                                    }
                                                },
                                                label = "tabSwitchingTransition"
                                            ) { destination ->
                                                when (destination) {
                                                    BottomNavDestination.CAPTURES,
                                                    BottomNavDestination.STARRED,
                                                    BottomNavDestination.REMINDERS -> {
                                                        HomeScreen(
                                                            viewModel = homeViewModel,
                                                            onOpenEntry = { entryId ->
                                                                activeDetailId = entryId
                                                            },
                                                            onOpenSettings = {
                                                                currentDestination = BottomNavDestination.SETTINGS
                                                            }
                                                        )
                                                    }

                                                    BottomNavDestination.SETTINGS -> {
                                                        SettingsScreen(
                                                            onBack = {
                                                                currentDestination = BottomNavDestination.CAPTURES
                                                                homeViewModel.setFilter(HomeViewModel.FilterMode.ALL)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissions() {
        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
            perms.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            @Suppress("DEPRECATION")
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionsLauncher.launch(perms.toTypedArray())
    }

    companion object {
        const val EXTRA_OPEN_ENTRY_ID = "extra_open_entry_id"
    }
}
