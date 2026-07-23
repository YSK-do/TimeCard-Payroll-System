public class MyTime {
    private final int hour;
    private final int minute;

    public MyTime(String timeText) {
        String cleaned = InputNormalizer.toHalfWidth(timeText).replace("：", ":");
        String[] parts = cleaned.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("時刻は「時:分」の形式で入力してください。");
        }
        this.hour = Integer.parseInt(parts[0]);
        this.minute = Integer.parseInt(parts[1]);
        if (hour < 0 || minute < 0 || minute >= 60) {
            throw new IllegalArgumentException("正しい時刻を入力してください。");
        }
    }

    public int toTotalMinutes() {
        return hour * 60 + minute;
    }

    public int diff(MyTime other) {
        return toTotalMinutes() - other.toTotalMinutes();
    }
}
