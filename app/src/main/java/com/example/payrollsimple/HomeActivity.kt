// Kiều Đức Nguyên: HomeActivity xử lý giao diện trang chủ và điều hướng theo vai trò
package com.example.payrollsimple

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class HomeActivity : BaseActivity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state);if(isFinishing)return
        setContentView(R.layout.activity_home)
        findViewById<TextView>(R.id.tvWelcome).text="${user.username} - ${user.role}"
        findViewById<Button>(R.id.btnEmployees).apply {
            visibility=if(isAdmin) View.VISIBLE else View.GONE
            setOnClickListener { startActivity(Intent(this@HomeActivity,EmployeeActivity::class.java)) }
        }
        findViewById<Button>(R.id.btnAttendance).setOnClickListener { startActivity(Intent(this,AttendanceActivity::class.java)) }
        findViewById<Button>(R.id.btnSalary).setOnClickListener { startActivity(Intent(this,SalaryActivity::class.java)) }
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            session.logout();startActivity(Intent(this,LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK));finish()
        }
    }
}
