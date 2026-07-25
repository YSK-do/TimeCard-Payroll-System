public class PayrollServiceTest {
    public static void main(String[] args) {
        PayrollService service = new PayrollService();
        Config config = new Config("テスト", 1200, 8 * 60, 0);

        WorkRecord daytime = service.calculate(
                "2026-07-25", config, new MyTime("9:00"), new MyTime("17:00"));
        assertEquals(8 * 60, daytime.getWorkMinutes());

        WorkRecord overnight = service.calculate(
                "2026-07-25", config, new MyTime("22:00"), new MyTime("6:00"));
        assertEquals(8 * 60, overnight.getWorkMinutes());

        WorkRecord overnightWithBreak = service.calculate(
                "2026-07-25", new Config("テスト", 1200, 8 * 60, 60),
                new MyTime("22:00"), new MyTime("6:00"));
        assertEquals(7 * 60, overnightWithBreak.getWorkMinutes());

        assertThrows(() -> new MyTime("24:00"));
        assertThrows(() -> new MyTime("30:00"));

        System.out.println("PayrollServiceTest: OK");
    }

    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError(
                    "expected=" + expected + ", actual=" + actual);
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
