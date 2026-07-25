public class PayrollServiceTest {
    public static void main(String[] args) {
        PayrollService service = new PayrollService();
        Config noBreakConfig = new Config("テスト", 1200, 8 * 60, 0);
        Config standardBreakConfig = new Config("テスト", 1200, 8 * 60, 60);

        WorkRecord daytime = service.calculate(
                "2026-07-25", noBreakConfig,
                new MyTime("9:00"), new MyTime("17:00"));
        assertEquals(8 * 60, daytime.getWorkMinutes());

        WorkRecord overnight = service.calculate(
                "2026-07-25", noBreakConfig,
                new MyTime("22:00"), new MyTime("6:00"));
        assertEquals(8 * 60, overnight.getWorkMinutes());

        WorkRecord overnightWithBreak = service.calculate(
                "2026-07-25", standardBreakConfig,
                new MyTime("22:00"), new MyTime("6:00"));
        assertEquals(7 * 60, overnightWithBreak.getWorkMinutes());

        WorkRecord shortShift = service.calculate(
                "2026-07-25", standardBreakConfig,
                new MyTime("10:00"), new MyTime("12:00"));
        assertEquals(2 * 60, shortShift.getWorkMinutes());

        WorkRecord exactlySixHours = service.calculate(
                "2026-07-25", standardBreakConfig,
                new MyTime("9:00"), new MyTime("15:00"));
        assertEquals(6 * 60, exactlySixHours.getWorkMinutes());

        WorkRecord overSixHours = service.calculate(
                "2026-07-25", standardBreakConfig,
                new MyTime("9:00"), new MyTime("15:01"));
        assertEquals(5 * 60 + 1, overSixHours.getWorkMinutes());

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
