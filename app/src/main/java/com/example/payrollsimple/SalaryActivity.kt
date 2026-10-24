package com.example.payrollsimple

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import java.time.YearMonth

class SalaryActivity : BaseActivity() {
    private lateinit var monthInput: EditText
    private lateinit var list: ListView
    private var employeeId=0
    override fun onCreate(state: Bundle?) {
        super.onCreate(state);if(isFinishing)return
        setContentView(R.layout.activity_salary);title="Lương tháng"
        employeeId=if(isAdmin) 0 else db.employeeId(user.id)
        monthInput=findViewById(R.id.edMonth);list=findViewById(R.id.listSalaries)
        monthInput.setText(YearMonth.now().toString())
        findViewById<Button>(R.id.btnCalculate).apply {
            visibility=if(isAdmin)View.VISIBLE else View.GONE
            setOnClickListener { action {
                val month=selectedMonth()
                // Một transaction: có ca thiếu checkout thì không tính dở dang cho cả nhóm.
                val database=db.writableDatabase;database.beginTransaction()
                try { db.employees().forEach { db.calculateSalary(it.id,month) };database.setTransactionSuccessful() }
                finally { database.endTransaction() }
                refresh();message("Đã tính lương")
            } }
        }
        findViewById<Button>(R.id.btnLoad).setOnClickListener { action { refresh() } };refresh()
    }
    private fun selectedMonth(): YearMonth {
        val month=YearMonth.parse(monthInput.text.toString().trim())
        require(!month.isAfter(YearMonth.now())) { "Không tính lương tháng tương lai" };return month
    }
    private fun refresh() {
        val salaries=db.salaries(employeeId,selectedMonth())
        list.adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1,salaries)
        findViewById<TextView>(R.id.tvSummary).text="${salaries.size} bảng lương - Tổng: ${money(salaries.sumOf { it.amount })}"
        list.setOnItemClickListener { _,_,position,_ -> if(isAdmin) manageSalary(salaries[position]) }
    }
    private fun manageSalary(salary: Salary) {
        val options=arrayOf("Đánh dấu đã trả","Đánh dấu chưa trả","Xóa bảng lương")
        AlertDialog.Builder(this).setTitle(salary.name).setItems(options) { _,index -> action {
            if(index<2) { db.updateSalaryStatus(salary.id,if(index==0)"Đã trả" else "Chưa trả");refresh() }
            else AlertDialog.Builder(this).setMessage("Xóa bảng lương này? Có thể tính lại từ chấm công.")
                .setNegativeButton("Hủy",null).setPositiveButton("Xóa") { _,_ -> action { db.deleteSalary(salary.id);refresh() } }.show()
        } }.show()
    }
}
