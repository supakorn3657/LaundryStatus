package testapp.Lib;

/** ใช้ตอนกดผิดจังหวะ เช่น จองตู้ที่ไม่เขียว */
public class InvalidOperationException extends Exception {
    public InvalidOperationException(String message) {
        super(message);
    }
}
