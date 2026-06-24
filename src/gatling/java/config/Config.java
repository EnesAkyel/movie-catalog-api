package config;

public class Config {

    public static final String BASE_URL = "http://localhost:8080";
    public static final int DEFAULT_USERS       = 5;
    public static final int RAMP_DURATION_SEC   = 10;
    public static final int TEST_DURATION_SEC   = 15;
    public static final int PEAK_USERS          = 10;
    public static final double MAX_RESPONSE_TIME_MS   = 60000;
    public static final double MAX_ERROR_RATE_PERCENT = 1.0;
    public static final double PERCENTILE_95_MS       = 1500;

    private Config() {}
}
