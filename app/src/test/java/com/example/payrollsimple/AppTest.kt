package com.example.payrollsimple

import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28])
class AppTest {
    private lateinit var context: Context
    private lateinit var db: DBHelper
    @Before fun prepare() {
        context=RuntimeEnvironment.getApplication()
        context.deleteDatabase("payroll_simple.db")
        SessionManager(context).logout();db=DBHelper(context);db.writableDatabase
    }
    @After fun close() { db.close();context.deleteDatabase("payroll_simple.db");SessionManager(context).logout() }
    private fun employee() = Employee(code="NV02",fullname="Trần Bình",email="binh@example.com",phone="0987654321",hourlyRate=60000.0,username="nv02")
    private fun fails(block: ()->Unit) {
        try { block();fail("Phải từ chối dữ liệu sai") } catch(e: IllegalArgumentException) { } catch(e: IllegalStateException) { }
    }
    @Test fun loginAndSession() {
        assertNull(db.login("admin","wrong"));assertNull(db.login("' OR 1=1 --","123456"))
        val a=db.login("admin","123456")!!;assertEquals("Admin",a.role)
        SessionManager(context).save(a);assertEquals(a.id,SessionManager(context).accountId)
        SessionManager(context).logout();assertEquals(0,SessionManager(context).accountId)
    }
    @Test fun crudEmployeeAndAccount() {
        db.saveEmployee(employee(),"123456")
        val saved=db.employees("NV02").single();assertNotNull(db.login("nv02","123456"))
        db.saveEmployee(saved.copy(fullname="Tên mới",hourlyRate=70000.0),"")
        assertEquals("Tên mới",db.employees("NV02").single().fullname)
        assertEquals(70000.0,db.employees("NV02").single().hourlyRate,0.001)
        assertEquals(1,db.employees("Tên mới").size)
        db.deleteEmployee(saved);assertTrue(db.employees("NV02").isEmpty());assertNull(db.login("nv02","123456"))
    }
    @Test fun duplicateCodeRollsBackAccountCreation() {
        try { db.saveEmployee(employee().copy(code="NV01"),"123456");fail() }
        catch(e: android.database.sqlite.SQLiteConstraintException) { }
        assertNull(db.login("nv02","123456"));assertEquals(1,db.employees().size)
    }
    @Test fun validateEmployee() {
        fails { db.saveEmployee(employee().copy(hourlyRate=-1.0),"123456") }
        fails { db.saveEmployee(employee().copy(email="wrong"),"123456") }
        fails { db.saveEmployee(employee(),"12") }
    }
    @Test fun attendanceAndAutomaticSalary() {
        val d=LocalDate.now().minusDays(1)
        db.checkIn(1,d.atTime(8,0));fails { db.checkIn(1,d.atTime(9,0)) }
        fails { db.checkOut(1,d.atTime(7,0)) };db.checkOut(1,d.atTime(17,0))
        val salary=db.salaries(1,YearMonth.from(d)).single()
        // Tháng trước còn một ca mẫu 9 giờ nếu test đúng ngày đầu tháng.
        val count=db.logs(1,YearMonth.from(d)).size
        assertEquals(9.0*count,salary.hours,0.0001);assertEquals(1.0*count,salary.overtime,0.0001)
        assertEquals(475000.0*count,salary.amount,0.01)
        fails { db.checkIn(1,d.atTime(18,0)) }
    }
    @Test fun incompleteShiftBlocksPayrollAndNextCheckin() {
        val d=LocalDate.now().minusDays(1);db.checkIn(1,d.atTime(8,0))
        fails { db.checkIn(1,LocalDate.now().atTime(8,0)) }
        fails { db.calculateSalary(1,YearMonth.from(d)) }
        val log=db.logs(1,YearMonth.from(d)).first()
        db.finishLog(log.id,LocalTime.of(8,0),LocalTime.of(16,0))
        assertTrue(db.logs(1,YearMonth.from(d)).none { it.checkOut==null })
    }
    @Test fun salaryCalculationStatusAndDelete() {
        val month=YearMonth.now().minusMonths(1);db.calculateSalary(1,month)
        var s=db.salaries(1,month).single();assertEquals(475000.0,s.amount,0.01)
        db.updateSalaryStatus(s.id,"Đã trả");db.calculateSalary(1,month)
        s=db.salaries(1,month).single();assertEquals("Đã trả",s.status)
        db.deleteSalary(s.id);assertTrue(db.salaries(1,month).isEmpty())
    }
    @Test fun deleteEmployeeCascadesAttendanceAndSalary() {
        val month=YearMonth.now().minusMonths(1);db.calculateSalary(1,month)
        db.deleteEmployee(db.employees().single())
        assertTrue(db.logs(0,month).isEmpty());assertTrue(db.salaries(0,month).isEmpty())
    }
    @Test fun xmlScreensInflateAndEmployeeIsScoped() {
        // Thêm nhân viên thứ hai để kiểm tra màn hình không lấy dữ liệu của người khác.
        db.saveEmployee(employee(),"123456")
        SessionManager(context).save(db.login("nv02","123456")!!)
        Robolectric.buildActivity(AttendanceActivity::class.java).setup().use {
            val a=it.get();a.findViewById<EditText>(R.id.edMonth).setText(YearMonth.now().minusMonths(1).toString())
            a.findViewById<Button>(R.id.btnLoad).performClick()
            assertEquals(0,a.findViewById<ListView>(R.id.listLogs).adapter.count)
        }
        Robolectric.buildActivity(SalaryActivity::class.java).setup().use {
            assertEquals(android.view.View.GONE,it.get().findViewById<Button>(R.id.btnCalculate).visibility)
        }
        SessionManager(context).save(db.login("admin","123456")!!)
        Robolectric.buildActivity(EmployeeActivity::class.java).setup().use {
            val a=it.get();assertEquals(2,a.findViewById<ListView>(R.id.listEmployees).adapter.count)
            a.findViewById<Button>(R.id.btnAdd).performClick()
            assertTrue(org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog().isShowing)
        }
    }
}
