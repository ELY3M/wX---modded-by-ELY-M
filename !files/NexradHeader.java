import java.net.URL;

public interface NexradHeader {
    public static final int GENERIC_RADIAL = -200;
    public static final int L3ALPHA = -33;
    public static final int L3DPA = -34;
    public static final int L3GSM = -36;
    public static final int L3PC_BASE_REFLECTIVITY_124NM = 19;
    public static final int L3PC_BASE_REFLECTIVITY_248NM = 20;
    public static final int L3PC_CLUTTER_FILTER_CONTROL = 34;
    public static final int L3PC_COMPOSITE_REFLECTIVITY_124NM_16LVL = 37;
    public static final int L3PC_COMPOSITE_REFLECTIVITY_248NM_16LVL = 38;
    public static final int L3PC_COMPOSITE_REFLECTIVITY_248NM_8LVL = 36;
    public static final int L3PC_DIGITAL_HYBRID_PRECIP = 138;
    public static final int L3PC_DIGITAL_HYBRID_SCAN = 32;
    public static final int L3PC_DIGITAL_MESOCYCLONE = 141;
    public static final int L3PC_DIGITAL_VERT_INT_LIQUID = 134;
    public static final int L3PC_DPA = 81;
    public static final int L3PC_ECHO_TOPS = 41;
    public static final int L3PC_ENHANCED_ECHO_TOPS = 135;
    public static final int L3PC_GSM = 2;
    public static final int L3PC_HIGH_LAYER_COMP_REFLECTIVITY = 90;
    public static final int L3PC_LONG_RANGE_BASE_REFLECTIVITY_8BIT = 94;
    public static final int L3PC_LONG_RANGE_BASE_VELOCITY_8BIT = 99;
    public static final int L3PC_LOW_LAYER_COMP_REFLECTIVITY = 65;
    public static final int L3PC_MID_LAYER_COMP_REFLECTIVITY = 66;
    public static final int L3PC_ONE_HOUR_PRECIP = 78;
    public static final int L3PC_RADAR_STATUS_LOG = 152;
    public static final int L3PC_SPECTRUM_WIDTH_124NM = 30;
    public static final int L3PC_SPECTRUM_WIDTH_32NM = 28;
    public static final int L3PC_STORM_RELATIVE_VELOCITY_124NM = 56;
    public static final int L3PC_STORM_TOTAL_PRECIP = 80;
    public static final int L3PC_TDWR_BASE_REFLECTIVITY = 181;
    public static final int L3PC_TDWR_BASE_REFLECTIVITY_8BIT = 180;
    public static final int L3PC_TDWR_BASE_VELOCITY_8BIT = 182;
    public static final int L3PC_TDWR_LONG_RANGE_BASE_REFLECTIVITY_8BIT = 186;
    public static final int L3PC_THREE_HOUR_PRECIP = 79;
    public static final int L3PC_VELOCITY_124NM = 27;
    public static final int L3PC_VELOCITY_32NM = 25;
    public static final int L3PC_VERTICAL_WIND_PROFILE = 48;
    public static final int L3PC_VERT_INT_LIQUID = 57;
    public static final int L3RADIAL = -31;
    public static final int L3RADIAL_8BIT = -38;
    public static final int L3RASTER = -32;
    public static final int L3RSL = -37;
    public static final int L3VAD = -35;
    public static final int LEVEL2 = -20;
    public static final int LEVEL2_CORRELATIONCOEFFICIENT = -25;
    public static final int LEVEL2_DIFFERENTIALPHASE = -26;
    public static final int LEVEL2_DIFFERENTIALREFLECTIVITY = -24;
    public static final int LEVEL2_REFLECTIVITY = -21;
    public static final int LEVEL2_SPECTRUMWIDTH = -23;
    public static final int LEVEL2_VELOCITY = -22;
    public static final int NO_SITE_DEFINED = -999999;
    public static final int Q2_NATL_MOSAIC_3DREFL = 6910;
    public static final int UNKNOWN = -1;
    public static final int XMRG = -50;

    void decodeHeader(URL url) throws Exception;

    double getAlt();

    String getDataThresholdString(int i);

    int getDate();

    short getHour();

    String getHourString();

    String getICAO();

    double getLat();

    double getLon();

    long getMilliseconds();

    short getMinute();

    String getMinuteString();

    RadarHashtables getNexradHashtables();

    URL getNexradURL();

    int getNumberOfLevels(boolean z);

    char getOpMode();

    short getProductCode();

    int getProductType();

    RandomAccessFile getRandomAccessFile();

    short getSecond();

    String getSecondString();

    short getVCP();

    float getVersion();
}
