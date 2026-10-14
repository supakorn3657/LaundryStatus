package Lib;

import java.time.Clock;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * สมองกลางของระบบ — GUI เรียกเมธอดนี้เท่านั้น ห้ามแตะตู้ตรง ๆ
 * นาฬิกาขวาบนกับเวลาเสร็จโดยประมาณใช้ now ตัวเดียวกัน
 */
public class MachineController {
    private static final DateTimeFormatter FINISH_CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private final List<WashingMachine> machines;
    private final LogService logService;
    private final List<MachineListener> listeners;
    private final TimerThread timerThread;
    private final Clock clock;
    private LocalTime now;

    public MachineController(LogService logService) {
        this(logService, Clock.systemDefaultZone());
    }

    public MachineController(LogService logService, Clock clock) {
        this.logService = logService;
        this.clock = clock;
        this.now = LocalTime.now(clock);
        this.machines = new ArrayList<WashingMachine>();
        this.listeners = new ArrayList<MachineListener>();
        for (int i = 1; i <= 6; i++) {
            machines.add(new WashingMachine("M-" + String.format("%02d", i)));
        }
        this.timerThread = new TimerThread(this);
    }

    public LocalTime getNow() {
        return now;
    }

    /** ตู้เขียวเท่านั้น เลือกโหมดแล้วเริ่มซักทันที */
    public void startWash(String machineId, String roomNo, WashMode mode)
            throws InvalidOperationException {
        WashingMachine machine = requireMachine(machineId);
        machine.startWash(roomNo, mode, now);
        log("START_WASH", machineId + " room=" + roomNo + " mode=" + mode.name()
                + " finish=" + machine.getExpectedFinish().format(FINISH_CLOCK));
        fireStatus(machine);
    }

    public void collectClothes(String machineId) throws InvalidOperationException {
        WashingMachine machine = requireMachine(machineId);
        machine.collect();
        log("COLLECT", machineId);
        fireStatus(machine);
    }

    /** เทสบนจอ: ข้ามไปวินาทีสุดท้ายของสถานะปัจจุบัน ไม่ได้นอนรอ 15 หรือ 70 นาที */
    public void skipToExpireForTest(String machineId) throws InvalidOperationException {
        WashingMachine machine = requireMachine(machineId);
        if (machine.getStatus() == MachineStatus.AVAILABLE) {
            throw new InvalidOperationException(machineId + " ว่าง ไม่มีเวลาให้ข้าม");
        }
        machine.skipToLastSecondForTest();
        fireTick(machine);
    }

    public List<WashingMachine> getMachines() {
        return Collections.unmodifiableList(machines);
    }

    public void addListener(MachineListener listener) {
        listeners.add(listener);
        listener.onClockTick(now);
    }

    public void startTimer() {
        if (!timerThread.isAlive()) {
            timerThread.start();
        }
    }

    public void shutdown() {
        timerThread.stopTimer();
    }

    public void tickAll() {
        now = LocalTime.now(clock);
        fireClock();
        for (WashingMachine machine : machines) {
            boolean changed = machine.tick();
            fireTick(machine);
            if (changed) {
                log("TICK_EXPIRE", machine.getMachineId() + " -> " + machine.getStatus());
                fireStatus(machine);
            }
        }
    }

    public WashingMachine findMachine(String machineId) {
        for (WashingMachine m : machines) {
            if (m.getMachineId().equals(machineId)) {
                return m;
            }
        }
        return null;
    }

    private WashingMachine requireMachine(String machineId) throws InvalidOperationException {
        WashingMachine machine = findMachine(machineId);
        if (machine == null) {
            throw new InvalidOperationException("ไม่พบตู้ " + machineId);
        }
        return machine;
    }

    private void log(String action, String detail) {
        if (logService != null) {
            logService.log("CONTROLLER", action, detail);
        }
    }

    private void fireStatus(WashingMachine machine) {
        for (MachineListener listener : listeners) {
            listener.onStatusChanged(machine);
        }
    }

    private void fireTick(WashingMachine machine) {
        for (MachineListener listener : listeners) {
            listener.onTimerTick(machine);
        }
    }

    private void fireClock() {
        for (MachineListener listener : listeners) {
            listener.onClockTick(now);
        }
    }
}
