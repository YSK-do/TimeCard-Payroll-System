# 勤怠管理・給与計算システム

Javaの学習を目的に開発している、個人向けの勤怠管理・給与計算システムです。

現在は、従来のコンソール版（CLI）と、Spring Bootを使用したWeb版が同じリポジトリにあります。  
CLI版には給与計算や月間集計まで実装済みです。Web版は基本設定、勤怠登録、給与計算、CSV保存まで対応しています。

## 開発の背景

前職で派遣社員として働いていた際、残業時間の管理が曖昧だったことから、給与の未払いを経験しました。

この経験をきっかけに、働いた時間と給与を1分単位で記録し、会社側の勤怠データとの違いを確認しやすくすることを目的として開発しています。

毎日使用することを想定し、PC操作が苦手な方でも迷いにくい、シンプルなシステムを目指しています。

## 現在の進捗

| 機能 | CLI版 | Web版 |
| --- | :---: | :---: |
| 勤務日の入力 | ✅ | ✅ |
| 出勤・退勤時刻の入力 | ✅ | ✅ |
| 実働時間・残業時間の計算 | ✅ | ✅ |
| CSVへの保存 | ✅ | ✅ |
| 同じ日付・氏名の記録を上書き | ✅ | ✅ |
| 氏名・時給・休憩時間などの設定 | ✅ | ✅ |
| 基本給与・残業手当の計算 | ✅ | ✅ |
| 月間集計 | ✅ | ✅ |
| 自動テスト | 簡易テストあり | ✅ |

## Web版で現在できること

ブラウザ画面から勤務日、出勤時刻、退勤時刻を入力して勤怠を登録できます。

- 対象月は今月または前月を選択
- 氏名、時給、標準労働時間、休憩時間を画面から設定
- 保存した標準労働時間と休憩時間で勤怠を計算
- 実働時間と残業時間を計算
- 時給を使って基本給与・残業手当・支給金額を計算
- 対象月の勤務日数・勤務時間・給与を月間集計
- 登録前の確認と登録結果を画面に表示
- POST APIを通じて `data/attendance.csv` に保存
- 同じ勤務日・氏名の記録は新しい内容で上書き
- 不正な入力に対してJSON形式のエラーメッセージを返却

### 基本設定API

```http
GET /api/settings
PUT /api/settings
Content-Type: application/json
```

設定は `data/settings.csv` に保存します。

```json
{
  "employeeName": "山田太郎",
  "hourlyWage": 1200,
  "standardWorkMinutes": 480,
  "breakMinutes": 60
}
```

### 月間集計API

```http
GET /api/attendances/summary?month=2026-07
```

保存済みの氏名と指定月に一致する勤怠から、勤務日数、実働時間、残業時間、基本給与、残業手当、支給金額を合計します。

### 勤怠登録API

```http
POST /api/attendances
Content-Type: application/json
```

リクエスト例：

```json
{
  "employeeName": "山田太郎",
  "workDate": "2026-07-27",
  "startTime": "09:00",
  "endTime": "18:00"
}
```

成功時はHTTP 201で次のような結果を返します。

```json
{
  "workMinutes": 480,
  "overtimeMinutes": 0,
  "basePay": 9600,
  "overtimePay": 0,
  "totalPay": 9600,
  "message": "勤怠をCSVへ保存しました。"
}
```

### Web版の保存ファイル

`data/attendance.csv` に次の形式で追記します。

```csv
employeeName,workDate,startTime,endTime,workMinutes,overtimeMinutes,basePay,overtimePay,totalPay
山田太郎,2026-07-27,09:00,18:00,480,0,9600,0,9600
```

同じ勤務日・氏名を再登録すると、以前の行を新しい内容へ上書きします。

## CLI版で現在できること

- 初回起動時に氏名、時給、契約労働時間、標準休憩時間を設定
- 設定を `config.csv` に保存し、次回起動時に読み込み
- 今月または前月の勤務日を指定
- 全角数字・全角コロンを含む時刻入力を半角へ変換
- 実働時間、残業時間、基本給与、残業手当を計算
- 勤務記録を `worklog.csv` に保存
- 同じ日付・同じ氏名の記録を上書き
- 対象月の残業時間と支給金額を集計

計算式：

```text
実働時間 ＝ 退勤時刻 − 出勤時刻 − 標準休憩時間
基本給与 ＝ 実働時間 × 時給
残業手当 ＝ 残業時間 × 時給 × 25%
支給金額 ＝ 基本給与 ＋ 残業手当
```

