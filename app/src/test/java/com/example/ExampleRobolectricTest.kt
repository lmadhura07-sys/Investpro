package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.localization.AppLanguage
import com.example.data.localization.Translations
import com.example.data.model.PaymentMethod
import com.example.data.model.PlanDuration
import com.example.data.repository.WalletRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("InvestPro", appName)
  }

  @Test
  fun `test phone number normalization to plus 94 format`() {
    val repository = WalletRepository()
    val formatted1 = repository.formatToPlus94("0771234567")
    assertEquals("+94 77 123 4567", formatted1)

    val formatted2 = repository.formatToPlus94("94712345678")
    assertEquals("+94 71 234 5678", formatted2)
  }

  @Test
  fun `test deposit enforces strict Rs 100 minimum`() {
    val repository = WalletRepository()
    // Under 100 should fail
    val failResult = repository.depositFunds(50.0, PaymentMethod.LANKA_QR)
    assertTrue(failResult.isFailure)

    // 100 or above should succeed
    val successResult = repository.depositFunds(100.0, PaymentMethod.LANKA_QR)
    assertTrue(successResult.isSuccess)
    assertEquals(100.0, repository.getAvailableCashStream().value, 0.01)
  }

  @Test
  fun `test high profit plan calculations and VIP bonus`() {
    val repository = WalletRepository()
    repository.depositFunds(150000.0, PaymentMethod.LANKA_QR)

    // Standard 6-month plan (120% profit) for 10,000 LKR
    val plan1Result = repository.investInPlan(10000.0, PlanDuration.MONTHS_6)
    assertTrue(plan1Result.isSuccess)
    val plan1 = plan1Result.getOrThrow()
    assertFalse(plan1.isVip)
    assertEquals(120.0, plan1.totalProfitPercent, 0.01)
    assertEquals(12000.0, plan1.totalProfitAmount, 0.01) // 120% of 10,000 = 12,000
    assertEquals(22000.0, plan1.finalMaturityAmount, 0.01) // 10,000 + 12,000 = 22,000

    // VIP Plan (>= 100,000 LKR) on 12-month plan (300% + 10% VIP = 310%)
    val plan2Result = repository.investInPlan(100000.0, PlanDuration.MONTHS_12)
    assertTrue(plan2Result.isSuccess)
    val plan2 = plan2Result.getOrThrow()
    assertTrue(plan2.isVip)
    assertEquals(310.0, plan2.totalProfitPercent, 0.01) // 300% + 10% = 310%
    assertEquals(310000.0, plan2.totalProfitAmount, 0.01)
    assertEquals(410000.0, plan2.finalMaturityAmount, 0.01)
  }

  @Test
  fun `test early withdrawal returns principal only without profit`() {
    val repository = WalletRepository()
    repository.depositFunds(5000.0, PaymentMethod.LANKA_QR)
    val plan = repository.investInPlan(5000.0, PlanDuration.MONTHS_3).getOrThrow()

    // Withdraw early before maturity
    val earlyResult = repository.withdrawEarlyPrincipalOnly(plan.id)
    assertTrue(earlyResult.isSuccess)
    assertEquals(5000.0, earlyResult.getOrThrow(), 0.01)
    assertEquals(5000.0, repository.getAvailableCashStream().value, 0.01)
    assertTrue(repository.getActivePlansStream().value.isEmpty())
  }

  @Test
  fun `test matured plan withdrawal returns capital plus full profit`() {
    val repository = WalletRepository()
    repository.depositFunds(1000.0, PaymentMethod.LANKA_QR)
    // 3 Months = 50% profit
    val plan = repository.investInPlan(1000.0, PlanDuration.MONTHS_3).getOrThrow()

    // Advance 3 months to mature
    repository.advancePlanMonth(plan.id)
    repository.advancePlanMonth(plan.id)
    repository.advancePlanMonth(plan.id)

    val maturedPlan = repository.getActivePlansStream().value.find { it.id == plan.id }!!
    assertTrue(maturedPlan.isMatured)

    val payoutResult = repository.withdrawMaturedPlan(plan.id)
    assertTrue(payoutResult.isSuccess)
    assertEquals(1500.0, payoutResult.getOrThrow(), 0.01) // 1000 + 500 = 1500
    assertEquals(1500.0, repository.getAvailableCashStream().value, 0.01)
  }

  @Test
  fun `test trilingual localization strings`() {
    val en = Translations.get(AppLanguage.ENGLISH)
    val si = Translations.get(AppLanguage.SINHALA)
    val ta = Translations.get(AppLanguage.TAMIL)

    assertEquals("InvestPro", en.appName)
    assertEquals("InvestPro", si.appName)
    assertEquals("InvestPro", ta.appName)

    assertEquals("Home", en.navHome)
    assertEquals("මුල් පිටුව", si.navHome)
    assertEquals("முகப்பு", ta.navHome)

    assertEquals("GAME", en.navGame)
    assertEquals("ක්‍රීඩා", si.navGame)
    assertEquals("விளையாட்டு", ta.navGame)
  }

  @Test
  fun `test separate game wallet ensures winnings never mix into main wallet directly`() {
    val repository = WalletRepository()
    repository.depositFunds(500.0, PaymentMethod.LANKA_QR)
    assertEquals(500.0, repository.getAvailableCashStream().value, 0.01)
    assertEquals(0.0, repository.getGameWalletBalanceStream().value, 0.01)

    // Play Spin & Win (Free game 1) winning Rs. 200
    val spinResult = repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 200.0)
    assertTrue(spinResult.isSuccess)
    val play = spinResult.getOrThrow()
    assertEquals(200.0, play.winAmount, 0.01)

    // Crucial rule: Main wallet must remain Rs. 500, game wallet gets Rs. 200
    assertEquals(500.0, repository.getAvailableCashStream().value, 0.01)
    assertEquals(200.0, repository.getGameWalletBalanceStream().value, 0.01)
    assertEquals(2, repository.getFreeGamesRemainingStream().value)
  }

  @Test
  fun `test game wallet transfer to main wallet with VIP bonus`() {
    val repository = WalletRepository()
    repository.depositFunds(100.0, PaymentMethod.LANKA_QR)

    // Simulate winning 1,000 in Game Wallet (qualifies for VIP Gamer bonus)
    repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 1000.0)
    assertEquals(1000.0, repository.getGameWalletBalanceStream().value, 0.01)

    // Transfer Rs. 1,000 from Game Wallet to Main Wallet
    // VIP Gamer bonus is +5% = +Rs. 50 extra credited to Main Wallet
    val transferResult = repository.transferGameToMain(1000.0)
    assertTrue(transferResult.isSuccess)
    assertEquals(1050.0, transferResult.getOrThrow(), 0.01) // 1000 + 50 = 1050

    // Game wallet is now 0, Main wallet has initial 100 + 1050 = 1150
    assertEquals(0.0, repository.getGameWalletBalanceStream().value, 0.01)
    assertEquals(1150.0, repository.getAvailableCashStream().value, 0.01)
  }

  @Test
  fun `test game wallet withdrawal enforces minimum Rs 200`() {
    val repository = WalletRepository()
    repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 500.0)
    assertEquals(500.0, repository.getGameWalletBalanceStream().value, 0.01)

    // Attempting withdrawal below Rs. 200 fails
    val failResult = repository.withdrawGameEarnings(100.0, PaymentMethod.EZ_CASH, "0771234567")
    assertTrue(failResult.isFailure)

    // Rs. 200 or above succeeds
    val successResult = repository.withdrawGameEarnings(200.0, PaymentMethod.EZ_CASH, "0771234567")
    assertTrue(successResult.isSuccess)
    assertEquals(300.0, repository.getGameWalletBalanceStream().value, 0.01)
  }

  @Test
  fun `test paid games deduct from main wallet after 3 free games`() {
    val repository = WalletRepository()
    repository.depositFunds(500.0, PaymentMethod.LANKA_QR)

    // 3 Free plays
    repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 50.0)
    repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 50.0)
    repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 50.0)

    assertEquals(0, repository.getFreeGamesRemainingStream().value)
    assertEquals(500.0, repository.getAvailableCashStream().value, 0.01) // Free plays didn't deduct cash

    // 4th game is a paid game (costs Rs. 100 from Main Wallet)
    val paidGame = repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 100.0)
    assertTrue(paidGame.isSuccess)
    assertEquals(100.0, paidGame.getOrThrow().feePaid, 0.01)
    assertEquals(400.0, repository.getAvailableCashStream().value, 0.01) // 500 - 100 fee
  }

  @Test
  fun `test dark mode toggle and state updates in profile`() {
    val repository = WalletRepository()
    repository.initializeUserSession("+94 77 123 4567", "Nimal Perera")

    // Default is light mode
    assertFalse(repository.getProfileStream().value.isDarkMode)

    // Switch to Dark Mode
    repository.toggleDarkMode(true)
    assertTrue(repository.getProfileStream().value.isDarkMode)

    // Switch back to Light Mode
    repository.toggleDarkMode(false)
    assertFalse(repository.getProfileStream().value.isDarkMode)
  }

  @Test
  fun `test strict user session isolation per plus 94 phone number`() {
    val repository = WalletRepository()
    repository.initializeUserSession("+94 77 123 4567", "Kasun")
    repository.depositFunds(1000.0, PaymentMethod.LANKA_QR)

    assertEquals("+94 77 123 4567", repository.getProfileStream().value.phoneNumber)
    assertEquals(1000.0, repository.getAvailableCashStream().value, 0.01)

    // On logout, clean user data is reset so next user cannot see Kasun's ledger
    repository.clearUserDataOnLogout()
    assertEquals(0.0, repository.getAvailableCashStream().value, 0.01)
    assertEquals("", repository.getProfileStream().value.phoneNumber)
    assertTrue(repository.getActivePlansStream().value.isEmpty())
  }

  @Test
  fun `test wallet transactions history captures deposits withdrawals and game earnings with status`() {
    val repository = WalletRepository()
    repository.initializeUserSession("+94 77 123 4567", "Kasun")

    // 1. Deposit
    val depResult = repository.depositFunds(1000.0, PaymentMethod.LANKA_QR)
    assertTrue(depResult.isSuccess)

    // 2. Game Win
    val gameResult = repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 250.0)
    assertTrue(gameResult.isSuccess)

    // 3. Game Withdrawal
    val withResult = repository.withdrawGameEarnings(200.0, PaymentMethod.EZ_CASH, "0771234567")
    assertTrue(withResult.isSuccess)

    val logs = repository.getTransactionLogs().value
    assertEquals(3, logs.size)

    val depositTx = logs.find { it.type == com.example.data.model.TransactionType.DEPOSIT }!!
    assertEquals(com.example.data.model.TransactionStatus.COMPLETED, depositTx.status)
    assertEquals(1000.0, depositTx.amountLkr, 0.01)

    val gameTx = logs.find { it.type == com.example.data.model.TransactionType.GAME_WINNING }!!
    assertEquals(com.example.data.model.TransactionStatus.COMPLETED, gameTx.status)
    assertEquals(250.0, gameTx.amountLkr, 0.01)

    val withdrawTx = logs.find { it.type == com.example.data.model.TransactionType.GAME_WALLET_WITHDRAWAL }!!
    assertEquals(com.example.data.model.TransactionStatus.COMPLETED, withdrawTx.status)
    assertEquals(-200.0, withdrawTx.amountLkr, 0.01)
  }

  @Test
  fun `test transaction filtering by category, status, and search query`() {
    val repository = WalletRepository()
    repository.initializeUserSession("+94 77 123 4567", "Kasun")

    // Setup transactions
    repository.depositFunds(1000.0, PaymentMethod.LANKA_QR)
    repository.playGame(com.example.data.model.GameType.SPIN_AND_WIN, spinSectorReward = 200.0)
    repository.withdrawGameEarnings(200.0, PaymentMethod.EZ_CASH, "0771234567")

    val allTx = repository.getTransactionLogs().value
    assertEquals(3, allTx.size)

    // Filter DEPOSITS only
    val deposits = allTx.filter { it.type == com.example.data.model.TransactionType.DEPOSIT }
    assertEquals(1, deposits.size)
    assertEquals(1000.0, deposits[0].amountLkr, 0.01)

    // Filter WITHDRAWALS only
    val withdrawals = allTx.filter { it.type == com.example.data.model.TransactionType.GAME_WALLET_WITHDRAWAL }
    assertEquals(1, withdrawals.size)
    assertEquals(-200.0, withdrawals[0].amountLkr, 0.01)

    // Filter GAME EARNINGS only
    val gameWins = allTx.filter { it.type == com.example.data.model.TransactionType.GAME_WINNING }
    assertEquals(1, gameWins.size)
    assertEquals(200.0, gameWins[0].amountLkr, 0.01)

    // Search query matching reference prefix
    val searchResults = allTx.filter { it.reference.contains("WIN") || it.title.contains("Spin", ignoreCase = true) }
    assertEquals(1, searchResults.size)
    assertEquals(com.example.data.model.TransactionType.GAME_WINNING, searchResults[0].type)
  }
}
