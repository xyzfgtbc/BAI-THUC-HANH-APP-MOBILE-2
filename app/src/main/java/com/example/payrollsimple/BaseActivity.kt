package com.example.payrollsimple

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import android.database.sqlite.SQLiteConstraintException

// Dùng chung cho các màn hình đã đăng nhập, tránh lặp code.
open class BaseActivity : Activity() {
    protected lateinit var db: DBHelper
    protected lateinit var session: SessionManager
    protected lateinit var user: Account
    protected val isAdmin get() = user.role == "Admin"
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        db=DBHelper(this); session=SessionManager(this)
        val account=db.account(session.accountId)
        if(account==null) { session.logout();startActivity(Intent(this,LoginActivity::class.java));finish();return }
        user=account
    }
    protected fun action(block: () -> Unit) {
        try { block() }
        catch(e: SQLiteConstraintException) { message("Mã nhân viên hoặc tên đăng nhập bị trùng") }
        catch(e: java.time.DateTimeException) { message("Sai định dạng: tháng yyyy-MM, giờ HH:mm:ss") }
        catch(e: IllegalArgumentException) { message(e.message ?: "Dữ liệu không hợp lệ") }
        catch(e: IllegalStateException) { message(e.message ?: "Không thực hiện được") }
    }
    protected fun message(text: String) { Toast.makeText(this,text,Toast.LENGTH_LONG).show() }
    override fun onDestroy() { if(::db.isInitialized) db.close();super.onDestroy() }
}