金額は整数で計算し、1円未満は切り捨てます。

## 使用技術

| 分類 | 技術 |
| --- | --- |
| 言語 | Java 17、JavaScript |
| フレームワーク | Spring Boot 4.1.0 |
| 画面 | HTML / CSS |
| API | Spring Web |
| ビルド | Maven |
| データ保存 | CSV |
| バージョン管理 | Git / GitHub |

データベースはまだ使用していません。

## Web版の起動方法

### 1. リポジトリをクローン

```bash
git clone https://github.com/YSK-do/TimeCard-Payroll-System.git
cd TimeCard-Payroll-System
```

### 2. Spring Bootを起動

Mavenがインストールされている場合：

```bash
mvn spring-boot:run
```

Maven Wrapperを使用する場合：

```bash
./mvnw spring-boot:run
```

Windowsでは次のコマンドを使用します。

```powershell
mvnw.cmd spring-boot:run
```

### 3. ブラウザで開く

```text
http://localhost:8080
```

画面の `app.js` が `POST /api/attendances` を呼び出し、Spring Boot側で計算とCSV保存を行います。

## CLI版の実行方法

ソースは `src/main/java` にあります。

```bash
javac src/main/java/*.java
java -cp src/main/java JobManager
```

CLI版とWeb版は現在別々の処理として動作しており、保存先のCSV形式も異なります。

| 種類 | 保存先 | 主な内容 |
| --- | --- | --- |
| CLI設定 | `config.csv` | 氏名、時給、契約労働時間、標準休憩時間 |
| CLI勤怠 | `worklog.csv` | 日付、氏名、実働時間、残業時間、基本給与、残業手当 |
| Web設定 | `data/settings.csv` | 氏名、時給、標準労働時間、休憩時間 |
| Web勤怠 | `data/attendance.csv` | 氏名、勤務日、出退勤時刻、実働・残業時間、基本給与、残業手当、支給金額 |

## テスト

### CLI版の簡易テスト

```bash
javac src/main/java/*.java
java -cp src/main/java TestRunner
```

次の内容を確認します。

- 時刻の変換と入力値チェック
- 実働時間・残業時間・給与・残業手当の計算
- 今月・前月の日付生成
- 勤務記録の新規保存・上書き・月間集計
- CSV形式のチェック

すべて成功すると、最後に次のように表示されます。

```text
TestRunner: 5 tests passed
```

### Web版のテスト状況

CSVの上書き保存、Web基本設定、基本給与・残業手当の計算、月間集計にJUnitテストを追加しています。Controllerの自動テストは今後追加予定です。

## 主な構成

```text
src/main/java/
├── JobManager.java
├── Config.java
├── ConfigRepository.java
├── MyTime.java
├── PayrollService.java
├── WorkDateResolver.java
├── WorkRecord.java
├── WorkRecordRepository.java
├── TestRunner.java
└── com/example/timecard/
    ├── TimeCardApplication.java
    ├── controller/
    │   ├── AttendanceController.java
    │   ├── SettingsController.java
    │   └── ApiExceptionHandler.java
    ├── domain/
    │   ├── AppSettings.java
    │   ├── AttendanceRecord.java
    │   ├── AttendanceRequest.java
    │   ├── AttendanceResponse.java
    │   └── PayrollCalculation.java
    ├── repository/
    │   ├── AttendanceCsvRepository.java
    │   └── SettingsCsvRepository.java
    └── service/
        ├── AttendanceService.java
        ├── PayrollCalculator.java
        └── SettingsService.java

src/main/resources/static/
├── index.html
├── styles.css
└── app.js
```

## 現在の制限

- CLI版とWeb版の処理およびCSVはまだ統合されていません
- 選択できる対象月は今月と前月のみです
- 日をまたぐ勤務には対応していません
- 土日・祝日、深夜労働などの割増計算には対応していません
- CSVの値にカンマが含まれる場合のエスケープには対応していません

## 次に取り組むこと

1. Web APIのController・Serviceテストを充実させる
2. CLI版とWeb版の給与計算処理を統合する
3. CSV保存をデータベース保存へ移行する

## 注意事項

このシステムは、JavaとSpring Bootの学習を目的とした個人開発作品です。

実際の給与計算や勤怠管理に使用する場合は、勤務先の就業規則や最新の法令に合わせた確認・調整が必要です。
