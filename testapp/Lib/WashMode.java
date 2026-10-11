package testapp.Lib;

/**
 * โหมดซัก — เวลาผูกกับโหมด ห้ามให้ผู้ใช้เลือกนาทีเอง
 *
 * ตัวเลขนี้เป็นค่าที่กลุ่มกำหนดไว้สำหรับเดโม ไม่ได้วัดจากเครื่องจริง
 * ก๊อปสำหรับทดสอบ: ปั่นเร็ว 10 วินาที | ปั่นทั่วไป 15 วินาที | ปั่นผ้านวม 20 วินาที
 */
public enum WashMode {
    NORMAL("ปั่นทั่วไป", 15),
    FAST("ปั่นเร็ว", 10),
    COMFORTER("ปั่นผ้านวม", 20);

    private final String label;
    private final int seconds;

    WashMode(String label, int seconds) {
        this.label = label;
        this.seconds = seconds;
    }

    public String getLabel() {
        return label;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMinutes() {
        return seconds / 60;
    }
}
