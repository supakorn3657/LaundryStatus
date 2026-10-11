package testapp.Lib;

/**
 * คลาสมาแม่ของตู้ (abstract = new Machine() ไม่ได้ ต้องใช้ลูก)
 * เก็บของกลาง: รหัสตู้ + สถานะสี
 */
public abstract class Machine {
    private final String machineId;
    private MachineStatus status;

    public Machine(String machineId) {
        this.machineId = machineId;
        this.status = MachineStatus.AVAILABLE;
        checkRep();
    }

    public String getMachineId() {
        return machineId;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public boolean isAvailable() {
        return status == MachineStatus.AVAILABLE;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
        checkRep();
    }

    /** ของในกล่องต้องมีรหัสตู้ และสถานะต้องไม่เป็น null */
    private void checkRep() {
        if (machineId == null || machineId.isEmpty()) {
            throw new RuntimeException("machineId ห้ามว่าง");
        }
        if (status == null) {
            throw new RuntimeException("status ห้าม null");
        }
    }
}
