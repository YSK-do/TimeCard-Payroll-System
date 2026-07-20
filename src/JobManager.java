import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// 時間管理のための専用クラス
class Mytime {
    private int hour;
    private int minute;
    
    public Mytime(String timeStr) {
        String cleaned = JobManager.toHalfWidth(timeStr).replace("：", ":");
        String[] parts = cleaned.split(":");
        this.hour = Integer.parseInt(parts[0]);
        this.minute = Integer.parseInt(parts[1]);
    }
    
    public int toTotalMinutes() {
        return hour * 60 + minute;
    }
    
    public int diff(Mytime other) {
        return this.toTotalMinutes() - other.toTotalMinutes();
    }
}

// 勤務記録を管理する専用クラス
class WorkRecord {
    private String date;
    private String name;
    private int workMinutes;
    private int overtimeMinutes;
    private int salary;
    private int overtimePay;

    public WorkRecord(String date, String name, int workMinutes, int overtimeMinutes, int salary, int overtimePay) {
        this.date = date;
        this.name = name;
        this.workMinutes = workMinutes;
        this.overtimeMinutes = overtimeMinutes;
        this.salary = salary;
        this.overtimePay = overtimePay;
    }

    public WorkRecord(String csvLine) {
        String[] data = csvLine.split(",");
        this.date = data[0];
        this.name = data[1];
        this.workMinutes = Integer.parseInt(data[2]);
        this.overtimeMinutes = Integer.parseInt(data[3]);
        this.salary = Integer.parseInt(data[4]);
        this.overtimePay = Integer.parseInt(data[5]);
    }

    public String toCsv() {
        return date + "," + name + "," + workMinutes + "," + overtimeMinutes + "," + salary + "," + overtimePay;
    }

    public String getDate() { return date; }
    public String getName() { return name; }
    public int getOvertimeMinutes() { return overtimeMinutes; }
    public int getSalary() { return salary; }
    public int getOvertimePay() { return overtimePay; }
}

// 【新設】設定情報を管理する専用クラス
class Config {
    private String name;
    private int hourlyWage;
    private int contractMinutes;
    private int breakMinutes;

    // 新規登録用のコンストラクタ
    public Config(String name, int hourlyWage, int contractMinutes, int breakMinutes) {
        this.name = name;
        this.hourlyWage = hourlyWage;
        this.contractMinutes = contractMinutes;
        this.breakMinutes = breakMinutes;
    }

    // CSVの1行から設定を復元するコンストラクタ
    public Config(String csvLine) {
        String[] data = csvLine.split(",");
        this.name = data[0];
        this.hourlyWage = Integer.parseInt(data[1]);
        this.contractMinutes = Integer.parseInt(data[2]);
        this.breakMinutes = Integer.parseInt(data[3]);
    }

    // CSV用の文字列に変換するメソッド
    public String toCsv() {
        return name + "," + hourlyWage + "," + contractMinutes + "," + breakMinutes;
    }

    // 外部から値を取得するためのゲッター
    public String getName() { return name; }
    public int getHourlyWage() { return hourlyWage; }
    public int getContractMinutes() { return contractMinutes; }
    public int getBreakMinutes() { return breakMinutes; }
}

