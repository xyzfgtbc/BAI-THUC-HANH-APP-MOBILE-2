package com.example.payrollsimple

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import java.time.LocalTime
import java.time.YearMonth

class AttendanceActivity : BaseActivity() {
    private lateinit var monthInput: EditText
    private lateinit var list: ListView
    private var employeeId=0 // 0: Admin xem tất cả; nhân viên: chỉ lấy ID của mình.
    override fun onCreate(state: Bundle?) {
        super.onCreate(state);if(isFinishing)return
        setContentView(R.layout.activity_attendance);title="Chấm công / lịch sử"
        employeeId=if(isAdmin) 0 else db.employeeId(user.id)
        monthInput=findViewById(R.id.edMonth);list=findViewById(R.id.listLogs)
        monthInput.setText(YearMonth.now().toString())
        findViewById<View>(R.id.attendanceButtons).visibility=if(isAdmin) View.GONE else View.VISIBLE
        findViewById<Button>(R.id.btnCheckIn).setOnClickListener { action { db.checkIn(employeeId);refresh();message("Check-in thành công") } }
        findViewById<Button>(R.id.btnCheckOut).setOnClickListener { action { db.checkOut(employeeId);refresh();message("Check-out và tính lương thành công") } }
        findViewById<Button>(R.id.btnLoad).setOnClickListener { action { refresh() } }
        refresh()
    }
    private fun refresh() {
        val month=YearMonth.parse(monthInput.text.toString().trim())
        val logs=db.logs(employeeId,month)
        list.adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1,logs)
        val total=logs.sumOf { it.hours };val open=logs.count { it.checkOut==null }
        findViewById<TextView>(R.id.tvSummary).text="Tổng: %.2f giờ | %d ca chưa hoàn tất".format(total,open)
        list.setOnItemClickListener { _,_,position,_ -> if(isAdmin) editLog(logs[position]) }
    }
    private fun editLog(log: WorkLog) {
        val form=layoutInflater.inflate(R.layout.dialog_worklog,null)
        val input=form.findViewById<EditText>(R.id.edIn);val output=form.findViewById<EditText>(R.id.edOut)
        input.setText(log.checkIn);output.setText(log.checkOut ?: "")
        val dialog=AlertDialog.Builder(this).setTitle("${log.name} - ${log.date}").setView(form)
            .setNegativeButton("Hủy",null).setPositiveButton("Lưu",null).create()
        dialog.setOnShowListener { dialog.getButton(-1).setOnClickListener { action {
            db.finishLog(log.id,LocalTime.parse(input.text.toString()),LocalTime.parse(output.text.toString()))
            dialog.dismiss();refresh()
        } } };dialog.show()
    }
}
