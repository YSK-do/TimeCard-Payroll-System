import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WorkRecordRepository {
     private final File worklogFile;

     public WorkRecordRepository() {
        this(new File("worklog.csv"));
    }

    WorkRecordRepository(File worklogFile) {
        this.worklogFile = worklogFile;
    }

    public boolean saveOrUpdate(WorkRecord newRecord) {
        List<WorkRecord> records = readAll();
        boolean updated = false;

        for (int i = 0; i < records.size(); i++) {
            WorkRecord existing = records.get(i);
            if (existing.getDate().equals(newRecord.getDate())
                    && existing.getName().equals(newRecord.getName())) {
                records.set(i, newRecord);
                updated = true;
                break;
            }
        }

        if (!updated) {
            records.add(newRecord);
        }
        writeAll(records);
        return updated;
    }

    public MonthlySummary summarize(String targetMonth, String name) {
        int overtimeMinutes = 0;
        int salary = 0;
        int overtimePay = 0;

        for (WorkRecord record : readAll()) {
            if (record.getDate().startsWith(targetMonth)
                    && record.getName().equals(name)) {
                overtimeMinutes += record.getOvertimeMinutes();
                salary += record.getSalary();
                overtimePay += record.getOvertimePay();
            }
        }
        return new MonthlySummary(overtimeMinutes, salary + overtimePay);
    }

    private List<WorkRecord> readAll() {
        List<WorkRecord> records = new ArrayList<>();
        File file = worklogFile;
        if (!file.exists()) {
            return records;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(new WorkRecord(line));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("勤務記録の読み込み中にエラーが発生しました。", exception);
        }
        return records;
    }

    private void writeAll(List<WorkRecord> records) {
        try (FileWriter writer = new FileWriter(worklogFile, false)) {
            for (WorkRecord record : records) {
                writer.write(record.toCsv() + System.lineSeparator());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("勤務記録の保存中にエラーが発生しました。", exception);
        }
    }

    public static class MonthlySummary {
        private final int overtimeMinutes;
        private final int payment;

        public MonthlySummary(int overtimeMinutes, int payment) {
            this.overtimeMinutes = overtimeMinutes;
            this.payment = payment;
        }

        public int getOvertimeMinutes() {
            return overtimeMinutes;
        }

        public int getPayment() {
            return payment;
        }
    }
}
