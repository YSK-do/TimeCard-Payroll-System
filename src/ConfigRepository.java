import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class ConfigRepository {
    private static final String CONFIG_FILE = "config.csv";

    public Config loadOrCreate(Scanner scanner) {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) {
            return create(scanner, file);
        }
        return load(file);
    }

    private Config create(Scanner scanner, File file) {
        System.out.println("【初期設定】初回起動のため、基本情報を登録してください。");
        System.out.print("社員名を入力してください：");
        String name = scanner.nextLine();

        System.out.print("時給を入力してください（例:1200）：");
        int hourlyWage = Integer.parseInt(InputNormalizer.toHalfWidth(scanner.nextLine()));

        System.out.print("1日の契約労働時間を入力してください（例:8:00）：");
        int contractMinutes = new MyTime(scanner.nextLine()).toTotalMinutes();

        System.out.print("標準の休憩時間を入力してください（例:1:00）：");
        int breakMinutes = new MyTime(scanner.nextLine()).toTotalMinutes();

        Config config = new Config(name, hourlyWage, contractMinutes, breakMinutes);
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(config.toCsv() + System.lineSeparator());
            System.out.println("初期設定を保存しました。");
        } catch (IOException exception) {
            throw new IllegalStateException("設定ファイルの保存中にエラーが発生しました。", exception);
        }
        return config;
    }

    private Config load(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            if (line == null) {
                throw new IllegalStateException("設定ファイルが空です。");
            }
            Config config = new Config(line);
            System.out.println("【ようこそ、" + config.getName() + " さん】");
            System.out.println("時給:" + config.getHourlyWage() + "円 / 契約労働時間:"
                    + config.getContractMinutes() / 60 + "時間 / 設定休憩時間:"
                    + config.getBreakMinutes() / 60 + "時間");
            return config;
        } catch (IOException exception) {
            throw new IllegalStateException("設定ファイルの読み込み中にエラーが発生しました。", exception);
        }
    }
}
