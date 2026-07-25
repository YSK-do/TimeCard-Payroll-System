import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class TestRunner {
    private int passed;
    
    public static void main(String[] args) throws IOException {
        new TestRunner().run();
    }

    private void run() throws IOException {
        testTimeConversion();
        testPayrollCalculation();
        testWorkDateResolution();
        testRecordSaveUpdateAndSummary();
        testCsvValidation();
        System.out.println("TestRunner: " + passed + " tests passed");
    }

    private void testTimeConversion() {
        assertEquals(570, new MyTime("９：３０").toTotalMinutes());
        assertEquals(90, new MyTime("10:30").diff(new MyTime("9:00")));
        assertThrows(() -> new MyTime("9:60"));
        pass("time conversion");
    }

    private void testPayrollCalculation() {
        Config config = new Config("テスト太郎", 1200, 480, 60);
        WorkRecord record = new PayrollService().calculate(
                "2026-07-01", config, new MyTime("9:00"), new MyTime("19:00"));

        assertEquals(540, record.getWorkMinutes());
        assertEquals(60, record.getOvertimeMinutes());
        assertEquals(10800, record.getSalary());
        assertEquals(300, record.getOvertimePay());
        assertThrows(() -> new PayrollService().calculate(
                "2026-07-01", config, new MyTime("18:00"), new MyTime("9:00")));
        pass("payroll calculation");
    }

    private void testWorkDateResolution() {
        WorkDateResolver resolver = new WorkDateResolver();
        LocalDate today = LocalDate.of(2026, 7, 25);

        assertEquals("2026-07-01", resolver.resolve(today, "１", "１"));
        assertEquals("2026-06-30", resolver.resolve(today, "2", "30"));
        assertThrows(() -> resolver.resolve(today, "1", "32"));
        pass("work date resolution");
    }

    private void testRecordSaveUpdateAndSummary() throws IOException {
        File worklog = Files.createTempFile("worklog-test-", ".csv").toFile();
        try {
            WorkRecordRepository repository = new WorkRecordRepository(worklog);
            WorkRecord first = new WorkRecord(
                    "2026-07-01", "テスト太郎", 480, 0, 9600, 0);
            WorkRecord replacement = new WorkRecord(
                    "2026-07-01", "テスト太郎", 540, 60, 10800, 300);
            WorkRecord second = new WorkRecord(
                    "2026-07-02", "テスト太郎", 510, 30, 10200, 150);

            assertFalse(repository.saveOrUpdate(first));
            assertTrue(repository.saveOrUpdate(replacement));
            assertFalse(repository.saveOrUpdate(second));

            List<String> lines = Files.readAllLines(worklog.toPath());
            assertEquals(2, lines.size());
            assertEquals(replacement.toCsv(), lines.get(0));

            WorkRecordRepository.MonthlySummary summary =
                    repository.summarize("2026-07", "テスト太郎");
            assertEquals(90, summary.getOvertimeMinutes());
            assertEquals(21450, summary.getPayment());
            pass("record save, update, and monthly summary");
        } finally {
            Files.deleteIfExists(worklog.toPath());
        }
    }

    private void testCsvValidation() {
        Config config = new Config("テスト太郎,1200,480,60");
        assertEquals("テスト太郎,1200,480,60", config.toCsv());
        assertThrows(() -> new Config("テスト太郎,1200,480"));
        assertThrows(() -> new WorkRecord("2026-07-01,テスト太郎,480"));
        pass("CSV conversion and validation");
    }

    private void pass(String testName) {
        passed++;
        System.out.println("PASS: " + testName);
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected=" + expected + ", actual=" + actual);
        }
    }

    private static void assertTrue(boolean value) {
        if (!value) {
            throw new AssertionError("expected=true, actual=false");
        }
    }

    private static void assertFalse(boolean value) {
        if (value) {
            throw new AssertionError("expected=false, actual=true");
        }
    }

    private static void assertThrows(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("IllegalArgumentException が発生しませんでした。");
    }
}
