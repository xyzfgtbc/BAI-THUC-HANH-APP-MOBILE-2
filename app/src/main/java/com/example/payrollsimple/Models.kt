// Kiều Đức Nguyên: Data models cho Account, Employee, WorkLog, Salary
package com.example.payrollsimple

// Một đối tượng tương ứng với một dòng của bảng.
data class Account(val id: Int, val username: String, val role: String)
data class Employee(
    val id: Int = 0, val code: String, val fullname: String,
    val email: String, val phone: String, val hourlyRate: Double,
    val accountId: Int = 0, val username: String
) {
    override fun toString() = "$code - $fullname\n$email | $phone\nLương giờ: ${money(hourlyRate)} | Tài khoản: $username"
}
data class WorkLog(val id: Int, val name: String, val date: String,
                   val checkIn: String, val checkOut: String?, val hours: Double) {
    override fun toString() = "$name - $date\n$checkIn → ${checkOut ?: "Chưa checkout"} | ${"%.2f".format(hours)} giờ"
}
data class Salary(val id: Int, val name: String, val hours: Double,
                  val overtime: Double, val amount: Double, val status: String) {
    override fun toString() = "$name\nTổng: ${"%.2f".format(hours)} giờ | Tăng ca: ${"%.2f".format(overtime)} giờ\n${money(amount)} - $status"
}
fun money(value: Double) = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN")).format(value)
