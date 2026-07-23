import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class JobManager {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            ConfigRepository configRepository = new ConfigRepository();
            WorkRecordRepository recordRepository = new WorkRecordRepository();
            PayrollService payrollService = new PayrollService();

            Config config = configRepository.loadOrCreate(scanner);
            String currentYearMonth = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM"));

            System.out.println();
            System.out.println("【現在の対象月: " + currentYearMonth + "】");
            System.out.print("日にちを入力してください（例:19 または 20）：");
            String day = InputNormalizer.toHalfWidth(scanner.next());
            if (day.length() == 1) {
                day = "0" + day;
            }
            String date = currentYearMonth + "-" + day;

            System.out.print("出勤時間を入力してください（例:9:30）：");
            MyTime startTime = new MyTime(scanner.next());
            System.out.print("退勤時間を入力してください（例:18:30）：");
            MyTime endTime = new MyTime(scanner.next());

            WorkRecord record = payrollService.calculate(date, config, startTime, endTime);
            boolean updated = recordRepository.saveOrUpdate(record);
            System.out.println(updated
                    ? "【確認】既存のデータを修正（上書き）しました。"
                    : "CSVに新規保存しました。");

            printMonthlySummary(recordRepository, record, config);
            printCurrentRecord(record, config);
            printBreakCard(scanner);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            System.out.println("エラー：" + exception.getMessage());
        }
    }

    private static void printMonthlySummary(WorkRecordRepository repository,
            WorkRecord record, Config config) {
        String targetMonth = record.getDate().substring(0, 7);
        WorkRecordRepository.MonthlySummary summary =
                repository.summarize(targetMonth, config.getName());

        System.out.println("================================");
        System.out.println("【" + targetMonth + " の月間集計結果】");
        System.out.println("月間残業時間：" + summary.getOvertimeMinutes() / 60
                + "時間" + summary.getOvertimeMinutes() % 60 + "分");
        System.out.println("当月支給金額：" + summary.getPayment() + "円");
        System.out.println("================================");
    }

    private static void printCurrentRecord(WorkRecord record, Config config) {
        System.out.println();
        System.out.println("【今回の入力内容】");
        System.out.println("名前：" + config.getName());
        System.out.println("実務時間：" + record.getWorkMinutes() / 60 + ":"
                + String.format("%02d", record.getWorkMinutes() % 60));
        System.out.println("残業時間：" + record.getOvertimeMinutes() + "分");
        System.out.println("給与(時給" + config.getHourlyWage() + "円)："
                + record.getSalary() + "円");
        System.out.println("残業手当(割増分)：" + record.getOvertimePay() + "円");
    }

    private static void printBreakCard(Scanner scanner) {
        System.out.println();
        System.out.println("--------------------------------");
        System.out.print("【みんなくろうカード】今日の休憩はしっかり取れましたか？(y/n): ");
        String answer = scanner.next().toLowerCase();

        if (answer.equals("y") || answer.equals("yes")) {
            System.out.println("素晴らしい！体調管理バッチリですね。明日もその調子で！");
        } else {
            System.out.println("今日もお疲れ様でした。無理しすぎず、ゆっくり休んでくださいね。");
        }
        System.out.println("--------------------------------");
    }
}
