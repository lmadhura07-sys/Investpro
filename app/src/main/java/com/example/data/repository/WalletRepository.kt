package com.example.data.repository

import com.example.data.localization.AppLanguage
import com.example.data.model.ActiveInvestmentPlan
import com.example.data.model.GamePlayResult
import com.example.data.model.GameType
import com.example.data.model.PaymentMethod
import com.example.data.model.PlanDuration
import com.example.data.model.TransactionRecord
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class WalletRepository {

    // Clean user profile - starts empty, populated when user logs in with their +94 number
    private val _profile = MutableStateFlow(UserProfile())
    private val _availableCash = MutableStateFlow(0.0) // Main Investment Wallet
    private val _gameWalletBalance = MutableStateFlow(0.0) // Separate Game Earning Wallet
    private val _dailyGameEarnings = MutableStateFlow(0.0)
    private val _freeGamesRemaining = MutableStateFlow(3)
    private val _activePlans = MutableStateFlow<List<ActiveInvestmentPlan>>(emptyList())
    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())

    /**
     * Normalizes a raw phone number input into the official format: +94 77 123 4567
     */
    fun formatToPlus94(rawInput: String): String {
        val digits = rawInput.filter { it.isDigit() }
        val localDigits = when {
            digits.startsWith("94") -> digits.drop(2)
            digits.startsWith("0") -> digits.drop(1)
            else -> digits
        }

        return if (localDigits.length >= 9) {
            val part1 = localDigits.substring(0, 2) // e.g. 77
            val part2 = localDigits.substring(2, 5) // e.g. 123
            val part3 = localDigits.substring(5, 9) // e.g. 4567
            "+94 $part1 $part2 $part3"
        } else {
            "+94 $localDigits"
        }
    }

    /**
     * Initializes or loads the private storage for this specific +94 phone number
     */
    fun initializeUserSession(normalizedPhone: String, enteredName: String = "") {
        _profile.update { current ->
            current.copy(
                phoneNumber = normalizedPhone,
                fullName = if (enteredName.isNotBlank()) enteredName else if (current.fullName.isNotBlank()) current.fullName else "Member",
                hasSelectedLanguage = current.hasSelectedLanguage
            )
        }
    }

    fun getProfileStream(): StateFlow<UserProfile> = _profile.asStateFlow()
    fun getAvailableCashStream(): StateFlow<Double> = _availableCash.asStateFlow()
    fun getGameWalletBalanceStream(): StateFlow<Double> = _gameWalletBalance.asStateFlow()
    fun getDailyGameEarningsStream(): StateFlow<Double> = _dailyGameEarnings.asStateFlow()
    fun getFreeGamesRemainingStream(): StateFlow<Int> = _freeGamesRemaining.asStateFlow()
    fun getActivePlansStream(): StateFlow<List<ActiveInvestmentPlan>> = _activePlans.asStateFlow()
    fun getTransactionLogs(): StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    fun updateLanguage(language: AppLanguage) {
        _profile.update { it.copy(preferredLanguage = language, hasSelectedLanguage = true) }
    }

    fun updateProfilePhoto(photoUri: String?) {
        _profile.update { it.copy(profilePhotoUri = photoUri) }
    }

    fun updateProfile(updated: UserProfile) {
        _profile.value = updated
    }

    fun toggleDarkMode(isDark: Boolean) {
        _profile.update { it.copy(isDarkMode = isDark) }
    }

    /**
     * Deposit Rule: Minimum Rs 100 strictly
     */
    fun depositFunds(amountLkr: Double, method: PaymentMethod = PaymentMethod.LANKA_QR): Result<TransactionRecord> {
        if (amountLkr < 100.0) {
            return Result.failure(IllegalArgumentException("Minimum deposit is Rs 100"))
        }
        if (amountLkr > 500000.0) {
            return Result.failure(IllegalArgumentException("Maximum single deposit is Rs 500,000"))
        }

        val now = System.currentTimeMillis()
        val tx = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Deposit Funds",
            amountLkr = amountLkr,
            type = TransactionType.DEPOSIT,
            status = TransactionStatus.COMPLETED,
            method = method,
            destination = "InvestPro Cash Wallet",
            timestamp = now,
            reference = "DEP-${(now % 1000000)}"
        )

        _availableCash.update { it + amountLkr }
        _transactions.update { listOf(tx) + it }

        return Result.success(tx)
    }

    /**
     * Invest into High Profit Plan:
     * - 3 Months: 50% Profit (1.5x)
     * - 6 Months: 120% Profit (2.2x)
     * - 12 Months: 300% Profit (4x)
     * - VIP Bonus: deposits >= Rs 100,000 get extra +10% profit!
     */
    fun investInPlan(amountLkr: Double, planDuration: PlanDuration): Result<ActiveInvestmentPlan> {
        if (amountLkr < 100.0) {
            return Result.failure(IllegalArgumentException("Minimum deposit is Rs 100"))
        }
        if (_availableCash.value < amountLkr) {
            return Result.failure(IllegalArgumentException("Insufficient cash balance. Please deposit first."))
        }

        val now = System.currentTimeMillis()
        val maturityMs = now + (planDuration.months.toLong() * 30L * 86400000L)
        val isVip = amountLkr >= 100000.0

        val newPlan = ActiveInvestmentPlan(
            id = UUID.randomUUID().toString(),
            depositAmount = amountLkr,
            planDuration = planDuration,
            startDate = now,
            maturityDate = maturityMs,
            currentMonth = 1,
            isMatured = false,
            isVip = isVip
        )

        val tx = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Started ${planDuration.title}",
            amountLkr = -amountLkr,
            type = TransactionType.PLAN_INVESTMENT,
            status = TransactionStatus.COMPLETED,
            method = PaymentMethod.LANKA_QR,
            destination = "${planDuration.title} (${planDuration.multiplierText})",
            timestamp = now,
            reference = "INV-${(now % 1000000)}"
        )

        _availableCash.update { it - amountLkr }
        _activePlans.update { listOf(newPlan) + it }
        _transactions.update { listOf(tx) + it }

        return Result.success(newPlan)
    }

    /**
     * Advance simulation month for demo testing of monthly growth
     */
    fun advancePlanMonth(planId: String) {
        _activePlans.update { list ->
            list.map { plan ->
                if (plan.id == planId) {
                    val nextMonth = plan.currentMonth + 1
                    val matured = nextMonth >= plan.planDuration.months
                    plan.copy(
                        currentMonth = nextMonth.coerceAtMost(plan.planDuration.months),
                        isMatured = matured
                    )
                } else {
                    plan
                }
            }
        }
    }

    /**
     * Maturity Withdrawal (Full Capital + Profit)
     */
    fun withdrawMaturedPlan(planId: String): Result<Double> {
        val plan = _activePlans.value.find { it.id == planId }
            ?: return Result.failure(IllegalArgumentException("Plan not found"))

        val totalPayout = plan.finalMaturityAmount
        val now = System.currentTimeMillis()

        val tx = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Maturity Payout (${plan.planDuration.title})",
            amountLkr = totalPayout,
            type = TransactionType.MATURITY_PAYOUT,
            status = TransactionStatus.COMPLETED,
            method = PaymentMethod.BANK_TRANSFER,
            destination = "InvestPro Cash Wallet",
            timestamp = now,
            reference = "MAT-${(now % 1000000)}"
        )

        _activePlans.update { it.filterNot { p -> p.id == planId } }
        _availableCash.update { it + totalPayout }
        _transactions.update { listOf(tx) + it }

        return Result.success(totalPayout)
    }

    /**
     * Early Withdrawal Rule:
     * User gets only their main deposit (principal), NO profit.
     */
    fun withdrawEarlyPrincipalOnly(planId: String): Result<Double> {
        val plan = _activePlans.value.find { it.id == planId }
            ?: return Result.failure(IllegalArgumentException("Plan not found"))

        val principalOnly = plan.depositAmount
        val now = System.currentTimeMillis()

        val tx = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Early Withdrawal (Principal Only)",
            amountLkr = principalOnly,
            type = TransactionType.EARLY_WITHDRAWAL,
            status = TransactionStatus.COMPLETED,
            method = PaymentMethod.BANK_TRANSFER,
            destination = "InvestPro Cash Wallet",
            timestamp = now,
            reference = "ERL-${(now % 1000000)}"
        )

        _activePlans.update { it.filterNot { p -> p.id == planId } }
        _availableCash.update { it + principalOnly }
        _transactions.update { listOf(tx) + it }

        return Result.success(principalOnly)
    }

    /**
     * Withdraw cash to Bank / eZ Cash / USDT (Min Rs 200)
     */
    fun withdrawCash(amountLkr: Double, method: PaymentMethod, destination: String): Result<TransactionRecord> {
        if (amountLkr < 200.0) {
            return Result.failure(IllegalArgumentException("Minimum withdrawal is Rs. 200"))
        }
        if (_availableCash.value < amountLkr) {
            return Result.failure(IllegalArgumentException("Insufficient available cash."))
        }

        val now = System.currentTimeMillis()
        val tx = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Withdrawal to ${method.displayName}",
            amountLkr = -amountLkr,
            type = TransactionType.EARLY_WITHDRAWAL,
            status = TransactionStatus.COMPLETED,
            method = method,
            destination = destination,
            timestamp = now,
            reference = "WTH-${(now % 1000000)}"
        )

        _availableCash.update { it - amountLkr }
        _transactions.update { listOf(tx) + it }

        return Result.success(tx)
    }

    /**
     * Executes gameplay with strict separation of wallets:
     * - Winnings go exclusively to Game Wallet (NEVER Main Wallet)
     * - First 3 games per day free, subsequent paid games deduct Rs. 100 from Main Wallet
     * - Daily earnings capped at Rs. 3,000 max per day
     */
    fun playGame(
        gameType: GameType,
        guessedNumber: Int? = null,
        tapCount: Int? = null,
        spinSectorReward: Double? = null
    ): Result<GamePlayResult> {
        val dailyCap = 3000.0
        if (_dailyGameEarnings.value >= dailyCap) {
            return Result.failure(IllegalStateException("Daily maximum earnings limit of Rs. 3,000 reached for today."))
        }

        val isFreePlay = _freeGamesRemaining.value > 0 && gameType != GameType.NUMBER_GUESS
        val fee = if (isFreePlay) 0.0 else 100.0

        if (fee > 0.0) {
            if (_availableCash.value < fee) {
                return Result.failure(IllegalArgumentException("Insufficient Main Wallet balance. Rs. 100 entry fee required."))
            }
            // Deduct entry fee from Main Wallet
            _availableCash.update { it - fee }
            val now = System.currentTimeMillis()
            val feeTx = TransactionRecord(
                id = UUID.randomUUID().toString(),
                title = "Game Entry Fee (${gameType.title})",
                amountLkr = -fee,
                type = TransactionType.GAME_FEE,
                status = TransactionStatus.COMPLETED,
                method = PaymentMethod.LANKA_QR,
                destination = "Game Center",
                timestamp = now,
                reference = "FEE-${(now % 1000000)}"
            )
            _transactions.update { listOf(feeTx) + it }
        } else {
            _freeGamesRemaining.update { (it - 1).coerceAtLeast(0) }
        }

        // Calculate reward
        val rawWin: Double
        val message: String
        when (gameType) {
            GameType.SPIN_AND_WIN -> {
                rawWin = spinSectorReward ?: 100.0
                message = if (rawWin > 0) "Congratulations! You won Rs. ${"%,.0f".format(rawWin)} on the Wheel!" else "Better luck next spin!"
            }
            GameType.TAP_TAP_COIN -> {
                val taps = tapCount ?: 0
                if (taps >= 20) {
                    rawWin = 50.0
                    message = "Great speed! You reached $taps taps and won Rs. 50!"
                } else {
                    rawWin = 0.0
                    message = "You reached $taps taps. Need 20+ taps to win Rs. 50. Try again!"
                }
            }
            GameType.NUMBER_GUESS -> {
                val guess = guessedNumber ?: 1
                val luckyNumber = ((System.currentTimeMillis() % 10) + 1).toInt()
                if (guess == luckyNumber) {
                    rawWin = 500.0 // 5x
                    message = "JACKPOT! Lucky number was $luckyNumber! 5X Win: Rs. 500!"
                } else if (kotlin.math.abs(guess - luckyNumber) == 1) {
                    rawWin = 100.0 // Consolation
                    message = "Close! Lucky number was $luckyNumber. Consolation win: Rs. 100!"
                } else {
                    rawWin = 0.0
                    message = "Lucky number was $luckyNumber. Your guess: $guess. Try again!"
                }
            }
        }

        // Apply daily cap protection
        val remainingCap = (dailyCap - _dailyGameEarnings.value).coerceAtLeast(0.0)
        val finalWin = minOf(rawWin, remainingCap)

        if (finalWin > 0) {
            val now = System.currentTimeMillis()
            val winTx = TransactionRecord(
                id = UUID.randomUUID().toString(),
                title = "Won in ${gameType.title}",
                amountLkr = finalWin,
                type = TransactionType.GAME_WINNING,
                status = TransactionStatus.COMPLETED,
                method = PaymentMethod.LANKA_QR,
                destination = "🎮 Game Wallet",
                timestamp = now,
                reference = "WIN-${(now % 1000000)}"
            )
            // Credit ONLY to Game Wallet!
            _gameWalletBalance.update { it + finalWin }
            _dailyGameEarnings.update { it + finalWin }
            _transactions.update { listOf(winTx) + it }
        }

        val result = GamePlayResult(
            gameType = gameType,
            winAmount = finalWin,
            feePaid = fee,
            isWin = finalWin > 0,
            message = message
        )
        return Result.success(result)
    }

    /**
     * Transfers funds from Game Wallet to Main Wallet.
     * If user has >= Rs. 1,000 in Game Wallet, qualifies for VIP Gamer bonus (+5% extra cash!).
     */
    fun transferGameToMain(amount: Double): Result<Double> {
        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("Please enter a valid transfer amount."))
        }
        if (_gameWalletBalance.value < amount) {
            return Result.failure(IllegalArgumentException("Insufficient Game Wallet balance. Available: Rs. ${"%,.0f".format(_gameWalletBalance.value)}"))
        }

        val isVip = _gameWalletBalance.value >= 1000.0
        val bonus = if (isVip) amount * 0.05 else 0.0
        val totalCreditedToMain = amount + bonus

        val now = System.currentTimeMillis()
        val tx = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = if (isVip) "Transferred to Main Wallet (+5% VIP Bonus)" else "Transferred to Main Wallet",
            amountLkr = totalCreditedToMain,
            type = TransactionType.GAME_TO_MAIN_TRANSFER,
            status = TransactionStatus.COMPLETED,
            method = PaymentMethod.BANK_TRANSFER,
            destination = "Main Investment Wallet",
            timestamp = now,
            reference = "TRF-${(now % 1000000)}"
        )

        _gameWalletBalance.update { it - amount }
        _availableCash.update { it + totalCreditedToMain }
        _transactions.update { listOf(tx) + it }

        return Result.success(totalCreditedToMain)
    }

    /**
     * Withdraws money from Game Wallet (Minimum Rs. 200)
     */
    fun withdrawGameEarnings(amountLkr: Double, method: PaymentMethod, destination: String): Result<TransactionRecord> {
        if (amountLkr < 200.0) {
            return Result.failure(IllegalArgumentException("Minimum Game Wallet withdrawal is Rs. 200"))
        }
        if (_gameWalletBalance.value < amountLkr) {
            return Result.failure(IllegalArgumentException("Insufficient Game Wallet balance. Available: Rs. ${"%,.0f".format(_gameWalletBalance.value)}"))
        }

        val now = System.currentTimeMillis()
        val tx = TransactionRecord(
            id = UUID.randomUUID().toString(),
            title = "Game Earnings Withdrawal (${method.displayName})",
            amountLkr = -amountLkr,
            type = TransactionType.GAME_WALLET_WITHDRAWAL,
            status = TransactionStatus.COMPLETED,
            method = method,
            destination = destination,
            timestamp = now,
            reference = "GMW-${(now % 1000000)}"
        )

        _gameWalletBalance.update { it - amountLkr }
        _transactions.update { listOf(tx) + it }

        return Result.success(tx)
    }

    fun clearUserDataOnLogout() {
        _profile.value = UserProfile(
            preferredLanguage = _profile.value.preferredLanguage,
            hasSelectedLanguage = _profile.value.hasSelectedLanguage,
            isDarkMode = _profile.value.isDarkMode
        )
        _availableCash.value = 0.0
        _gameWalletBalance.value = 0.0
        _dailyGameEarnings.value = 0.0
        _freeGamesRemaining.value = 3
        _activePlans.value = emptyList()
        _transactions.value = emptyList()
    }
}
