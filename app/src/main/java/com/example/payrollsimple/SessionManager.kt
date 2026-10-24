package com.example.payrollsimple

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("login", Context.MODE_PRIVATE)
    val accountId get() = prefs.getInt("accountId", 0)
    fun save(account: Account) {
        prefs.edit().putInt("accountId", account.id)
            .putString("username", account.username).putString("role", account.role).apply()
    }
    fun logout() = prefs.edit().clear().apply()
}
