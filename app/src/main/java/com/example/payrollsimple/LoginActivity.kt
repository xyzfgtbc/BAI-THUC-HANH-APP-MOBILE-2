package com.example.payrollsimple

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : Activity() {
    private lateinit var db: DBHelper
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        db=DBHelper(this)
        val session=SessionManager(this)
        if(db.account(session.accountId)!=null) { openHome();return }
        setContentView(R.layout.activity_login)
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val username=findViewById<EditText>(R.id.edUsername).text.toString()
            val password=findViewById<EditText>(R.id.edPassword).text.toString()
            val account=db.login(username,password)
            if(account==null) Toast.makeText(this,"Sai tài khoản hoặc mật khẩu",Toast.LENGTH_SHORT).show()
            else { session.save(account);openHome() }
        }
    }
    private fun openHome() { startActivity(Intent(this,HomeActivity::class.java));finish() }
    override fun onDestroy() { db.close();super.onDestroy() }
}
