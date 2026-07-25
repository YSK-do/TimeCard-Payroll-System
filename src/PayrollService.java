public class PayrollService {
    private static final double OVERTIME_RATE = 0.25;
    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final int BREAK_THRESHOLD_MINUTES = 6 * 60;

    public WorkRecord calculate(String date, Config config, MyTime startTime, MyTime endTime) {
        int elapsedMinutes = endTime.diff(startTime);
        if (elapsedMinutes < 0) {
            elapsedMinutes += MINUTES_PER_DAY;
        }

        int breakMinutes = elapsedMinutes > BREAK_THRESHOLD_MINUTES
                ? config.getBreakMinutes()
                : 0;
        int workMinutes = elapsedMinutes - breakMinutes;
        if (workMinutes < 0) {
            throw new IllegalArgumentException("休憩時間が勤務時間を超えています。");
        }

        int overtimeMinutes = Math.max(0, workMinutes - config.getContractMinutes());
        int salary = workMinutes * config.getHourlyWage() / 60;
        int overtimePay = (int) (config.getHourlyWage() * OVERTIME_RATE
                * overtimeMinutes / 60);

        return new WorkRecord(date, config.getName(), workMinutes, overtimeMinutes,
                salary, overtimePay);
    }
}
