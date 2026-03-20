package com.example.babyparenting.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Subscription manager — NO free trial.
 *
 * Flow:
 *  1. Pehli milestone tap → Paywall dikhao
 *  2. ₹1 pay karo → 30 din access milta hai
 *  3. 30 din baad → Phir Paywall → Phir ₹1
 */
class SubscriptionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("subscription_prefs", Context.MODE_PRIVATE)

    // ── Subscription ──────────────────────────────────────────────────────────

    /** Call this after successful Razorpay payment */
    fun activateSubscription(razorpayPaymentId: String) {
        val validUntil = System.currentTimeMillis() + SUBSCRIPTION_DURATION_MS
        prefs.edit()
            .putBoolean(KEY_SUBSCRIBED, true)
            .putString(KEY_PAYMENT_ID, razorpayPaymentId)
            .putLong(KEY_VALID_UNTIL, validUntil)
            .apply()
    }

    fun isSubscriptionActive(): Boolean {
        val subscribed = prefs.getBoolean(KEY_SUBSCRIBED, false)
        val validUntil = prefs.getLong(KEY_VALID_UNTIL, 0L)
        return subscribed && System.currentTimeMillis() < validUntil
    }

    /** Days remaining in current subscription period */
    fun daysRemaining(): Int {
        val validUntil = prefs.getLong(KEY_VALID_UNTIL, 0L)
        val remaining  = validUntil - System.currentTimeMillis()
        return if (remaining <= 0) 0 else (remaining / MS_PER_DAY).toInt()
    }

    fun getLastPaymentId(): String? = prefs.getString(KEY_PAYMENT_ID, null)

    /** Main gate — advice screen uses this */
    fun canAccessAdvice(): Boolean = isSubscriptionActive()

    fun getStatus(): SubscriptionStatus = when {
        isSubscriptionActive() -> SubscriptionStatus.SUBSCRIBED
        else                   -> SubscriptionStatus.NOT_SUBSCRIBED
    }

    /** For testing — reset everything */
    fun resetForTesting() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_SUBSCRIBED  = "is_subscribed"
        private const val KEY_PAYMENT_ID  = "razorpay_payment_id"
        private const val KEY_VALID_UNTIL = "subscription_valid_until_ms"

        private const val SUBSCRIPTION_DURATION_MS = 30L * 24 * 60 * 60 * 1000  // 30 days
        private const val MS_PER_DAY               = 24L * 60 * 60 * 1000
    }
}

enum class SubscriptionStatus {
    NOT_SUBSCRIBED,  // Never paid — show paywall
    SUBSCRIBED       // Active paid subscription
}