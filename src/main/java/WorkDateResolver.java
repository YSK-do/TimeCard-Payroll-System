import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class WorkDateResolver {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String resolve(LocalDate today, String monthChoice, String dayInput) {
        String normalizedChoice = MyTime.toHalfWidth(monthChoice);
        String normalizedDay = MyTime.toHalfWidth(dayInput);

        YearMonth targetMonth;
        if ("1".equals(normalizedChoice)) {
            targetMonth = YearMonth.from(today);
        } else if ("2".equals(normalizedChoice)) {
            targetMonth = YearMonth.from(today).minusMonths(1);
        } else {
            throw new IllegalArgumentException(
                    "対象月は 1（今月）または 2（前月）で入力してください。");
        }

        try {
            int day = Integer.parseInt(normalizedDay);
            return targetMonth.atDay(day).format(DATE_FORMATTER);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "日にちは数字で入力してください。");
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(
                    targetMonth + " に " + normalizedDay + "日はありません。");
        }
    }
}
