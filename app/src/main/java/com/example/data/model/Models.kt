package com.example.data.model

import com.example.data.localization.AppLanguage

enum class PlanDuration(
    val months: Int,
    val profitPercent: Double,
    val title: String,
    val multiplierText: String
) {
    MONTHS_3(3, 50.0, "3 Months Plan", "1.5X (50% Profit)"),
    MONTHS_6(6, 120.0, "6 Months Plan", "2.2X (120% Profit)"),
    MONTHS_12(12, 300.0, "12 Months Plan", "4.0X (300% Profit)")
}

data class ActiveInvestmentPlan(
    val id: String,
    val depositAmount: Double,
    val planDuration: PlanDuration,
    val startDate: Long,
    val maturityDate: Long,
    val currentMonth: Int = 1,
    val isMatured: Boolean = false,
    val isVip: Boolean = false
) {
    val totalProfitPercent: Double
        get() = planDuration.profitPercent + if (isVip) 10.0 else 0.0

    val totalProfitAmount: Double
        get() = (depositAmount * totalProfitPercent) / 100.0

    val finalMaturityAmount: Double
        get() = depositAmount + totalProfitAmount

    /**
     * Calculates the growing value for the current month step
     */
    val currentGrowingValue: Double
        get() {
            if (isMatured) return finalMaturityAmount
            val monthlyProfit = totalProfitAmount / planDuration.months
            return depositAmount + (monthlyProfit * currentMonth)
        }
}

enum class TransactionType {
    DEPOSIT,
    PLAN_INVESTMENT,
    MATURITY_PAYOUT,
    EARLY_WITHDRAWAL,
    VIP_BONUS,
    GAME_WINNING,
    GAME_FEE,
    GAME_WALLET_WITHDRAWAL,
    GAME_TO_MAIN_TRANSFER
}

enum class GameType(val title: String, val subtitle: String, val baseRewardText: String) {
    SPIN_AND_WIN("SPIN & WIN", "Spin the Wheel & Win up to Rs 1000", "Up to Rs. 1,000"),
    TAP_TAP_COIN("TAP TAP COIN", "Tap Fast & Earn Rs 50", "Rs. 50 Win"),
    NUMBER_GUESS("NUMBER GUESS", "Guess the Number & Win 5x Your Money", "5x Win (Rs. 500)")
}

data class GamePlayResult(
    val gameType: GameType,
    val winAmount: Double,
    val feePaid: Double,
    val isWin: Boolean,
    val message: String,
    val vipBonusApplied: Boolean = false
)

enum class TransactionStatus {
    PENDING,
    APPROVED,
    COMPLETED
}

enum class PaymentMethod(val displayName: String) {
    BANK_TRANSFER("Sri Lankan Bank Transfer"),
    EZ_CASH("Dialog eZ Cash"),
    USDT("USDT (TRC-20)"),
    LANKA_QR("LankaQR / CEFTS")
}

data class TransactionRecord(
    val id: String,
    val title: String,
    val amountLkr: Double,
    val type: TransactionType,
    val status: TransactionStatus,
    val method: PaymentMethod,
    val destination: String,
    val timestamp: Long,
    val reference: String
)

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val username: String = "",
    val birthday: String = "",
    val email: String = "",
    val fullName: String = "",
    val phoneNumber: String = "", // Saved as "+94 77 123 4567"
    val profilePhotoUri: String? = null,
    val bankName: String = "",
    val accountNumber: String = "",
    val ezCashNumber: String = "",
    val usdtAddress: String = "",
    val preferredLanguage: AppLanguage = AppLanguage.ENGLISH,
    val hasSelectedLanguage: Boolean = false,
    val isKycVerified: Boolean = false,
    val isDarkMode: Boolean = false
) {
    val displayName: String
        get() = if (firstName.isNotBlank()) "$firstName $lastName" else if (fullName.isNotBlank()) fullName else if (username.isNotBlank()) username else "Investor"

    /**
     * Formats phone number as masked: +94 77 XXX X567
     */
    val maskedPhoneNumber: String
        get() {
            val digits = phoneNumber.filter { it.isDigit() }
            if (digits.length >= 11) { // e.g. 94 77 123 4567
                val country = "+94"
                val prefix = digits.substring(2, 4) // e.g. 77
                val last3 = digits.takeLast(3)
                return "$country $prefix ••• •$last3"
            }
            return if (phoneNumber.isNotBlank()) phoneNumber else "+94 7X ••• •XXX"
        }

    val avatarLetter: String
        get() = if (fullName.isNotBlank()) fullName.take(1).uppercase() else "U"
}
