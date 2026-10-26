package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.localization.Translations
import com.example.data.model.GameType
import com.example.data.model.PlanDuration
import com.example.ui.auth.LoginScreen
import com.example.ui.components.DepositDialog
import com.example.ui.components.EarlyWithdrawalWarningDialog
import com.example.ui.components.GameWithdrawModal
import com.example.ui.components.InvestInPlanDialog
import com.example.ui.components.NumberGuessDialog
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.components.SpinAndWinDialog
import com.example.ui.components.TapTapCoinDialog
import com.example.ui.components.TermsConditionsDialog
import com.example.ui.components.TransferGameModal
import com.example.ui.components.WithdrawCashDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.GoalScreen
import com.example.ui.screens.LanguageSelectScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.ButtonGreen
import com.example.ui.theme.DarkBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AppViewModel = viewModel()
            val profile by viewModel.profile.collectAsState()
            MyApplicationTheme(darkTheme = profile.isDarkMode) {
                InvestProApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun InvestProApp(
    viewModel: AppViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val profile by viewModel.profile.collectAsState()
    val availableCash by viewModel.availableCash.collectAsState()
    val activePlans by viewModel.activePlans.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    val gameWalletBalance by viewModel.gameWalletBalance.collectAsState()
    val dailyGameEarnings by viewModel.dailyGameEarnings.collectAsState()
    val freeGamesRemaining by viewModel.freeGamesRemaining.collectAsState()
    val activeGameModal by viewModel.activeGameModal.collectAsState()
    val showTransferGameModal by viewModel.showTransferGameModal.collectAsState()
    val showGameWithdrawModal by viewModel.showGameWithdrawModal.collectAsState()

    val showLanguageSelect by viewModel.showLanguageSelect.collectAsState()
    val showDeposit by viewModel.showDepositDialog.collectAsState()
    val showInvest by viewModel.showInvestDialog.collectAsState()
    val showWithdrawCash by viewModel.showWithdrawCashDialog.collectAsState()
    val earlyWithdrawPlan by viewModel.earlyWithdrawPlanWarning.collectAsState()
    val notification by viewModel.notification.collectAsState()

    val showPrivacyPolicy by viewModel.showPrivacyPolicy.collectAsState()
    val showTermsConditions by viewModel.showTermsConditions.collectAsState()

    val strings = Translations.get(profile.preferredLanguage)

    // 1. If not logged in, show Login Screen (First Screen User Sees)
    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { phone ->
                viewModel.login(phone)
            }
        )
        return
    }

    // 2. Language Selection gate on first open or when requested
    if (!profile.hasSelectedLanguage || showLanguageSelect) {
        LanguageSelectScreen(
            currentLanguage = profile.preferredLanguage,
            onLanguageSelected = { lang ->
                viewModel.selectLanguage(lang)
            }
        )
        return
    }

    // Auto dismiss notifications after 3.5s
    LaunchedEffect(notification) {
        if (notification != null) {
            delay(3500L)
            viewModel.dismissNotification()
        }
    }

    // Back handling for navigation
    if (currentTab != AppTab.DASHBOARD) {
        BackHandler {
            viewModel.selectTab(AppTab.DASHBOARD)
        }
    }

    val navBarItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = ButtonGreen,
        selectedTextColor = ButtonGreen,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        indicatorColor = if (profile.isDarkMode) Color(0xFF263238) else Color(0xFFE8F8EE)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("investpro_bottom_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // 1. Home
                NavigationBarItem(
                    selected = currentTab == AppTab.DASHBOARD,
                    onClick = { viewModel.selectTab(AppTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.Home, contentDescription = strings.navHome) },
                    label = { Text(strings.navHome, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_home"),
                    colors = navBarItemColors
                )

                // 2. Plans / Investment
                NavigationBarItem(
                    selected = currentTab == AppTab.PLANS,
                    onClick = { viewModel.selectTab(AppTab.PLANS) },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = strings.navPlans) },
                    label = { Text(strings.navPlans, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_plans"),
                    colors = navBarItemColors
                )

                // 3. GAME (New Separate Game Wallet tab)
                NavigationBarItem(
                    selected = currentTab == AppTab.GAME,
                    onClick = { viewModel.selectTab(AppTab.GAME) },
                    icon = { Icon(Icons.Default.SportsEsports, contentDescription = strings.navGame) },
                    label = { Text(strings.navGame, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_game"),
                    colors = navBarItemColors
                )

                // 4. Wallet
                NavigationBarItem(
                    selected = currentTab == AppTab.WALLET,
                    onClick = { viewModel.selectTab(AppTab.WALLET) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = strings.navWallet) },
                    label = { Text(strings.navWallet, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_wallet"),
                    colors = navBarItemColors
                )

                // 5. Profile
                NavigationBarItem(
                    selected = currentTab == AppTab.PROFILE,
                    onClick = { viewModel.selectTab(AppTab.PROFILE) },
                    icon = { Icon(Icons.Default.Person, contentDescription = strings.navProfile) },
                    label = { Text(strings.navProfile, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("nav_profile"),
                    colors = navBarItemColors
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DASHBOARD -> {
                    DashboardScreen(
                        profile = profile,
                        availableCash = availableCash,
                        activePlans = activePlans,
                        recentTransactions = transactions,
                        strings = strings,
                        isDarkMode = profile.isDarkMode,
                        onToggleTheme = { viewModel.toggleDarkMode() },
                        onDepositClick = { viewModel.setDepositDialogVisible(true) },
                        onInvestClick = { viewModel.setInvestDialogVisible(true) },
                        onNavigateToPlans = { viewModel.selectTab(AppTab.PLANS) },
                        onAdvanceMonth = { planId -> viewModel.advancePlanMonth(planId) },
                        onEarlyWithdrawClick = { plan -> viewModel.showEarlyWithdrawWarning(plan) },
                        onMaturityWithdrawClick = { planId -> viewModel.withdrawMaturedPlan(planId) }
                    )
                }
                AppTab.PLANS -> {
                    GoalScreen(
                        availableCash = availableCash,
                        activePlans = activePlans,
                        strings = strings,
                        onOpenInvestModal = { viewModel.setInvestDialogVisible(true) },
                        onInvestDirectly = { amount, dur -> viewModel.investInPlan(amount, dur) },
                        onDepositClick = { viewModel.setDepositDialogVisible(true) }
                    )
                }
                AppTab.GAME -> {
                    GameScreen(
                        gameWalletBalance = gameWalletBalance,
                        availableCash = availableCash,
                        dailyGameEarnings = dailyGameEarnings,
                        freeGamesRemaining = freeGamesRemaining,
                        strings = strings,
                        onPlayGame = { gameType -> viewModel.openGame(gameType) },
                        onOpenTransfer = { viewModel.setTransferGameModalVisible(true) },
                        onOpenWithdraw = { viewModel.setGameWithdrawModalVisible(true) }
                    )
                }
                AppTab.WALLET -> {
                    WalletScreen(
                        availableCash = availableCash,
                        activePlans = activePlans,
                        transactions = transactions,
                        strings = strings,
                        onOpenDeposit = { viewModel.setDepositDialogVisible(true) },
                        onOpenWithdrawCash = { viewModel.setWithdrawCashDialogVisible(true) },
                        onEarlyWithdrawClick = { plan -> viewModel.showEarlyWithdrawWarning(plan) },
                        onMaturityWithdrawClick = { planId -> viewModel.withdrawMaturedPlan(planId) }
                    )
                }
                AppTab.PROFILE -> {
                    ProfileScreen(
                        profile = profile,
                        strings = strings,
                        isDarkMode = profile.isDarkMode,
                        onToggleTheme = { viewModel.toggleDarkMode(it) },
                        onOpenPrivacyPolicy = { viewModel.setPrivacyPolicyVisible(true) },
                        onOpenTermsConditions = { viewModel.setTermsConditionsVisible(true) },
                        onUpdateProfile = { updated -> viewModel.saveProfile(updated) },
                        onUpdatePhoto = { uri -> viewModel.updateProfilePhoto(uri) },
                        onOpenLanguageSelect = { viewModel.openLanguageSelect() },
                        onLogout = { viewModel.logout() }
                    )
                }
            }

            // In-app Notification Banner
            AnimatedVisibility(
                visible = notification != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                notification?.let { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .testTag("notification_banner"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (notif.isSuccess) Color(0xFF0D47A1) else Color(0xFFB71C1C)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (notif.isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (notif.isSuccess) SuccessGreen else Color(0xFFFF8A80),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = notif.title,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = notif.description,
                                    color = Color(0xFFE3F2FD),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogs
    if (showDeposit) {
        DepositDialog(
            strings = strings,
            onDismiss = { viewModel.setDepositDialogVisible(false) },
            onConfirm = { amount, method ->
                viewModel.depositFunds(amount, method)
            }
        )
    }

    if (showInvest) {
        InvestInPlanDialog(
            strings = strings,
            availableCash = availableCash,
            onDismiss = { viewModel.setInvestDialogVisible(false) },
            onConfirmInvest = { amount, dur ->
                viewModel.investInPlan(amount, dur)
            },
            onNeedsDeposit = {
                viewModel.setDepositDialogVisible(true)
            }
        )
    }

    if (showWithdrawCash) {
        WithdrawCashDialog(
            availableBalance = availableCash,
            savedAccount = "${profile.bankName} ${profile.accountNumber}".trim(),
            strings = strings,
            onDismiss = { viewModel.setWithdrawCashDialogVisible(false) },
            onConfirmWithdrawal = { amount, method, destination ->
                viewModel.withdrawCash(amount, method, destination)
            }
        )
    }

    earlyWithdrawPlan?.let { plan ->
        EarlyWithdrawalWarningDialog(
            plan = plan,
            strings = strings,
            onDismiss = { viewModel.dismissEarlyWithdrawWarning() },
            onConfirmEarlyWithdrawal = {
                viewModel.confirmEarlyWithdrawal(plan.id)
            }
        )
    }

    // Interactive Game Play Dialogs
    when (activeGameModal) {
        GameType.SPIN_AND_WIN -> {
            SpinAndWinDialog(
                isFreePlay = freeGamesRemaining > 0,
                onDismiss = { viewModel.closeGame() },
                onSpinClaim = { prize ->
                    viewModel.playGame(GameType.SPIN_AND_WIN, spinSectorReward = prize)
                }
            )
        }
        GameType.TAP_TAP_COIN -> {
            TapTapCoinDialog(
                isFreePlay = freeGamesRemaining > 0,
                onDismiss = { viewModel.closeGame() },
                onComplete = { taps ->
                    viewModel.playGame(GameType.TAP_TAP_COIN, tapCount = taps)
                }
            )
        }
        GameType.NUMBER_GUESS -> {
            NumberGuessDialog(
                onDismiss = { viewModel.closeGame() },
                onSubmitGuess = { number ->
                    viewModel.playGame(GameType.NUMBER_GUESS, guessedNumber = number)
                }
            )
        }
        null -> {}
    }

    if (showTransferGameModal) {
        TransferGameModal(
            gameWalletBalance = gameWalletBalance,
            onDismiss = { viewModel.setTransferGameModalVisible(false) },
            onConfirmTransfer = { amt ->
                viewModel.transferGameToMain(amt)
            }
        )
    }

    if (showGameWithdrawModal) {
        GameWithdrawModal(
            gameWalletBalance = gameWalletBalance,
            savedAccount = "${profile.bankName} ${profile.accountNumber}".trim(),
            onDismiss = { viewModel.setGameWithdrawModalVisible(false) },
            onConfirmWithdrawal = { amt, method, dest ->
                viewModel.withdrawGameWallet(amt, method, dest)
            }
        )
    }

    if (showPrivacyPolicy) {
        PrivacyPolicyDialog(
            onDismiss = { viewModel.setPrivacyPolicyVisible(false) }
        )
    }

    if (showTermsConditions) {
        TermsConditionsDialog(
            onDismiss = { viewModel.setTermsConditionsVisible(false) }
        )
    }
}
