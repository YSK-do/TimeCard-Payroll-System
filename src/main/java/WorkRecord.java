public class WorkRecord {
    private final String date;
    private final String name;
    private final int workMinutes;
    private final int overtimeMinutes;
    private final int salary;
    private final int overtimePay;

    public WorkRecord(String date, String name, int workMinutes, int overtimeMinutes,
            int salary, int overtimePay) {
        this.date = date;
        this.name = name;
        this.workMinutes = workMinutes;
        this.overtimeMinutes = overtimeMinutes;
        this.salary = salary;
        this.overtimePay = overtimePay;
    }

    public WorkRecord(String csvLine) {
        String[] data = csvLine.split(",", -1);
        if (data.length != 6) {
            throw new IllegalArgumentException("勤務記録ファイルの形式が不正です。worklog.csvを確認してください。");
        }
        this.date = data[0];
        this.name = data[1];
        this.workMinutes = Integer.parseInt(data[2]);
        this.overtimeMinutes = Integer.parseInt(data[3]);
        this.salary = Integer.parseInt(data[4]);
        this.overtimePay = Integer.parseInt(data[5]);
    }

    public String toCsv() {
        return date + "," + name + "," + workMinutes + "," + overtimeMinutes
                + "," + salary + "," + overtimePay;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public int getWorkMinutes() {
        return workMinutes;
    }

    public int getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public int getSalary() {
        return salary;
    }

    public int getOvertimePay() {
        return overtimePay;
    }
}
