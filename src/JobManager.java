import java.util.Scanner;

public class JobManager {

	public static void main(String[] args) {
		// Scannerを準備する
		Scanner scanner = new Scanner(System.in);
		System.out.println("社員名を入力してください：");
		// ユーザーが入力した名前を受け取る
		String name = scanner.nextLine();
		
		System.out.println("出勤時間を入力してください(例:9:30)");
		// "9:30" という文字列として受け取る
		String startInput = scanner.next(); 
		
		// 「:」で文字を分割する
		String[] startTimeParts = startInput.split(":");
		int startHour = Integer.parseInt(startTimeParts[0]);  // "◯:◯◯" の◯を数値に変換
		int startMinute = Integer.parseInt(startTimeParts[1]);// "◯:◯◯" の◯◯を数値に変換
				
		
		System.out.println("退勤時間を入力してください(例:18:30)");
		 // "18:30" という文字列として受け取る
		String endInput = scanner.next();
		String[] endTimeParts = endInput.split(":");
		int endHour = Integer.parseInt(endTimeParts[0]);// "◯:◯◯" の◯を数値に変換
		int endMinute = Integer.parseInt(endTimeParts[1]);// "◯:◯◯" の◯◯を数値に変換
		
		
		System.out.println("休憩時間を入力してください(例:1:00)");
		 // "1:00" という文字列として受け取る
		String breakInput = scanner.next();
		String[] breakTimeParts = breakInput.split(":");
		int breakHour = Integer.parseInt(breakTimeParts[0]);// "◯:◯◯" の◯を数値に変換
		int breakMinute = Integer.parseInt(breakTimeParts[1]);// "◯:◯◯" の◯◯を数値に変換
		
		// 勤務時間の計算※休憩を含む
		int workHour = endHour - startHour;	
		int workMinute = endMinute - startMinute;		
		// 実務時間の計算※休憩を引いた時間
		int workingHour = workHour - breakHour;	
		int workingMinute = workMinute - breakMinute;
		if (workingMinute < 0) {
		    workingHour = workingHour - 1;       // 時間を1時間減らす
		    workingMinute = workingMinute + 60;  // その分、分に60分を足す
		}
		//残業時間の計算
		// 法定労働時間（8時間）
		int legalWorkMinute = 8 * 60;
		// 勤務時間（分）
		int workingTotalMinute = workingHour * 60 + workingMinute;
		// 残業時間(分)
		int overtimeMinute =  0;
		if (workingTotalMinute > legalWorkMinute) {
		    overtimeMinute = workingTotalMinute - legalWorkMinute;
		}		
		// 給与の計算
		int hourlyWage = 1200;
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

