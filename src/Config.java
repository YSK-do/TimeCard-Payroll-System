public class Config {
    private final String name;
    private final int hourlyWage;
    private final int contractMinutes;
    private final int breakMinutes;

    public Config(String name, int hourlyWage, int contractMinutes, int breakMinutes) {
        this.name = name;
        this.hourlyWage = hourlyWage;
        this.contractMinutes = contractMinutes;
        this.breakMinutes = breakMinutes;
    }

    public Config(String csvLine) {
        String[] data = csvLine.split(",");
        this.name = data[0];
        this.hourlyWage = Integer.parseInt(data[1]);
        this.contractMinutes = Integer.parseInt(data[2]);
        this.breakMinutes = Integer.parseInt(data[3]);
    }

    public String toCsv() {
        return name + "," + hourlyWage + "," + contractMinutes + "," + breakMinutes;
    }

    public String getName() {
        return name;
    }

    public int getHourlyWage() {
        return hourlyWage;
    }

    public int getContractMinutes() {
        return contractMinutes;
    }

    public int getBreakMinutes() {
        return breakMinutes;
    }
}
