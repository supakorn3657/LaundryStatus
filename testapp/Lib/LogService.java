package testapp.Lib;

/**
 * บันทึกประวัติการใช้งาน ยังไม่ผูกชนิดไฟล์ — FileLogService เป็นคนเขียนจริง
 */
public interface LogService {
    void log(String machineId, String action, String detail);
}
