package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.localization.AppLanguage
import com.example.data.model.ActiveInvestmentPlan
import com.example.data.model.GamePlayResult
import com.example.data.model.GameType
import com.example.data.model.PaymentMethod
import com.example.data.model.PlanDuration
import com.example.data.model.TransactionRecord
import com.example.data.model.UserProfile
import com.example.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab {
    DASHBOARD,
    PLANS,
    GAME,
    WALLET,
    PROFILE
}

data class NotificationMessage(
    val title: String,
    val message: String,
    val description: String = message,
    val isSuccess: Boolean = true
)

class AppViewModel(
    private val repository: WalletRepository = WalletRepository()
) : ViewModel() {

    val profile: StateFlow<UserProfile> = repository.getProfileStream()
    val availableCash: StateFlow<Double> = repository.getAvailableCashStream()
    val gameWalletBalance: StateFlow<Double> = repository.getGameWalletBalanceStream()
    val dailyGameEarnings: StateFlow<Double> = repository.getDailyGameEarningsStream()
    val freeGamesRemaining: StateFlow<Int> = repository.getFreeGamesRemainingStream()
    val activePlans: StateFlow<List<ActiveInvestmentPlan>> = repository.getActivePlansStream()
    val transactions: StateFlow<List<TransactionRecord>> = repository.getTransactionLogs()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _showLanguageSelect = MutableStateFlow(false)
    val showLanguageSelect: StateFlow<Boolean> = _showLanguageSelect.asStateFlow()

    private val _showDepositDialog = MutableStateFlow(false)
    val showDepositDialog: StateFlow<Boolean> = _showDepositDialog.asStateFlow()

    private val _showInvestDialog = MutableStateFlow(false)
    val showInvestDialog: StateFlow<Boolean> = _showInvestDialog.asStateFlow()

    private val _showWithdrawCashDialog = MutableStateFlow(false)
    val showWithdrawCashDialog: StateFlow<Boolean> = _showWithdrawCashDialog.asStateFlow()

    private val _earlyWithdrawPlanWarning = MutableStateFlow<ActiveInvestmentPlan?>(null)
    val earlyWithdrawPlanWarning: StateFlow<ActiveInvestmentPlan?> = _earlyWithdrawPlanWarning.asStateFlow()

    private val _activeGameModal = MutableStateFlow<GameType?>(null)
    val activeGameModal: StateFlow<GameType?> = _activeGameModal.asStateFlow()

    private val _showTransferGameModal = MutableStateFlow(false)
    val showTransferGameModal: StateFlow<Boolean> = _showTransferGameModal.asStateFlow()

    private val _showGameWithdrawModal = MutableStateFlow(false)
    val showGameWithdrawModal: StateFlow<Boolean> = _showGameWithdrawModal.asStateFlow()

    private val _showPrivacyPolicy = MutableStateFlow(false)
    val showPrivacyPolicy: StateFlow<Boolean> = _showPrivacyPolicy.asStateFlow()

    private val _showTermsConditions = MutableStateFlow(false)
    val showTermsConditions: StateFlow<Boolean> = _showTermsConditions.asStateFlow()

    private val _notification = MutableStateFlow<NotificationMessage?>(null)
    val notification: StateFlow<NotificationMessage?> = _notification.asStateFlow()

    fun login(rawPhone: String) {
        val normalized = repository.formatToPlus94(rawPhone)
        repository.initializeUserSession(normalized)
        _isLoggedIn.value = true
        _notification.value = NotificationMessage(
            title = "Welcome to InvestPro",
            message = "Signed in securely as $normalized"
        )
    }

    fun logout() {
        repository.clearUserDataOnLogout()
        _isLoggedIn.value = false
        _currentTab.value = AppTab.DASHBOARD
        _notification.value = NotificationMessage(
            title = "Logged Out",
            message = "You have been securely signed out."
        )
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openLanguageSelect() {
        _showLanguageSelect.value = true
    }

    fun selectLanguage(language: AppLanguage) {
        repository.updateLanguage(language)
        _showLanguageSelect.value = false
    }

    fun setDepositDialogVisible(visible: Boolean) {
        _showDepositDialog.value = visible
    }

    fun setInvestDialogVisible(visible: Boolean) {
        _showInvestDialog.value = visible
    }

    fun setWithdrawCashDialogVisible(visible: Boolean) {
        _showWithdrawCashDialog.value = visible
    }

    fun showEarlyWithdrawWarning(plan: ActiveInvestmentPlan) {
        _earlyWithdrawPlanWarning.value = plan
    }

    fun dismissEarlyWithdrawWarning() {
        _earlyWithdrawPlanWarning.value = null
    }

    fun toggleDarkMode(enabled: Boolean? = null) {
        val target = enabled ?: !profile.value.isDarkMode
        repository.toggleDarkMode(target)
    }

    fun setPrivacyPolicyVisible(visible: Boolean) {
        _showPrivacyPolicy.value = visible
    }

    fun setTermsConditionsVisible(visible: Boolean) {
        _showTermsConditions.value = visible
    }

    fun dismissNotification() {
        _notification.value = null
    }

    fun updateProfilePhoto(uri: String?) {
        repository.updateProfilePhoto(uri)
        _notification.value = NotificationMessage(
            title = "Profile Photo",
            message = if (uri != null) "Photo updated successfully." else "Photo removed."
        )
    }

    fun saveProfile(updated: UserProfile) {
        repository.updateProfile(updated)
        _notification.value = NotificationMessage(
            title = "Profile Saved",
            message = "Account information updated."
        )
    }

    fun depositFunds(amount: Double, method: PaymentMethod = PaymentMethod.LANKA_QR) {
        viewModelScope.launch {
            val result = repository.depositFunds(amount, method)
            _showDepositDialog.value = false
            result.onSuccess {
                _notification.value = NotificationMessage(
                    title = "Deposit Successful",
                    message = "Rs. ${"%,.0f".format(amount)} added to your InvestPro Wallet."
                )
            }.onFailure { error ->
                _notification.value = NotificationMessage(
                    title = "Deposit Failed",
                    message = error.message ?: "Could not process deposit.",
                    isSuccess = false
                )
            }
        }
    }

    fun investInPlan(amount: Double, planDuration: PlanDuration) {
        viewModelScope.launch {
            val result = repository.investInPlan(amount, planDuration)
            _showInvestDialog.value = false
            result.onSuccess { plan ->
                val vipNotice = if (plan.isVip) " (+10% VIP Bonus active!)" else ""
                _notification.value = NotificationMessage(
                    title = "Investment Activated! 🚀",
                    message = "Rs. ${"%,.0f".format(amount)} locked into ${plan.planDuration.title} (${plan.totalProfitPercent.toInt()}% Profit)$vipNotice"
                )
            }.onFailure { error ->
                _notification.value = NotificationMessage(
                    title = "Investment Failed",
                    message = error.message ?: "Could not activate investment.",
                    isSuccess = false
                )
            }
        }
    }

    fun advancePlanMonth(planId: String) {
        viewModelScope.launch {
            repository.advancePlanMonth(planId)
            val updatedPlan = repository.getActivePlansStream().value.find { it.id == planId }
            if (updatedPlan != null) {
                if (updatedPlan.isMatured) {
                    _notification.value = NotificationMessage(
                        title = "🎉 Plan Matured!",
                        message = "Your plan has reached 100% maturity! Total payout: Rs. ${"%,.0f".format(updatedPlan.finalMaturityAmount)}"
                    )
                } else {
                    _notification.value = NotificationMessage(
                        title = "Monthly Growth Step",
                        message = "Plan progressed to Month ${updatedPlan.currentMonth} of ${updatedPlan.planDuration.months}. Value: Rs. ${"%,.0f".format(updatedPlan.currentGrowingValue)}"
                    )
                }
            }
        }
    }

    fun withdrawMaturedPlan(planId: String) {
        viewModelScope.launch {
            val result = repository.withdrawMaturedPlan(planId)
            result.onSuccess { total ->
                _notification.value = NotificationMessage(
                    title = "Full Maturity Payout Credited! 🏆",
                    message = "Rs. ${"%,.0f".format(total)} (Capital + Profit) deposited into your cash balance."
                )
            }.onFailure { error ->
                _notification.value = NotificationMessage(
                    title = "Withdrawal Failed",
                    message = error.message ?: "Failed to claim matured plan.",
                    isSuccess = false
                )
            }
        }
    }

    fun confirmEarlyWithdrawal(planId: String) {
        viewModelScope.launch {
            val result = repository.withdrawEarlyPrincipalOnly(planId)
            _earlyWithdrawPlanWarning.value = null
            result.onSuccess { principal ->
                _notification.value = NotificationMessage(
                    title = "Principal Withdrawn",
                    message = "Early withdrawal processed. Rs. ${"%,.0f".format(principal)} returned to cash balance (profit forfeited)."
                )
            }.onFailure { error ->
                _notification.value = NotificationMessage(
                    title = "Withdrawal Error",
                    message = error.message ?: "Failed early withdrawal.",
                    isSuccess = false
                )
            }
        }
    }

    fun withdrawCash(amount: Double, method: PaymentMethod, destination: String) {
        viewModelScope.launch {
            val result = repository.withdrawCash(amount, method, destination)
            _showWithdrawCashDialog.value = false
            result.onSuccess {
                _notification.value = NotificationMessage(
                    title = "Withdrawal Dispatched",
                    message = "Rs. ${"%,.0f".format(amount)} sent to $destination via ${method.displayName}."
                )
            }.onFailure { error ->
                _notification.value = NotificationMessage(
                    title = "Withdrawal Failed",
                    message = error.message ?: "Could not process withdrawal.",
                    isSuccess = false
                )
            }
        }
    }

    fun openGame(gameType: GameType) {
        _activeGameModal.value = gameType
    }

    fun closeGame() {
        _activeGameModal.value = null
    }

    fun setTransferGameModalVisible(visible: Boolean) {
        _showTransferGameModal.value = visible
    }

    fun setGameWithdrawModalVisible(visible: Boolean) {
        _showGameWithdrawModal.value = visible
    }

    fun playGame(gameType: GameType, guessedNumber: Int? = null, tapCount: Int? = null, spinSectorReward: Double? = null) {
        viewModelScope.launch {
            val result = repository.playGame(gameType, guessedNumber, tapCount, spinSectorReward)
            result.onSuccess { playResult ->
                if (playResult.isWin) {
                    _notification.value = NotificationMessage(
                        title = "🎉 Win in ${gameType.title}!",
                        message = "Rs. ${"%,.0f".format(playResult.winAmount)} added to your Game Wallet!"
                    )
                } else {
                    _notification.value = NotificationMessage(
                        title = gameType.title,
                        message = playResult.message,
                        isSuccess = false
                    )
                }
            }.onFailure { error ->
                _notification.value = NotificationMessage(
                    title = "Game Error",
                    message = error.message ?: "Unable to play game.",
                    isSuccess = false
                )
            }
        }
    }

    fun transferGameToMain(amount: Double) {
        viewModelScope.launch {
            val result = repository.transferGameToMain(amount)
            _showTransferGameModal.value = false
            result.onSuccess { total ->
                _notification.value = NotificationMessage(
                    title = "Transfer Successful! 💰",
                    message = "Rs. ${"%,.0f".format(total)} moved to your Main Wallet ready for High Profit investments!"
                )
            }.onFailure { error ->
                _notification.value = NotificationMessage(
                    title = "Transfer Failed",
                    message = error.message ?: "Could not complete transfer.",
                    isSuccess = false
                )
            }
        }
    }

    fun withdrawGameWallet(amount: Double, method: PaymentMethod, destination: String) {
        viewModelScope.launch {
            val result = repository.withdrawGameEarnings(amount, method, destination)
            _showGameWithdrawModal.value = false
            result.onSuccess {
                _notification.value = NotificationMessage(
                    title = "Game Withdrawal Dispatched 🎮",
                    message = "Rs. ${"%,.0f".format(amount)} sent to $destination via ${method.displayName}."
                )
            }.onFailure { error ->
                _notification.value = NotificationMessage(
                    title = "Withdrawal Failed",
                    message = error.message ?: "Could not process Game Wallet withdrawal.",
                    isSuccess = false
                )
            }
        }
    }
}
