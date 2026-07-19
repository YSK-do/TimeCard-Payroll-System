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
		double overtimeRate = 1.25; 
		// 先に「時給 × 割増率 × 分」を計算してから、最後に60で割る
		int overtimePay = (int) ((hourlyWage * overtimeRate * overtimeMinute) / 60);
		
		// 入力の表示
		System.out.println("名前：" + name);
		System.out.println("実務時間：" + workingHour + ":" + workingMinute);
		System.out.println("残業時間：" + overtimeMinute + "分");
		System.out.println("給与(時給1200円)：" + salary + "円");
		System.out.println("残業手当：" + overtimePay + "円");
		// Scannerの片付け
		scanner.close();

	}

}

