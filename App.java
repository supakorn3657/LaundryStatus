import Lib.InvalidOperationException;
import Lib.LogService;
import Lib.MachineController;
import Lib.MachineStatus;
import Lib.WashMode;
import Lib.WashingMachine;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * เทสแกนหลักหลังตัดจองช่วงเวลา
 * รัน: javac -encoding UTF-8 -d out App.java Lib\*.java GUI\*.java && java -cp out App
 */
public class App {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testSixMachines();
        testFastFinishAt1438();
        testModesHaveDifferentDurations();
        testFinishStaysWhileCountdownMoves();
        testBusyMachineRejectsSecondStart();
        testEmptyRoomFails();
        testNullModeFails();
        testUnknownMachineFails();
        testCollectWhileWashingFails();
        testWashThenPendingThenCollect();
        testPendingExpireBackToGreen();

        System.out.println();
        System.out.println("passed " + passed + " / failed " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static MachineController newController(int hour, int minute) {
        LogService silent = new LogService() {
            public void log(String machineId, String action, String detail) {
            }
        };
        ZoneId zone = ZoneId.of("Asia/Bangkok");
        Clock clock = Clock.fixed(
                LocalDateTime.of(2026, 9, 29, hour, minute, 0).atZone(zone).toInstant(),
                zone);
        return new MachineController(silent, clock);
    }

    private static void testSixMachines() {
        MachineController c = newController(14, 23);
        check("6 machines", c.getMachines().size() == 6);
        check("id M-01", c.findMachine("M-01") != null);
        check("id M-06", c.findMachine("M-06") != null);
        check("start AVAILABLE", c.findMachine("M-01").getStatus() == MachineStatus.AVAILABLE);
    }

    private static void testFastFinishAt1438() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-01", "101", WashMode.FAST);
            WashingMachine m = c.findMachine("M-01");
            check("fast -> IN_USE", m.getStatus() == MachineStatus.IN_USE);
            check("lock room 101", "101".equals(m.getCurrentRoomNo()));
            check("fast 15 min", m.getTimeRemaining() == 15 * 60);
            check("finish 14:38", LocalTime.of(14, 38).equals(m.getExpectedFinish()));
        } catch (InvalidOperationException e) {
            fail("fast start should succeed: " + e.getMessage());
        }
    }

    private static void testModesHaveDifferentDurations() {
        MachineController normal = newController(8, 0);
        MachineController heavy = newController(8, 0);
        try {
            normal.startWash("M-02", "102", WashMode.NORMAL);
            heavy.startWash("M-03", "103", WashMode.COMFORTER);
            check("normal 45 min", normal.findMachine("M-02").getTimeRemaining() == 45 * 60);
            check("comforter 70 min", heavy.findMachine("M-03").getTimeRemaining() == 70 * 60);
            check("normal finish 08:45",
                    LocalTime.of(8, 45).equals(normal.findMachine("M-02").getExpectedFinish()));
            check("comforter finish 09:10",
                    LocalTime.of(9, 10).equals(heavy.findMachine("M-03").getExpectedFinish()));
        } catch (InvalidOperationException e) {
            fail("mode durations failed: " + e.getMessage());
        }
    }

    private static void testFinishStaysWhileCountdownMoves() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-01", "101", WashMode.FAST);
            WashingMachine m = c.findMachine("M-01");
            LocalTime finish = m.getExpectedFinish();
            for (int i = 0; i < 5; i++) {
                c.tickAll();
            }
            check("finish unchanged", finish.equals(m.getExpectedFinish()));
            check("countdown minus 5s", m.getTimeRemaining() == WashMode.FAST.getSeconds() - 5);
        } catch (InvalidOperationException e) {
            fail("countdown test failed: " + e.getMessage());
        }
    }

    private static void testBusyMachineRejectsSecondStart() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-02", "102", WashMode.FAST);
            c.startWash("M-02", "103", WashMode.NORMAL);
            fail("busy machine must reject second start");
        } catch (InvalidOperationException e) {
            check("second start throws", true);
            check("room stays 102", "102".equals(c.findMachine("M-02").getCurrentRoomNo()));
        }
    }

    private static void testEmptyRoomFails() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-01", "  ", WashMode.FAST);
            fail("blank room must fail");
        } catch (InvalidOperationException e) {
            check("blank room throws", true);
            check("still available", c.findMachine("M-01").isAvailable());
        }
    }

    private static void testNullModeFails() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-01", "101", null);
            fail("null mode must fail");
        } catch (InvalidOperationException e) {
            check("null mode throws", true);
        }
    }

    private static void testUnknownMachineFails() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-99", "101", WashMode.FAST);
            fail("unknown machine must fail");
        } catch (InvalidOperationException e) {
            check("unknown machine throws", true);
        }
    }

    private static void testCollectWhileWashingFails() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-04", "201", WashMode.FAST);
            c.collectClothes("M-04");
            fail("collect during wash must fail");
        } catch (InvalidOperationException e) {
            check("collect during wash throws", true);
            check("still in use", c.findMachine("M-04").getStatus() == MachineStatus.IN_USE);
        }
    }

    private static void testWashThenPendingThenCollect() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-05", "301", WashMode.FAST);
            WashingMachine m = c.findMachine("M-05");
            int washSteps = m.getTimeRemaining();
            for (int i = 0; i < washSteps; i++) {
                c.tickAll();
            }
            check("wash done -> PENDING", m.getStatus() == MachineStatus.PENDING);
            check("collect 10 min", m.getTimeRemaining() == WashingMachine.PENDING_SECONDS);
            check("finish cleared", m.getExpectedFinish() == null);

            c.collectClothes("M-05");
            check("collect -> AVAILABLE", m.getStatus() == MachineStatus.AVAILABLE);
            check("room unlocked", m.getCurrentRoomNo() == null);
        } catch (InvalidOperationException e) {
            fail("wash-pending-collect failed: " + e.getMessage());
        }
    }

    private static void testPendingExpireBackToGreen() {
        MachineController c = newController(14, 23);
        try {
            c.startWash("M-06", "401", WashMode.FAST);
            WashingMachine m = c.findMachine("M-06");
            int steps = m.getTimeRemaining() + WashingMachine.PENDING_SECONDS;
            for (int i = 0; i < steps; i++) {
                c.tickAll();
            }
            check("pending expire -> AVAILABLE", m.getStatus() == MachineStatus.AVAILABLE);
            check("mode cleared", m.getCurrentMode() == null);
        } catch (InvalidOperationException e) {
            fail("pending expire failed: " + e.getMessage());
        }
    }

    private static void check(String name, boolean ok) {
        if (ok) {
            passed++;
            System.out.println("[PASS] " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }

    private static void fail(String name) {
        failed++;
        System.out.println("[FAIL] " + name);
    }
}
