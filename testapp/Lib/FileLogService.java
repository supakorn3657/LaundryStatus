package testapp.Lib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** บันทึกประวัติลงไฟล์ เขียนต่อท้าย ไม่ทับของเก่า */
public class FileLogService implements LogService {
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String filePath;

    public FileLogService() {
        this("laundry_log.txt");
    }

    public FileLogService(String filePath) {
        this.filePath = filePath;
        File file = new File(this.filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("สร้างไฟล์ log ไม่ได้: " + e.getMessage());
        }
    }

    @Override
    public synchronized void log(String machineId, String action, String detail) {
        String line = String.format("[%s] | Machine: %s | Action: %s | Detail: %s",
                LocalDateTime.now().format(TIMESTAMP_FORMAT),
                machineId,
                action,
                detail);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));
            try {
                writer.write(line);
                writer.newLine();
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            System.err.println("บันทึก log ไม่ได้: " + e.getMessage());
        }
    }
}
