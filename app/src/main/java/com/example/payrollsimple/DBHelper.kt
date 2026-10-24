package com.example.payrollsimple

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.Duration
import java.time.format.DateTimeFormatter

class DBHelper(context: Context) : SQLiteOpenHelper(context, "payroll_simple.db", null, 1) {
    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE Account(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, password TEXT NOT NULL, role TEXT NOT NULL)")
        db.execSQL("CREATE TABLE Employee(id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT UNIQUE NOT NULL, fullname TEXT NOT NULL, email TEXT NOT NULL, phone TEXT NOT NULL, hourlyRate REAL NOT NULL CHECK(hourlyRate>0), accountId INTEGER UNIQUE REFERENCES Account(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE WorkLog(id INTEGER PRIMARY KEY AUTOINCREMENT, employeeId INTEGER REFERENCES Employee(id) ON DELETE CASCADE, workDate TEXT NOT NULL, checkInTime TEXT NOT NULL, checkOutTime TEXT, totalHours REAL DEFAULT 0, status TEXT NOT NULL, UNIQUE(employeeId,workDate))")
        db.execSQL("CREATE TABLE Salary(id INTEGER PRIMARY KEY AUTOINCREMENT, employeeId INTEGER REFERENCES Employee(id) ON DELETE CASCADE, month INTEGER NOT NULL, year INTEGER NOT NULL, totalHours REAL, overtimeHours REAL, totalSalary REAL, status TEXT NOT NULL, UNIQUE(employeeId,month,year))")
        // Mật khẩu chữ thường chỉ dành cho dữ liệu giả trong bài thực hành, giống mẫu.
        db.execSQL("INSERT INTO Account(username,password,role) VALUES('admin','123456','Admin'),('nv01','123456','Employee')")
        db.execSQL("INSERT INTO Employee(code,fullname,email,phone,hourlyRate,accountId) VALUES('NV01','Nguyễn Văn An','an@example.com','0912345678',50000,2)")
        val date = LocalDate.now().minusMonths(1).withDayOfMonth(1)
        db.execSQL("INSERT INTO WorkLog(employeeId,workDate,checkInTime,checkOutTime,totalHours,status) VALUES(1,?,'08:00:00','17:00:00',9,'Complete')", arrayOf(date.toString()))
    }
    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) { /* Bản thực hành chỉ có version 1. */ }

    // PHẦN 1: đăng nhập và tìm nhân viên ứng với tài khoản hiện tại.
    fun login(username: String, password: String): Account? {
        readableDatabase.rawQuery("SELECT id,username,role FROM Account WHERE username=? AND password=?", arrayOf(username.trim(), password)).use {
            return if (it.moveToFirst()) Account(it.getInt(0), it.getString(1), it.getString(2)) else null
        }
    }
    fun account(id: Int): Account? {
        readableDatabase.rawQuery("SELECT id,username,role FROM Account WHERE id=?", arrayOf("$id")).use {
            return if (it.moveToFirst()) Account(it.getInt(0), it.getString(1), it.getString(2)) else null
        }
    }
    fun employeeId(accountId: Int): Int {
        readableDatabase.rawQuery("SELECT id FROM Employee WHERE accountId=?", arrayOf("$accountId")).use {
            check(it.moveToFirst()) { "Không có hồ sơ nhân viên" }; return it.getInt(0)
        }
    }

    // PHẦN 2: CRUD nhân viên. Thêm/sửa Account và Employee cùng một transaction.
    fun employees(keyword: String = ""): List<Employee> {
        val list = mutableListOf<Employee>()
        readableDatabase.rawQuery("SELECT e.id,e.code,e.fullname,e.email,e.phone,e.hourlyRate,e.accountId,a.username FROM Employee e JOIN Account a ON e.accountId=a.id WHERE e.code LIKE ? OR e.fullname LIKE ? ORDER BY e.id DESC", arrayOf("%$keyword%", "%$keyword%")).use {
            while (it.moveToNext()) list.add(Employee(it.getInt(0),it.getString(1),it.getString(2),it.getString(3),it.getString(4),it.getDouble(5),it.getInt(6),it.getString(7)))
        }
        return list
    }
    fun saveEmployee(e: Employee, password: String) {
        require(e.code.isNotBlank() && e.fullname.isNotBlank() && e.username.isNotBlank()) { "Nhập mã, họ tên và tài khoản" }
        require(android.util.Patterns.EMAIL_ADDRESS.matcher(e.email).matches()) { "Email không hợp lệ" }
        require(e.phone.matches(Regex("\\+?[0-9]{9,15}"))) { "Điện thoại cần 9–15 chữ số" }
        require(e.hourlyRate.isFinite() && e.hourlyRate > 0) { "Lương giờ phải lớn hơn 0" }
        require((e.id != 0 && password.isEmpty()) || password.length >= 6) { "Mật khẩu ít nhất 6 ký tự" }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val a = ContentValues().apply { put("username", e.username); if (password.isNotEmpty()) put("password", password) }
            val accountId = if (e.id == 0) {
                a.put("role", "Employee"); db.insertOrThrow("Account", null, a).toInt()
            } else {
                check(db.update("Account", a, "id=?", arrayOf("${e.accountId}")) == 1) { "Không tìm thấy tài khoản" }; e.accountId
            }
            val v = ContentValues().apply {
                put("code",e.code); put("fullname",e.fullname); put("email",e.email); put("phone",e.phone)
                put("hourlyRate",e.hourlyRate); put("accountId",accountId)
            }
            if (e.id == 0) db.insertOrThrow("Employee",null,v) else {
                check(db.update("Employee",v,"id=?",arrayOf("${e.id}")) == 1) { "Không tìm thấy nhân viên" }
                db.delete("Salary", "employeeId=? AND status='Chưa trả'", arrayOf("${e.id}"))
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    fun deleteEmployee(e: Employee) {
        check(writableDatabase.delete("Account","id=?",arrayOf("${e.accountId}")) == 1) { "Không tìm thấy nhân viên" }
    }

    // PHẦN 3: chấm công, lịch sử và tính lương.
    fun logs(employeeId: Int, month: YearMonth): List<WorkLog> {
        val list = mutableListOf<WorkLog>()
        readableDatabase.rawQuery("SELECT w.id,e.fullname,w.workDate,w.checkInTime,w.checkOutTime,w.totalHours FROM WorkLog w JOIN Employee e ON e.id=w.employeeId WHERE (?=0 OR w.employeeId=?) AND substr(w.workDate,1,7)=? ORDER BY w.workDate DESC,w.id DESC", arrayOf("$employeeId","$employeeId",month.toString())).use {
            while(it.moveToNext()) list.add(WorkLog(it.getInt(0),it.getString(1),it.getString(2),it.getString(3),it.getString(4),it.getDouble(5)))
        }
        return list
    }
    fun checkIn(employeeId: Int, now: LocalDateTime = LocalDateTime.now()) {
        readableDatabase.rawQuery("SELECT id FROM WorkLog WHERE employeeId=? AND (workDate=? OR status='Open')",arrayOf("$employeeId",now.toLocalDate().toString())).use {
            check(!it.moveToFirst()) { "Đã chấm công hôm nay hoặc còn ca chưa checkout" }
        }
        val v = ContentValues().apply { put("employeeId",employeeId); put("workDate",now.toLocalDate().toString());put("checkInTime",now.toLocalTime().format(TIME));put("status","Open") }
        writableDatabase.insertOrThrow("WorkLog",null,v)
        writableDatabase.delete("Salary","employeeId=? AND month=? AND year=? AND status='Chưa trả'", arrayOf("$employeeId","${now.monthValue}","${now.year}"))
    }
    fun checkOut(employeeId: Int, now: LocalDateTime = LocalDateTime.now()) {
        readableDatabase.rawQuery("SELECT id,checkInTime FROM WorkLog WHERE employeeId=? AND workDate=? AND status='Open'", arrayOf("$employeeId",now.toLocalDate().toString())).use {
            check(it.moveToFirst()) { "Chưa check-in hôm nay. Ca cũ cần Admin sửa giờ ra" }
            finishLog(it.getInt(0), LocalTime.parse(it.getString(1)), now.toLocalTime())
        }
    }
    fun finishLog(id: Int, checkIn: LocalTime, checkOut: LocalTime) {
        val seconds = Duration.between(checkIn.withNano(0),checkOut.withNano(0)).seconds
        require(seconds > 0) { "Giờ ra phải sau giờ vào, cùng ngày" }
        val db = writableDatabase
        db.beginTransaction()
        try {
            var employee: Int; var month: YearMonth
            db.rawQuery("SELECT employeeId,workDate FROM WorkLog WHERE id=?",arrayOf("$id")).use {
                check(it.moveToFirst()) { "Không tìm thấy ca" }; employee=it.getInt(0); month=YearMonth.from(LocalDate.parse(it.getString(1)))
                val date=LocalDate.parse(it.getString(1))
                require(!date.isAfter(LocalDate.now()) && (date!=LocalDate.now() || !checkOut.isAfter(LocalTime.now()))) { "Không nhập giờ tương lai" }
            }
            val v=ContentValues().apply { put("checkInTime",checkIn.format(TIME));put("checkOutTime",checkOut.format(TIME));put("totalHours",seconds/3600.0);put("status","Complete") }
            db.update("WorkLog",v,"id=?",arrayOf("$id"))
            calculateSalary(employee,month)
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    fun calculateSalary(employeeId: Int, month: YearMonth) {
        val records=logs(employeeId,month)
        check(records.none { it.checkOut==null }) { "Còn ca chưa checkout, chưa thể tính lương" }
        val rate=employees().first { it.id==employeeId }.hourlyRate
        val total=records.sumOf { it.hours }; val overtime=records.sumOf { maxOf(0.0,it.hours-8) }
        val salary=kotlin.math.round((total-overtime)*rate + overtime*rate*1.5)
        val where="employeeId=? AND month=? AND year=?"; val args=arrayOf("$employeeId","${month.monthValue}","${month.year}")
        val v=ContentValues().apply { put("totalHours",total);put("overtimeHours",overtime);put("totalSalary",salary) }
        // Tính lại chỉ cập nhật số liệu; giữ nguyên trạng thái mà Admin đã đặt.
        if(writableDatabase.update("Salary",v,where,args)==0) {
            v.put("employeeId",employeeId);v.put("month",month.monthValue);v.put("year",month.year);v.put("status","Chưa trả")
            writableDatabase.insertOrThrow("Salary",null,v)
        }
    }
    fun salaries(employeeId: Int, month: YearMonth): List<Salary> {
        val list=mutableListOf<Salary>()
        readableDatabase.rawQuery("SELECT s.id,e.fullname,s.totalHours,s.overtimeHours,s.totalSalary,s.status FROM Salary s JOIN Employee e ON s.employeeId=e.id WHERE (?=0 OR s.employeeId=?) AND s.month=? AND s.year=?",arrayOf("$employeeId","$employeeId","${month.monthValue}","${month.year}")).use {
            while(it.moveToNext()) list.add(Salary(it.getInt(0),it.getString(1),it.getDouble(2),it.getDouble(3),it.getDouble(4),it.getString(5)))
        }
        return list
    }
    fun updateSalaryStatus(id: Int, status: String) {
        require(status in listOf("Chưa trả","Đã trả"))
        writableDatabase.update("Salary",ContentValues().apply { put("status",status) },"id=?",arrayOf("$id"))
    }
    fun deleteSalary(id: Int) { writableDatabase.delete("Salary","id=?",arrayOf("$id")) }
    companion object { val TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss") }
}
