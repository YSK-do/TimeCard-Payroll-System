# 勤怠管理・給与計算システム

Javaの学習から始めた、時給制勤務者向けの個人用勤怠・給与計算アプリです。

最初にJavaの基本処理を学ぶためのCLI版を作り、その後、より使いやすくするためにSpring BootのWeb版へ発展させました。現在は**Web版を主な成果物**とし、CLI版は開発初期の学習・試作版として残しています。

## 開発の背景

前職で派遣社員として働いていた際、残業時間の管理が曖昧で、給与の未払いを経験しました。

この経験から、働いた時間と給与を自分でも記録し、勤務先の勤怠データと比較しやすくすることを目的に開発しました。毎日使うことを想定し、PC操作が得意でない方でも迷いにくい画面を目指しています。

## Web版でできること

- 氏名、時給、1日の所定労働時間、休憩時間の保存
- 時給を1円単位で自由入力
- 所定労働時間を4時間〜8時間の30分刻みで選択
- 出勤・退勤時刻と勤務日の入力
- 実働時間、所定時間を超えた時間、給与の自動計算
- 同じ氏名・勤務日の記録を再登録した場合の上書き
- 月間カレンダーで登録日を確認
- 選択した日の出退勤時刻、実働時間、給与を表示
- 勤務日数、実働時間、所定時間超過、給与の月間集計
- 年月指定、前月・次月ボタンによる過去履歴の表示
- 過去月の閲覧専用表示と、必要に応じた編集許可
- 選択月の勤怠をCSV形式で出力
- 休憩を取れたかを振り返る任意項目
- 入力エラーの画面表示とJSON形式のAPIエラー応答

設定と勤怠記録はサーバー側のCSVファイルへ保存します。

## CLI版とWeb版の位置づけ

| 版 | 位置づけ | 主な内容 |
| --- | --- | --- |
| CLI版 | Java基礎学習のための最初の試作 | コンソール入力、給与計算、CSV保存、月間集計 |
| Web版 | 現在の主な成果物 | ブラウザ操作、基本設定、カレンダー、日別表示、月間集計、履歴、CSV出力 |

CLI版をそのまま画面へ移したのではなく、実際の利用場面を考えてWeb版の入力方法と画面構成を作り直しています。そのため、両者は目的を共有していますが、処理構成と保存するCSV形式は別です。

## 給与計算

現在のWeb版では、設定した所定労働時間を超えた分を「残業時間」として表示し、25%分を追加で計算しています。

```text
実働時間 ＝ 退勤時刻 − 出勤時刻 − 設定した休憩時間
基本給与 ＝ 実働時間 × 時給
残業手当 ＝ 所定労働時間を超えた時間 × 時給 × 25%
支給金額 ＝ 基本給与 ＋ 残業手当
```

金額は1分単位で計算し、1円未満を切り捨てます。

> この計算は学習用に単純化しています。法定時間外労働、深夜・休日労働などを含む正式な給与計算には対応していません。

## 使用技術

| 分類 | 技術 |
| --- | --- |
| 言語 | Java 17、JavaScript |
| フレームワーク | Spring Boot 4.1.0 |
| 画面 | HTML / CSS |
| API | Spring Web |
| ビルド | Maven / Maven Wrapper |
| データ保存 | CSV |
| テスト | JUnit |
| バージョン管理 | Git / GitHub |
| 公開 | Render |

データベースとログイン機能は使用していません。

## Web版の起動方法

### 1. リポジトリを取得

```bash
git clone https://github.com/YSK-do/TimeCard-Payroll-System.git
cd TimeCard-Payroll-System
```

### 2. Spring Bootを起動

macOS / Linux：

```bash
./mvnw spring-boot:run
```

Windows：

```powershell
mvnw.cmd spring-boot:run
```

### 3. ブラウザで開く

```text
http://localhost:8080
```

## テスト

macOS / Linux：

```bash
./mvnw test
```

Windows：

```powershell
mvnw.cmd test
```

現在、次の処理にJUnitテストがあります。

- 設定の保存と読み込み
- 勤怠CSVの新規保存と上書き
- 給与計算
- 月間集計
- 設定サービス

CLI版には `TestRunner` を使用した簡易テストもあります。

## 主なAPI

| メソッド | URL | 内容 |
| --- | --- | --- |
| GET | `/api/settings` | 基本設定の取得 |
| PUT | `/api/settings` | 基本設定の保存 |
| POST | `/api/attendances` | 勤怠の登録 |
| GET | `/api/attendances/summary?month=YYYY-MM` | 月間集計の取得 |
| GET | `/api/attendances/dates?month=YYYY-MM` | 登録済み勤務日の取得 |
| GET | `/api/attendances/detail?date=YYYY-MM-DD` | 選択日の勤務内容の取得 |

## データ保存

| 種類 | 保存先 |
| --- | --- |
| Web版の基本設定 | `data/settings.csv` |
| Web版の勤怠 | `data/attendance.csv` |
| CLI版の基本設定 | `config.csv` |
| CLI版の勤怠 | `worklog.csv` |

Web版では、同じ氏名・勤務日の記録を再登録すると既存の行を上書きします。

## 主な構成

```text
src/main/java/
├── JobManager.java                  # CLI版
├── TestRunner.java                  # CLI版の簡易テスト
└── com/example/timecard/
    ├── TimeCardApplication.java     # Web版の起動クラス
    ├── controller/                  # APIの受付
    ├── domain/                      # データの型
    ├── repository/                  # CSVの読み書き
    └── service/                     # 勤怠・給与・集計処理

src/main/resources/static/
├── index.html
├── app.js
├── monthly-dashboard.js
├── history-controls.js
└── 各画面用のCSS・JavaScript

src/test/java/com/example/timecard/
├── repository/
└── service/
```

## 現在の制限

- 個人利用を想定しており、複数ユーザーのアカウント管理には未対応
- データベースではなくCSVへ保存
- 日をまたぐ勤務には未対応
- 法定時間外労働、深夜労働、休日労働、有給休暇などの正式な勤怠・割増計算には未対応
- 税金、社会保険料、交通費などの控除・手当には未対応
- 休憩を取れたかの回答は振り返り用で、給与計算やCSV保存には使用しない
- CLI版とWeb版は別の処理・CSV形式で動作

## 今後の改善案

- Controllerを含むWeb APIテストの追加
- 入力値チェックとエラー表示の充実
- データベース保存とログイン機能の検討
- 法令や就業規則に合わせた計算ルールの拡張

## 注意事項

このシステムは、JavaとSpring Bootの学習を目的とした個人開発のポートフォリオ作品です。

表示される金額は確認用の概算です。実際の勤怠管理や給与計算に使用する場合は、勤務先の就業規則と最新の法令を確認してください。
