import java.io.BufferedReader;// テキストファイルを読み込むクラス
import java.io.FileReader;// JavaScriptでファイル内容を読み込む
import java.io.FileWriter;// 追加で文字を書き込む
import java.io.IOException;// 例外処理
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//時間管理のための専用クラス
class Mytime{
	private int hour;// カプセル化(private)して書き換えを不可とする
	private int minute;
	
	//9:30など文字列を受け取って、時と分に分解して保存する。
	public Mytime(String timeStr) {
        String[] parts = timeStr.split(":");
        this.hour = Integer.parseInt(parts[0]);
        this.minute = Integer.parseInt(parts[1]);
	}
	// 計算が楽になるように、すべての時間を「分」に変換するメソッド
    public int toTotalMinutes() {
    	return hour * 60 + minute;
    }
    // 他の Mytime との差（diff）を計算してそのまま値を返す
    public int diff(Mytime other) {
        return this.toTotalMinutes() - other.toTotalMinutes();
    }
}

public class JobManager {
	public static void main(String[] args) {
		// Scannerを準備する
		Scanner scanner = new Scanner(System.in);
		System.out.println("社員名を入力してください：");
		// ユーザーが入力した名前を受け取る
		String name = scanner.nextLine();
		// ユーザーが入力した日付を受け取る
		System.out.println("日付を入力してください（例:2026-07-19）");
		String date = scanner.nextLine();
		// 入力時間を箱に入れる
		System.out.println("出勤時間を入力してください(例:9:30)");
		Mytime startTime = new Mytime(scanner.next());
		System.out.println("退勤時間を入力してください(例:18:30)");
		Mytime endTime = new Mytime(scanner.next());
		System.out.println("休憩時間を入力してください(例:1:00)");
		Mytime breakTime = new Mytime(scanner.next());
		
		// 実務時間の計算※休憩を引いた時間
        int workingTotalMinute =
        	    endTime.diff(startTime) - breakTime.toTotalMinutes();
		//残業時間の計算
		// 法定労働時間（8時間）
		int legalWorkMinute = 8 * 60;
		// 残業時間(分)
		int overtimeMinute = Math.max(0, workingTotalMinute - legalWorkMinute);
		
		// 画面表示用に 時 と 分 に戻す
		int workingHour = workingTotalMinute / 60;
		int workingMinute = workingTotalMinute % 60;

		System.out.println("実務時間：" + workingHour + "時間" + workingMinute + "分");
		// 給与の計算
		int hourlyWage = 1200;
		// 基本給（実務時間全体にかかる分）
		int salary = (workingTotalMinute * hourlyWage) / 60;
		// 残業時間の計算 残業代の割増率（25%アップ）と仮定
		double overtimeRate = 0.25; 
		// 先に「時給 × 割増率 × 分」を計算してから、最後に60で割る
		int overtimePay = (int) ((hourlyWage * overtimeRate * overtimeMinute) / 60);
		
		// 今回の入力データ（CSVの1行分）を作成
        String newRecord = date + "," + name + "," + workingTotalMinute + "," + overtimeMinute + "," + salary + "," + overtimePay;

        // --- 【改善】既存のCSVを読み込み、同じ日付のデータがあれば差し替える ---
        List<String> fileLines = new ArrayList<>();
        boolean isUpdated = false;

        try {
            // ファイルが存在する場合のみ読み込む
            java.io.File file = new java.io.File("worklog.csv");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    // 「日付」と「名前」が一致する行を見つけたら、新しいデータに差し替える
                    if (data[0].equals(date) && data[1].equals(name)) {
                        fileLines.add(newRecord);
                        isUpdated = true;
                    } else {
                        fileLines.add(line);
                    }
                }
                reader.close();
            }
        } catch (IOException e) {
            System.out.println("ファイルの読み込み中にエラーが発生しました。");
        }

        // 過去に同じ日付のデータがなければ、末尾に新規追加する
        if (!isUpdated) {
            fileLines.add(newRecord);
        }

        // CSVファイルに書き戻す（全上書き保存）
        try {
            FileWriter writer = new FileWriter("worklog.csv", false); // falseで上書きモード
            for (String line : fileLines) {
                writer.write(line + "\n");
            }
            writer.close();
            
            if (isUpdated) {
                System.out.println("【確認】既存のデータを修正（上書き）しました。");
            } else {
                System.out.println("CSVに新規保存しました。");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 集計（名前が一致するデータのみを合計）
        int totalOvertime = 0;
        int monthlySalary = 0;
        int monthlyOvertimePay = 0;
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader("worklog.csv"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                // 配布用に「自分の名前」のデータだけを集計するように変更
                if (data[1].equals(name)) {
                    totalOvertime += Integer.parseInt(data[3]);
                    monthlySalary += Integer.parseInt(data[4]);
                    monthlyOvertimePay += Integer.parseInt(data[5]);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		System.out.println("================================");
        System.out.println("【月間集計結果】");
        System.out.println("月間残業時間：" + totalOvertime / 60 + "時間" + totalOvertime % 60 + "分");
        System.out.println("当月支給金額：" + (monthlySalary + monthlyOvertimePay) + "円");
        System.out.println("================================");
        
        
     // 今回の入力の表示
        System.out.println("\n【今回の入力内容】");
        System.out.println("名前：" + name);
        System.out.println("実務時間：" + workingHour + ":" + String.format("%02d", workingMinute));
        System.out.println("残業時間：" + overtimeMinute + "分");
        System.out.println("給与(時給1200円)：" + salary + "円");
        System.out.println("残業手当(割増分)：" + overtimePay + "円");
        // スキャナーの片付け
        scanner.close();
    }
}