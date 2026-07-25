import java.time.LocalDate;

public class WorkDateResolverTest {
    public static void main(String[] args) {
        WorkDateResolver resolver = new WorkDateResolver();

        assertEquals("2026-11-01",
                resolver.resolve(LocalDate.of(2026, 11, 1), "1", "1"));
        assertEquals("2026-10-31",
                resolver.resolve(LocalDate.of(2026, 11, 1), "2", "31"));
        assertThrows(() ->
                resolver.resolve(LocalDate.of(2026, 11, 1), "1", "31"));
        assertThrows(() ->
                resolver.resolve(LocalDate.of(2026, 2, 1), "1", "30"));
        assertThrows(() ->
                resolver.resolve(LocalDate.of(2026, 11, 1), "3", "1"));

        System.out.println("WorkDateResolverTest: OK");
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) {
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
