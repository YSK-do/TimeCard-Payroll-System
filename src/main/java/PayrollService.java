public class PayrollService {
    private static final double OVERTIME_RATE = 0.25;

    public WorkRecord calculate(String date, Config config, MyTime startTime, MyTime endTime) {
        int workMinutes = endTime.diff(startTime) - config.getBreakMinutes();
        if (workMinutes < 0) {
            throw new IllegalArgumentException("退勤時間は出勤時間より後にしてください。");
        }

        int overtimeMinutes = Math.max(0, workMinutes - config.getContractMinutes());
        int salary = workMinutes * config.getHourlyWage() / 60;
        int overtimePay = (int) (config.getHourlyWage() * OVERTIME_RATE
                * overtimeMinutes / 60);

        return new WorkRecord(date, config.getName(), workMinutes, overtimeMinutes,
                salary, overtimePay);
    }
}
