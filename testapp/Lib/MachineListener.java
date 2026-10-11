package testapp.Lib;

/**
 * จอต้องทำตามนี้ เมื่อตู้เปลี่ยนสถานะ หรือนาฬิกาเดิน
 */
public interface MachineListener {
    void onStatusChanged(WashingMachine machine);

    void onTimerTick(WashingMachine machine);

    /** นาฬิกาหลักของระบบ — จอขวาบนต้องโชว์ค่านี้ */
    void onClockTick(java.time.LocalTime now);
}