public class JobManager {
    public static String toHalfWidth(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= '０' && c <= '９') {
                sb.append((char) (c - '０' + '0'));
            } else if (c == '－' || c == 'ー' || c == '‐') {
                sb.append('-');
            } else if (c == '：') {
                sb.append(':');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        // --- 初期設定ファイルの読み込み・作成機能 ---
        Config config = null; // 【改善】バラバラだった変数を1つのオブジェクトに統合
        File configFile = new File("config.csv");
        
        if (!configFile.exists()) {
            System.out.println("【初期設定】初回起動のため、基本情報を登録してください。");
            System.out.print("社員名を入力してください：");
            String name = scanner.nextLine();
            
            System.out.print("時給を入力してください（例:1200）：");
            String wageInput = toHalfWidth(scanner.nextLine());
            int hourlyWage = Integer.parseInt(wageInput);
            
            System.out.print("1日の契約労働時間を入力してください（例:8:00）：");
            Mytime contractTime = new Mytime(scanner.nextLine());
            int contractMinutes = contractTime.toTotalMinutes();
            
            System.out.print("標準の休憩時間を入力してください（例:1:00）：");
            Mytime breakTimeSetup = new Mytime(scanner.nextLine());
            int breakMinutes = breakTimeSetup.toTotalMinutes();
            
            // 入力された情報からConfigオブジェクトを作成
            config = new Config(name, hourlyWage, contractMinutes, breakMinutes);
            
            // 設定をCSVに保存
            try (FileWriter configWriter = new FileWriter(configFile, false)) {
                configWriter.write(config.toCsv() + "\n");
                System.out.println("初期設定を保存しました。\n");
            } catch (IOException e) {
                System.out.println("設定ファイルの保存中にエラーが発生しました。");
            }
        } else {
            // 設定ファイルがある場合は、ファイルから1行読み込んでConfigオブジェクトを復元
            try (BufferedReader configReader = new BufferedReader(new FileReader(configFile))) {
                String line = configReader.readLine();
                if (line != null) {
                    config = new Config(line);
                    System.out.println("【ようこそ、" + config.getName() + " さん】");
                    System.out.println("時給:" + config.getHourlyWage() + "円 / 契約労働時間:" + (config.getContractMinutes() / 60) + "時間 / 設定休憩時間:" + (config.getBreakMinutes() / 60) + "時間\n");
                }
            } catch (IOException e) {
                System.out.println("設定ファイルの読み込み中にエラーが発生しました。");
            }
        }
        
        // --- ここから現行の勤怠入力システム ---
        System.out.println("日付を入力してください（例:2026-07-19）：");
        String date = toHalfWidth(scanner.next());
        
        System.out.println("出勤時間を入力してください(例:9:30)：");
        Mytime startTime = new Mytime(scanner.next());
        System.out.println("退勤時間を入力してください(例:18:30)：");
        Mytime endTime = new Mytime(scanner.next());
        
        // 【改善】configオブジェクトから設定値を引っ張る
        int workingTotalMinute = endTime.diff(startTime) - config.getBreakMinutes();
        int overtimeMinute = Math.max(0, workingTotalMinute - config.getContractMinutes());
        
        int workingHour = workingTotalMinute / 60;
        int workingMinute = workingTotalMinute % 60;

        System.out.println("実務時間：" + workingHour + "時間" + workingMinute + "分");
        
        // 【改善】給与計算もconfigの値を使用
        int salary = (workingTotalMinute * config.getHourlyWage()) / 60;
        double overtimeRate = 0.25; 
        int overtimePay = (int) ((config.getHourlyWage() * overtimeRate * overtimeMinute) / 60);
        
        // 今回の入力データを作成
        WorkRecord newRecord = new WorkRecord(date, config.getName(), workingTotalMinute, overtimeMinute, salary, overtimePay);

        // --- 既存のCSVを読み込み、同じ日付のデータがあれば差し替える ---
        List<WorkRecord> fileRecords = new ArrayList<>();
        boolean isUpdated = false;

        File worklogFile = new File("worklog.csv");
        if (worklogFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(worklogFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    WorkRecord existingRecord = new WorkRecord(line);
                    if (existingRecord.getDate().equals(date) && existingRecord.getName().equals(config.getName())) {
                        fileRecords.add(newRecord);
                        isUpdated = true;
                    } else {
                        fileRecords.add(existingRecord);
                    }
                }
            } catch (IOException e) {
                System.out.println("ファイルの読み込み中にエラーが発生しました。");
            }
        }

        if (!isUpdated) {
            fileRecords.add(newRecord);
        }

        // CSVファイルに書き戻す
        try (FileWriter writer = new FileWriter("worklog.csv", false)) {
            for (WorkRecord record : fileRecords) {
                writer.write(record.toCsv() + "\n");
            }
            if (isUpdated) {
                System.out.println("【確認】既存のデータを修正（上書き）しました。");
            } else {
                System.out.println("CSVに新規保存しました。");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 集計
        int totalOvertime = 0;
        int monthlySalary = 0;
        int monthlyOvertimePay = 0;
        
        if (worklogFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(worklogFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    WorkRecord record = new WorkRecord(line);
                    if (record.getName().equals(config.getName())) {
                        totalOvertime += record.getOvertimeMinutes();
                        monthlySalary += record.getSalary();
                        monthlyOvertimePay += record.getOvertimePay();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        System.out.println("================================");
        System.out.println("【月間集計結果】");
        System.out.println("月間残業時間：" + totalOvertime / 60 + "時間" + totalOvertime % 60 + "分");
        System.out.println("当月支給金額：" + (monthlySalary + monthlyOvertimePay) + "円");
        System.out.println("================================");
        
        // 今回の入力の表示
        System.out.println("\n【今回の入力内容】");
        System.out.println("名前：" + config.getName());
        System.out.println("実務時間：" + workingHour + ":" + String.format("%02d", workingMinute));
        System.out.println("残業時間：" + overtimeMinute + "分");
        System.out.println("給与(時給" + config.getHourlyWage() + "円)：" + salary + "円");
        System.out.println("残業手当(割増分)：" + overtimePay + "円");
        
        // --- 自己啓発：休憩は取れましたカード ---
        System.out.println("\n--------------------------------");
        System.out.print("【みんなくろうカード】今日の休憩はしっかり取れましたか？(y/n): ");
        String answer = scanner.next().toLowerCase();
        
        if (answer.equals("y") || answer.equals("yes")) {
            System.out.println("素晴らしい！体調管理バッチリですね。明日もその調子で！");
        } else {
            System.out.println("そっか、今日もお疲れ様でした。無理しすぎないで、温かいものでも飲んでゆっくり休んでくださいね。");
        }
        System.out.println("--------------------------------");
        
        scanner.close();
    }
}