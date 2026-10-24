package com.example.payrollsimple

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView

class EmployeeActivity : BaseActivity() {
    private lateinit var search: EditText
    private lateinit var list: ListView
    override fun onCreate(state: Bundle?) {
        super.onCreate(state);if(isFinishing)return
        if(!isAdmin) { finish();return }
        setContentView(R.layout.activity_employee);title="CRUD nhân viên"
        search=findViewById(R.id.edSearch);list=findViewById(R.id.listEmployees)
        findViewById<Button>(R.id.btnSearch).setOnClickListener { refresh() }
        findViewById<Button>(R.id.btnAdd).setOnClickListener { showForm(null) }
        refresh()
    }
    private fun refresh() {
        val employees=db.employees(search.text.toString().trim())
        list.adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1,employees)
        list.setOnItemClickListener { _,_,position,_ -> showForm(employees[position]) }
    }
    private fun showForm(old: Employee?) {
        val form=layoutInflater.inflate(R.layout.dialog_employee,null)
        val code=form.findViewById<EditText>(R.id.edCode)
        val name=form.findViewById<EditText>(R.id.edName)
        val email=form.findViewById<EditText>(R.id.edEmail)
        val phone=form.findViewById<EditText>(R.id.edPhone)
        val rate=form.findViewById<EditText>(R.id.edRate)
        val username=form.findViewById<EditText>(R.id.edAccount)
        val password=form.findViewById<EditText>(R.id.edNewPassword)
        if(old!=null) {
            code.setText(old.code);name.setText(old.fullname);email.setText(old.email)
            phone.setText(old.phone);rate.setText(old.hourlyRate.toString());username.setText(old.username)
            password.hint="Mật khẩu mới (trống = giữ nguyên)"
        }
        val dialog=AlertDialog.Builder(this).setTitle(if(old==null)"Thêm nhân viên" else "Xem / sửa nhân viên")
            .setView(form).setNegativeButton("Hủy",null).setPositiveButton("Lưu",null)
            .apply { if(old!=null) setNeutralButton("Xóa") { _,_ -> confirmDelete(old) } }.create()
        dialog.setOnShowListener {
            // Không đóng form nếu nhập sai; chỉ đóng sau khi lưu thành công.
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { action {
                val employee=Employee(old?.id ?: 0,code.text.toString().trim(),name.text.toString().trim(),
                    email.text.toString().trim(),phone.text.toString().trim(),
                    rate.text.toString().toDoubleOrNull() ?: 0.0,old?.accountId ?: 0,username.text.toString().trim())
                db.saveEmployee(employee,password.text.toString());dialog.dismiss();refresh();message("Đã lưu")
            } }
        }
        dialog.show()
    }
    private fun confirmDelete(e: Employee) {
        AlertDialog.Builder(this).setTitle("Xóa ${e.fullname}?")
            .setMessage("Xóa cả tài khoản, chấm công và lương của nhân viên này.")
            .setNegativeButton("Hủy",null).setPositiveButton("Xóa") { _,_ -> action { db.deleteEmployee(e);refresh() } }.show()
    }
}
