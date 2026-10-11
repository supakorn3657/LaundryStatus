package testapp.Lib;

import java.time.LocalTime;

/**
 * ตู้ซักผ้า — เวลาซักมาจาก WashMode ตอนกดเริ่ม
 *
 * นาฬิกา timeRemaining นับเป็นวินาที เพราะ TimerThread จะ tick ทุก 1 วินาที
 * ก๊อปสำหรับทดสอบ: เหลืองรอเก็บผ้า 5 วินาที
 */
public class WashingMachine extends Machine {
    public static final int PENDING_SECONDS = 5;

    private int timeRemaining;
    private int cycleDuration;
    private String currentRoomNo;
    private WashMode currentMode;
    private LocalTime expectedFinish;

    public WashingMachine(String machineId) {
        super(machineId);
        this.timeRemaining = 0;
        this.cycleDuration = 0;
        this.currentRoomNo = null;
        this.currentMode = null;
        this.expectedFinish = null;
        checkRep();
    }

    /**
     * เขียว → แดง
     * expectedFinish = เวลาที่กด + นาทีของโหมด เช่น 14:23 + 15 นาที = 14:38
     */
    public void startWash(String roomNo, WashMode mode, LocalTime startedAt)
            throws InvalidOperationException {
        if (getStatus() != MachineStatus.AVAILABLE) {
            throw new InvalidOperationException(getMachineId() + " ไม่ว่าง เริ่มซักไม่ได้");
        }
        if (roomNo == null || roomNo.trim().isEmpty()) {
            throw new InvalidOperationException("ต้องมีเลขห้อง");
        }
        if (mode == null) {
            throw new InvalidOperationException("ต้องเลือกโหมดซัก");
        }
        if (startedAt == null) {
            throw new InvalidOperationException("ต้องมีเวลาเริ่ม");
        }
        this.currentRoomNo = roomNo.trim();
        this.currentMode = mode;
        this.cycleDuration = mode.getSeconds();
        this.timeRemaining = mode.getSeconds();
        this.expectedFinish = startedAt.plusSeconds(mode.getSeconds());
        setStatus(MachineStatus.IN_USE);
        checkRep();
    }

    /**
     * เรียกทุก 1 วินาที
     * @return true ถ้าสถานะเปลี่ยนในรอบนี้ (หมดเวลา)
     */
    public boolean tick() {
        if (getStatus() == MachineStatus.AVAILABLE) {
            return false;
        }
        if (timeRemaining <= 0) {
            return expire();
        }
        timeRemaining--;
        if (timeRemaining <= 0) {
            return expire();
        }
        return false;
    }

    /** เหลือง กดเอาผ้าออก → เขียว */
    public void collect() throws InvalidOperationException {
        if (getStatus() != MachineStatus.PENDING) {
            throw new InvalidOperationException(getMachineId() + " ยังไม่ใช่ PENDING เก็บผ้าไม่ได้");
        }
        release();
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public int getCycleDuration() {
        return cycleDuration;
    }

    public String getCurrentRoomNo() {
        return currentRoomNo;
    }

    public WashMode getCurrentMode() {
        return currentMode;
    }

    /** เวลาเสร็จบนนาฬิกา ตอนกำลังซัก มีค่า หมดรอบแล้วเป็น null */
    public LocalTime getExpectedFinish() {
        return expectedFinish;
    }

    /** หมดเวลาแดง/เหลือง */
    private boolean expire() {
        MachineStatus before = getStatus();
        if (before == MachineStatus.IN_USE) {
            expectedFinish = null;
            timeRemaining = PENDING_SECONDS;
            setStatus(MachineStatus.PENDING);
            checkRep();
            return true;
        }
        if (before == MachineStatus.PENDING) {
            release();
            return true;
        }
        return false;
    }

    private void release() {
        currentRoomNo = null;
        currentMode = null;
        expectedFinish = null;
        timeRemaining = 0;
        cycleDuration = 0;
        setStatus(MachineStatus.AVAILABLE);
        checkRep();
    }

    /** ใช้ตอนเดโมหรือเทส ไม่ต้องรอจนครบนาทีจริง เหลือ 1 วินาทีแล้วปล่อยให้ tick ปกติเปลี่ยนสถานะ */
    public void skipToLastSecondForTest() {
        if (getStatus() == MachineStatus.AVAILABLE || timeRemaining <= 1) {
            return;
        }
        timeRemaining = 1;
    }

    private void checkRep() {
        if (timeRemaining < 0) {
            throw new RuntimeException("timeRemaining ห้ามติดลบ");
        }
        if (getStatus() == MachineStatus.AVAILABLE
                && (currentRoomNo != null || currentMode != null || expectedFinish != null)) {
            throw new RuntimeException("ตู้เขียวต้องไม่มีห้อง โหมด หรือเวลาเสร็จค้างอยู่");
        }
        if (getStatus() == MachineStatus.IN_USE
                && (currentRoomNo == null || currentMode == null || expectedFinish == null)) {
            throw new RuntimeException("ตู้แดงต้องมีห้อง โหมด และเวลาเสร็จ");
        }
    }
}
