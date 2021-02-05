package solar.model;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * Solar irradiance sums.
 *
 * @threadsafety Immutable
 */
public class Calculator {

    private static final double TROPIC = 23.45; // degrees
    private static final double SPRINGEQUINOX = 81.0; // julian days
    private static final double DAYSPERYEAR = 365.0;
    private static final String[] MONTHNAMES = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * Sensible default values?
     */
    public static final double CN = 1.0;
    /**
     * Sensible default values?
     */
    public static final double SURFACE_REFLECTIVITY = 0.2;
    /**
     * Ordered by month
     */
    /**
     * Originally I was given the middle 12 values and told they were monthly. I
     * assume they are averages for the month, so to interpolate I put the nodes
     * at 1/2 months. Also my naive date calculations sometimes end up as just
     * negative or just over 11, so expand the range to make it more circular by
     * putting a December at the start and a January at the end.
     */
    private static final double[] months = new double[]{-0.5, 0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 7.5, 8.5, 9.5, 10.5, 11.5, 12.5};
    private static final double[] skyDiffusionFactors
            = {0.057, 0.058, 0.060, 0.071, 0.097, 0.121, 0.134, 0.136, 0.122, 0.092, 0.073, 0.063, 0.057, 0.058};
    private static final double[] attmosphericAttenuations
            = {0.142, 0.142, 0.144, 0.156, 0.180, 0.196, 0.205, 0.207, 0.201, 0.177, 0.160, 0.149, 0.142, 0.142};
    // Sunny day fractions by month. See Excel
    // Treat them as a constant value for the month? Or a random number with the specified mean?
    // Use triangular: https://en.wikipedia.org/wiki/Triangular_distribution 
    // https://commons.apache.org/proper/commons-math/javadocs/api-3.5/org/apache/commons/math3/distribution/TriangularDistribution.html
    // no-not triangular because its mean is between 1/3 and 2/3
    //private static final double[] sunnyDays = {0.27, 0.31, 0.37, 0.48, 0.49, 0.47, 0.47, 0.47, 0.41, 0.34, 0.31, 0.25};
    private static final double[] sunnyDayFactors
            = {0.26, 0.27, 0.31, 0.37, 0.48, 0.49, 0.47, 0.47, 0.47, 0.41, 0.34, 0.31, 0.25, 0.26};
    /**
     * Calendar data. TODO: Remove. Use java.Date?
     */
    private static final int daysPerMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    public static int daysPerMonth(int month) {
        if (month < 0) {
            throw new IllegalArgumentException("Too small " + month);
        }
        if (month > 11) {
            throw new IllegalArgumentException("Too big " + month);
        }
        return daysPerMonth[month];
    }

    private static PolynomialSplineFunction attenuation;
    private static PolynomialSplineFunction diffusion;
    private static PolynomialSplineFunction weather;

    public Calculator() {
        SplineInterpolator li = new SplineInterpolator();
        attenuation = li.interpolate(months, attmosphericAttenuations);
        diffusion = li.interpolate(months, skyDiffusionFactors);
        weather = li.interpolate(months, sunnyDayFactors);
    }

    public double getAttenuation(int dayNumber) {
        return attenuation.value(dayNumber / (DAYSPERYEAR / 12.0));
    }

    public double getDiffusion(int dayNumber) {
        return diffusion.value(dayNumber / (DAYSPERYEAR / 12.0));
    }

    public double getWeatherFactorSmoothed(int dayNumber) {
        return weather.value(dayNumber / (DAYSPERYEAR / 12.0));
    }

    public double getWeatherFactorRecorded(int dayNumber) {
        return pv1Factors[dayNumber] + pv2Factors[dayNumber] + pv3Factors[dayNumber];
    }

    /**
     *
     * KA: Returns the solar declination angle in degrees. This is the offset to
     * be applied to the latitude due to the tilt of the earth.
     *
     * https://www.pveducation.org/pvcdrom/properties-of-sunlight/declination-angle
     *
     * @param dayNumber 0-364
     *
     * @return The solar declination angle in degrees
     */
    public double solarDeclination(int dayNumber) {
        return -TROPIC * Math.cos(Math.toRadians((360.0 * (10.0 + dayNumber)) / DAYSPERYEAR));
    }

    /**
     * (Absolute?) Solar Time
     *
     * @param lst Local solar time in whole HOURS (why?)
     * @param et minutes offset due to wobble
     * @param standardMeridian local meridian, degrees
     * @param longitude Longitude, degrees
     *
     * @return Solar Time in MINUTES
     */
    public double solarTime(double lst, double et, double standardMeridian, double longitude) {
        double lstMin = lst * 60;
        return (lstMin + et + 4 * (standardMeridian - longitude));
    }

    /**
     * The equation of time (EoT) (in minutes) is an empirical equation that
     * corrects for the eccentricity of the Earth's orbit and the Earth's axial
     * tilt. This approximation is accurate to within ½ minute.
     *
     * https://www.pveducation.org/pvcdrom/properties-of-sunlight/solar-time
     *
     * Note max value is 15 minutes so can usually ignore
     *
     * @param dayNumber Day number 0-364
     *
     * @return
     */
    public double timeEquation(int dayNumber) {
        double b = (360.0 * (dayNumber - SPRINGEQUINOX)) / DAYSPERYEAR;
        return 9.87 * Math.sin(Math.toRadians(2 * b)) - 7.53 * Math.cos(Math.toRadians(b)) - 1.5 * Math.sin(Math.toRadians(b));
    }

    /**
     * Convert solar time to an equivalent rotation of the earth.
     *
     * @param solarTimeInMinutes Solar time in minutes
     * @return degrees
     */
    private double timeAngle(double solarTimeInMinutes) {
        return (solarTimeInMinutes - 720) / 4;
    }

    /**
     * Elevation angle of the sun. For maximum elevation angle at solar noon,
     * set hAngle = 0. Was alpha.
     *
     * https://www.pveducation.org/pvcdrom/properties-of-sunlight/elevation-angle
     *
     * @param latitude Latitude, degrees
     * @param sDec Declination, degrees
     * @param hAngle Time angle, degrees
     *
     * @return Elevation angle of the sun in degrees
     */
    public double sunElevation(double latitude, double sDec, double hAngle) {
        double temp = Math.asin(Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(sDec)) * Math.cos(Math.toRadians(hAngle)) + Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(sDec)));
        return Math.toDegrees(temp);
    }

    /**
     * The solar zenith angle. The zenith angle is the angle between the sun and
     * the vertical.
     *
     * @param elevation Elevation in degrees
     * @return The solar zenith angle in degrees
     */
    //
    public double solarZenith(double elevation) {
        return (90.0 - elevation);
    }

    /**
     *
     * Azimuth angle of the sun. The above equation only gives the correct
     * azimuth in the solar morning. Was alphaS
     *
     * https://www.pveducation.org/pvcdrom/properties-of-sunlight/azimuth-angle
     *
     * @param sDec Declination
     * @param hAngle solar hour as an angle
     * @param alpha Elevation
     * @return Azimuth angle of the sun in degrees
     */
    public double sunAzimuth(double sDec, double hAngle, double alpha) {
        double temp = Math.asin((Math.cos(Math.toRadians(sDec)) * Math.sin(Math.toRadians(hAngle))) / Math.cos(Math.toRadians(alpha)));
        return Math.toDegrees(temp);
    }

    /**
     * Time from solar noon to sunset (numerically the same as sunrise to solar
     * noon)
     *
     * @param latitude Latitude, degrees
     * @param sDec Declination, degrees
     * @return Time in minutes
     */
    public double hssNhsr(double latitude, double sDec) {
        double temp = Math.acos(-Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(sDec)));
        return Math.toDegrees(temp) * 4;
    }

    /**
     * Gives the extraterrestrial solar radiation in W/m2 Corrected from
     * https://www.pveducation.org/pvcdrom/properties-of-sunlight/solar-radiation-outside-the-earths-atmosphere
     *
     * @param dayNumber Day number, 0-364
     * @return the extraterrestrial solar radiation in W/m2
     */
    public double solarIrradiance(int dayNumber) {
        return (1353.0 * (1 + 0.033 * Math.cos(Math.toRadians(360.0 * ((dayNumber - 2) / 365.0)))));
    }

    /**
     *
     * @param solarTime
     * @param et Time Equation
     * @param standardMeridian
     * @param longitude
     * @return sunset or sunrise(Local Time) - MINUTES
     */
    public double sunriseOrSunset(double solarTime, double et, double standardMeridian, double longitude) {
        return (solarTime - et - 4 * (standardMeridian - longitude));
    }

    /**
     * Beam normal irradiance (ie pointing at the sun)
     *
     * @param cn
     * @param extraterrestialRadiance
     * @param k
     * @param sunElevation
     * @return irradiance W/m2
     */
    private static double beamNormalIrradiance(double cn, double extraterrestialRadiance, double k, double sunElevation) {
        double exponent = -k / Math.sin(Math.toRadians(sunElevation));
        double temp = (cn * extraterrestialRadiance * Math.exp(exponent));
        if (temp > 1353) {
            return 0.0;
        } else {
            return temp;
        }
    }

    /**
     * Array angle factor
     *
     * @param sunElevation Elevation
     * @param sunAzimuth Azimuth
     * @param panelAzimuth Degrees
     * @param panelTilt Degrees
     * @return Cos of 3D angle between sun and the array
     */
    private double cosI(double sunElevation, double sunAzimuth, double panelAzimuth, double panelTilt) {
        return Math.cos(Math.toRadians(sunElevation)) * Math.cos(Math.toRadians(sunAzimuth - panelAzimuth)) * Math.sin(Math.toRadians(panelTilt)) + Math.sin(Math.toRadians(sunElevation)) * Math.cos(Math.toRadians(panelTilt));
    }

    /**
     * sky diffuse radiation
     *
     * Note: Can't find this formula
     *
     * @param diffusionFactor Scaling factor, depends on month.
     * @param normalIrradiance
     * @param panelTilt Array angle to horizontal
     * @return sky diffuse radiation W/m2
     */
    private double diffuseIrradiance(double diffusionFactor, double normalIrradiance, double panelTilt) {
        return diffusionFactor * normalIrradiance * Math.pow(Math.cos(Math.toRadians(panelTilt / 2.0)), 2);
    }

    /**
     * ground reflected radiation
     *
     * @param rho
     * @param normalIrradiance W/m2
     * @param sunElevation degrees
     * @param diffusionFactor
     * @param panelTilt degrees
     * @return ground reflected radiation W/m2
     */
    private double groundReflected(
            double rho,
            double normalIrradiance,
            double sunElevation,
            double diffusionFactor,
            double panelTilt) {
        return (rho
                * normalIrradiance
                * (Math.sin(Math.toRadians(sunElevation)) + diffusionFactor)
                * Math.pow(Math.sin(Math.toRadians(panelTilt / 2.0)), 2));
    }

    /**
     * Get the day number from the month and day in the month
     *
     * @param month Month 0-11
     * @param date Day in month, 1-31
     * @return Day number, 0-364
     */
    public static int dayNumber(int month, int date) {
        if (month < 0) {
            throw new IllegalArgumentException("Too small " + month);
        }
        if (month > 11) {
            throw new IllegalArgumentException("Too big " + month);
        }
        if (date < 1) {
            throw new IllegalArgumentException("Too small " + date);
        }
        if (date > 31) {
            throw new IllegalArgumentException("Too big " + date);
        }
        int out = 0;
        for (int i = 0; i < month; i++) {
            out += daysPerMonth[i];
        }
        out += date-1;
        return out;
    }

    /**
     * Print time
     *
     * @param hrs Time in hours, like 7.533
     * @return Formatted string like "7:32 PM"
     */
    public String formatHours(double hrs) {
        int outHours = (int) (hrs);
        int outMinutes = (int) Math.round((hrs - outHours) * 60);
        if (outHours > 12) {
            return (outHours - 12) + ":" + outMinutes + " PM";
        } else {
            return outHours + ":" + outMinutes + " AM";
        }
    }

    /**
     * Sunrise in local time, ie 7.5 = 7:30 AM
     *
     * @param dayNumber 0-364
     * @param latitude degrees
     * @param standardMeridian degrees
     * @param longitude degrees
     * @return Local time in hours
     */
    public double sunrise(int dayNumber, double latitude, double standardMeridian, double longitude) {
        double sDec = solarDeclination(dayNumber);
        double et = timeEquation(dayNumber);
        double hsr = hssNhsr(latitude, sDec);// hsr is in MINUTES
        double solarSunrise = 720 - hsr;
        double sunrise = solarSunrise - et - 4 * (standardMeridian - longitude);
        return sunrise / 60.0;
    }

    /**
     * Time of sunset in local time, ie 18.5 = 6:30 PM
     *
     * @param dayNumber 0-364
     * @param latitude degrees
     * @param standardMeridian degrees
     * @param longitude degrees
     * @return Local time in hours
     */
    public double sunset(int dayNumber, double latitude, double standardMeridian, double longitude) {
        double sDec = solarDeclination(dayNumber);
        double et = timeEquation(dayNumber);
        double hss = hssNhsr(latitude, sDec);// hss is in MINUTES
        double solarSunset = hss;
        double sunset = solarSunset - et - 4 * (standardMeridian - longitude);
        return 12.0 + sunset / 60.0;
    }

    /**
     * returns the instantaneous solar radiation
     *
     * @param localSolarTime Local solar time in hours
     * @param longitude Longitude, degrees
     * @param latitude Latitude, degrees
     * @param standardMeridian Closest meridian
     * @param panelTilt Panel tilt (relative to horizontal?) degrees
     * @param panelAzimuth Panel direction (relative to south?) degrees
     * @param dayNumber day number 0-364
     * @param cn Scaling for irradiance. Clouds?
     * @param rho Scaling for reflection. Surface color?
     *
     * @return The instantaneous solar radiation
     */
    public double insSolarRadiation(double localSolarTime, double latitude, double longitude, double standardMeridian, double panelTilt, double panelAzimuth, int dayNumber, double cn, double rho) {

        // close enough-DEGREES
        double earthTilt = solarDeclination(dayNumber);

        // Offset due to wobble-MINUTES
        double eqTime = timeEquation(dayNumber);

        // Absolute solar time-MINUTES
        double solarTime = solarTime(localSolarTime, eqTime, standardMeridian, longitude);

        // solar angle relative to solar noon-DEGREES
        double hourAngle = timeAngle(solarTime);

        // DEGREES
        double sunElevation = sunElevation(latitude, earthTilt, hourAngle);

        // DEGREES
        double sunAzimuth = sunAzimuth(earthTilt, hourAngle, sunElevation);

        // W/m2
        double extraterrestialRadiance = solarIrradiance(dayNumber);

        // factor 0-1
        double attenuationFactor = getAttenuation(dayNumber);

        // W/m2
        double directIrradiance = beamNormalIrradiance(cn, extraterrestialRadiance, attenuationFactor, sunElevation);

        // cos law for the actual incidence angle (in 3D)
        double cosI = cosI(sunElevation, sunAzimuth, panelAzimuth, panelTilt);

        // W/m2
        double incidentIrradiance = directIrradiance * cosI;

        // factor 0-1
        double skyDiffusionFactor = getDiffusion(dayNumber);

        // W/m2
        double skyDiffuse = diffuseIrradiance(skyDiffusionFactor, directIrradiance, panelTilt);

        // W/m2
        double groundReflected = groundReflected(rho, directIrradiance, sunElevation, skyDiffusionFactor, panelTilt);

        // W/m2
        return incidentIrradiance + skyDiffuse + groundReflected;
    }

    public static String monthName(int month) {
        if (month < 0) {
            throw new IllegalArgumentException("Too small " + month);
        }
        if (month > 11) {
            throw new IllegalArgumentException("Too big " + month);
        }
        return MONTHNAMES[month];
    }

    // CHECK ME
    public double getSunElevation(double latitude, int daynumber, double hour) {
        double hourAngle = 15 * (hour - 12);
        double declination = solarDeclination(daynumber);
        return sunElevation(latitude, declination, hourAngle);
    }

    // CHECK ME
    public double getSunAzimuth(double latitude, int daynumber, double hour) {
        double hourAngle = 15 * (hour - 12);
        double declination = solarDeclination(daynumber);
        double elevation = sunElevation(latitude, declination, hourAngle);
        return sunAzimuth(declination, hourAngle, elevation);
    }

    private final double[] pv1Factors = {
        0.0609,
        0.0693,
        0.0189,
        0.063,
        0.0084,
        0.0105,
        0.0525,
        0.0147,
        0.0588,
        0.0735,
        0.042,
        0.0168,
        0.0252,
        0.0441,
        0.0336,
        0.0084,
        0.0231,
        0.0546,
        0.0042,
        0.0441,
        0.0063,
        0.0378,
        0.0441,
        0.0063,
        0.0063,
        0.0273,
        0.0567,
        0.0651,
        0.0252,
        0.0441,
        0.0609,
        0.0693,
        0.0189,
        0.063,
        0.0084,
        0.0105,
        0.0525,
        0.0147,
        0.0588,
        0.0735,
        0.042,
        0.0168,
        0.0252,
        0.0441,
        0.0336,
        0.0084,
        0.0231,
        0.0546,
        0.0042,
        0.0441,
        0.0063,
        0.0378,
        0.0441,
        0.0063,
        0.0063,
        0.0273,
        0.0567,
        0.0651,
        0.0252,
        0.0441,
        0.0168,
        0.0714,
        0.0357,
        0.1092,
        0.0903,
        0.0903,
        0.0735,
        0.0966,
        0.08505,
        0.05355,
        0.04725,
        0.063,
        0.04095,
        0.07875,
        0.10395,
        0.00945,
        0.0504,
        0.04725,
        0.02835,
        0.0063,
        0.0882,
        0.02205,
        0.02835,
        0.0378,
        0.0567,
        0.1008,
        0.07245,
        0.02835,
        0.0504,
        0.2772,
        0.03465,
        0.1512,
        0.19215,
        0.02835,
        0.0378,
        0.08505,
        0.2331,
        0.15435,
        0.1575,
        0.1701,
        0.16695,
        0.16695,
        0.0693,
        0.19845,
        0.14805,
        0.2394,
        0.14175,
        0.09135,
        0.07875,
        0.10395,
        0.19845,
        0.07245,
        0.2079,
        0.10395,
        0.02205,
        0.17955,
        0.1953,
        0.1071,
        0.05985,
        0.02205,
        0.252,
        0.3255,
        0.2331,
        0.084,
        0.2226,
        0.168,
        0.2709,
        0.2499,
        0.2793,
        0.2709,
        0.1701,
        0.2016,
        0.2394,
        0.2331,
        0.2709,
        0.3129,
        0.3318,
        0.3402,
        0.3255,
        0.3528,
        0.399,
        0.3318,
        0.3801,
        0.3213,
        0.3969,
        0.4116,
        0.3864,
        0.4074,
        0.3864,
        0.3927,
        0.3843,
        0.4011,
        0.3738,
        0.3549,
        0.1134,
        0.1932,
        0.3234,
        0.1743,
        0.3423,
        0.2646,
        0.3402,
        0.2373,
        0.2583,
        0.1386,
        0.1785,
        0.3927,
        0.3696,
        0.3507,
        0.3738,
        0.1008,
        0.2604,
        0.3465,
        0.3738,
        0.3654,
        0.3969,
        0.399,
        0.3759,
        0.1659,
        0.2898,
        0.3318,
        0.2541,
        0.0945,
        0.2499,
        0.3318,
        0.231,
        0.0966,
        0.4179,
        0.2247,
        0.3045,
        0.2121,
        0.1197,
        0.2625,
        0.3927,
        0.4011,
        0.2457,
        0.1974,
        0.1533,
        0.3486,
        0.3528,
        0.357,
        0.1932,
        0.357,
        0.3444,
        0.3675,
        0.2268,
        0.3402,
        0.1008,
        0.2226,
        0.1995,
        0.3633,
        0.3192,
        0.357,
        0.1932,
        0.336,
        0.2499,
        0.2814,
        0.2856,
        0.1932,
        0.3024,
        0.1386,
        0.2877,
        0.2898,
        0.1722,
        0.2982,
        0.2814,
        0.1197,
        0.0525,
        0.0504,
        0.1428,
        0.2268,
        0.3297,
        0.0567,
        0.3255,
        0.2226,
        0.2667,
        0.1806,
        0.1638,
        0.294,
        0.273,
        0.1659,
        0.2457,
        0.2121,
        0.1974,
        0.2457,
        0.1785,
        0.1365,
        0.0525,
        0.1701,
        0.2268,
        0.1638,
        0.1344,
        0.2667,
        0.1344,
        0.2289,
        0.1449,
        0.2226,
        0.2457,
        0.2415,
        0.1386,
        0.21,
        0.2184,
        0.1806,
        0.1071,
        0.2079,
        0.2079,
        0.0546,
        0.0798,
        0.1113,
        0.1911,
        0.1701,
        0.1407,
        0.1197,
        0.1848,
        0.0231,
        0.1008,
        0.1281,
        0.0189,
        0.0252,
        0.0567,
        0.1554,
        0.1029,
        0.105,
        0.1134,
        0.1113,
        0.1113,
        0.0462,
        0.1323,
        0.0987,
        0.1596,
        0.0945,
        0.0609,
        0.0525,
        0.0693,
        0.1323,
        0.0483,
        0.1386,
        0.0693,
        0.0147,
        0.1197,
        0.1302,
        0.0714,
        0.0399,
        0.0147,
        0.0168,
        0.0714,
        0.0357,
        0.1092,
        0.0903,
        0.0903,
        0.0735,
        0.0966,
        0.0567,
        0.0357,
        0.0315,
        0.042,
        0.0273,
        0.0525,
        0.0693,
        0.0063,
        0.0336,
        0.0315,
        0.0189,
        0.0042,
        0.0588,
        0.0147,
        0.0189,
        0.0252,
        0.0378,
        0.0672,
        0.0483,
        0.0189,
        0.0336,
        0.0609,
        0.0693,
        0.0189,
        0.063,
        0.0084,
        0.0105,
        0.0525,
        0.0147,
        0.0588,
        0.0735,
        0.042,
        0.0168,
        0.0252,
        0.0441,
        0.0336,
        0.0084,
        0.0231,
        0.0546,
        0.0042,
        0.0441,
        0.0063,
        0.0378,
        0.0441,
        0.0063,
        0.0063,
        0.0273,
        0.0567,
        0.0651,
        0.0252,
        0.0441,
        0.0567,
        0.0651,
        0.0252
    };
    private final double[] pv2Factors = {
        0.042,
        0.0756,
        0.0315,
        0.0714,
        0.0084,
        0.0084,
        0.0294,
        0.0126,
        0.0693,
        0.0294,
        0.0798,
        0.0189,
        0.0399,
        0.0525,
        0.0651,
        0.0084,
        0.0294,
        0.0588,
        0.0042,
        0.0693,
        0.0063,
        0.0462,
        0.0462,
        0.0042,
        0.0084,
        0.0357,
        0.0756,
        0.0735,
        0.0336,
        0.0525,
        0.042,
        0.0756,
        0.0315,
        0.0714,
        0.0084,
        0.0084,
        0.0294,
        0.0126,
        0.0693,
        0.0294,
        0.0798,
        0.0189,
        0.0399,
        0.0525,
        0.0651,
        0.0084,
        0.0294,
        0.0588,
        0.0042,
        0.0693,
        0.0063,
        0.0462,
        0.0462,
        0.0042,
        0.0084,
        0.0357,
        0.0756,
        0.0735,
        0.0336,
        0.0525,
        0.0189,
        0.0294,
        0.0567,
        0.1113,
        0.0756,
        0.1323,
        0.126,
        0.1071,
        0.06615,
        0.04725,
        0.07875,
        0.1386,
        0.04095,
        0.1449,
        0.1512,
        0.00945,
        0.0819,
        0.04725,
        0.0315,
        0.0126,
        0.11655,
        0.0252,
        0.0315,
        0.03465,
        0.0441,
        0.09135,
        0.0441,
        0.04095,
        0.063,
        0.31185,
        0.0378,
        0.34965,
        0.12285,
        0.0315,
        0.0378,
        0.09765,
        0.2016,
        0.24255,
        0.1575,
        0.27405,
        0.26145,
        0.28665,
        0.0819,
        0.24885,
        0.2898,
        0.29925,
        0.26145,
        0.2646,
        0.063,
        0.19845,
        0.1323,
        0.0693,
        0.16695,
        0.1008,
        0.02205,
        0.14175,
        0.17955,
        0.0567,
        0.063,
        0.0252,
        0.189,
        0.3297,
        0.3633,
        0.0882,
        0.1638,
        0.105,
        0.3843,
        0.3507,
        0.315,
        0.3528,
        0.2688,
        0.3486,
        0.3297,
        0.3591,
        0.4011,
        0.399,
        0.315,
        0.3906,
        0.4116,
        0.2583,
        0.4305,
        0.4095,
        0.2856,
        0.4179,
        0.3822,
        0.4284,
        0.4326,
        0.4473,
        0.4053,
        0.4179,
        0.42,
        0.4326,
        0.4326,
        0.42,
        0.1092,
        0.3129,
        0.3276,
        0.3486,
        0.4011,
        0.4074,
        0.4704,
        0.2205,
        0.2457,
        0.1428,
        0.3591,
        0.2415,
        0.4305,
        0.231,
        0.2667,
        0.1029,
        0.2058,
        0.2625,
        0.4263,
        0.3843,
        0.4305,
        0.4389,
        0.4305,
        0.1428,
        0.2709,
        0.3969,
        0.2709,
        0.0987,
        0.3129,
        0.4074,
        0.3822,
        0.1029,
        0.3423,
        0.3129,
        0.3738,
        0.21,
        0.1197,
        0.3675,
        0.4263,
        0.4179,
        0.378,
        0.2709,
        0.1701,
        0.2268,
        0.4158,
        0.2814,
        0.1617,
        0.4242,
        0.4011,
        0.3969,
        0.3423,
        0.2247,
        0.0735,
        0.3759,
        0.1071,
        0.3402,
        0.4095,
        0.4032,
        0.3822,
        0.3255,
        0.378,
        0.3696,
        0.3381,
        0.2415,
        0.1281,
        0.3108,
        0.336,
        0.3339,
        0.2625,
        0.336,
        0.3549,
        0.1995,
        0.0588,
        0.0504,
        0.0882,
        0.2919,
        0.2562,
        0.0567,
        0.3675,
        0.3129,
        0.3045,
        0.1974,
        0.2142,
        0.1638,
        0.315,
        0.0819,
        0.294,
        0.2688,
        0.3339,
        0.2268,
        0.3402,
        0.2184,
        0.0546,
        0.2058,
        0.273,
        0.2709,
        0.2835,
        0.1932,
        0.1848,
        0.231,
        0.2331,
        0.252,
        0.2625,
        0.2772,
        0.2205,
        0.2184,
        0.2625,
        0.2898,
        0.1953,
        0.2016,
        0.2436,
        0.0609,
        0.0714,
        0.1155,
        0.2751,
        0.2625,
        0.2709,
        0.126,
        0.2079,
        0.0252,
        0.2331,
        0.0819,
        0.021,
        0.0252,
        0.0651,
        0.1344,
        0.1617,
        0.105,
        0.1827,
        0.1743,
        0.1911,
        0.0546,
        0.1659,
        0.1932,
        0.1995,
        0.1743,
        0.1764,
        0.042,
        0.1323,
        0.0882,
        0.0462,
        0.1113,
        0.0672,
        0.0147,
        0.0945,
        0.1197,
        0.0378,
        0.042,
        0.0168,
        0.0189,
        0.0294,
        0.0567,
        0.1113,
        0.0756,
        0.1323,
        0.126,
        0.1071,
        0.0441,
        0.0315,
        0.0525,
        0.0924,
        0.0273,
        0.0966,
        0.1008,
        0.0063,
        0.0546,
        0.0315,
        0.021,
        0.0084,
        0.0777,
        0.0168,
        0.021,
        0.0231,
        0.0294,
        0.0609,
        0.0294,
        0.0273,
        0.042,
        0.042,
        0.0756,
        0.0315,
        0.0714,
        0.0084,
        0.0084,
        0.0294,
        0.0126,
        0.0693,
        0.0294,
        0.0798,
        0.0189,
        0.0399,
        0.0525,
        0.0651,
        0.0084,
        0.0294,
        0.0588,
        0.0042,
        0.0693,
        0.0063,
        0.0462,
        0.0462,
        0.0042,
        0.0084,
        0.0357,
        0.0756,
        0.0735,
        0.0336,
        0.0525,
        0.0756,
        0.0735,
        0.0336};
    private final double[] pv3Factors = {
        0.0273,
        0.0399,
        0.0126,
        0.0357,
        0.0042,
        0.0042,
        0.0231,
        0.0084,
        0.0336,
        0.0273,
        0.0336,
        0.0105,
        0.0168,
        0.0252,
        0.0273,
        0.0042,
        0.0147,
        0.0315,
        0.0021,
        0.0315,
        0.0042,
        0.0231,
        0.0252,
        0.0021,
        0.0042,
        0.0168,
        0.0357,
        0.0378,
        0.0168,
        0.0252,
        0.0273,
        0.0399,
        0.0126,
        0.0357,
        0.0042,
        0.0042,
        0.0231,
        0.0084,
        0.0336,
        0.0273,
        0.0336,
        0.0105,
        0.0168,
        0.0252,
        0.0273,
        0.0042,
        0.0147,
        0.0315,
        0.0021,
        0.0315,
        0.0042,
        0.0231,
        0.0252,
        0.0021,
        0.0042,
        0.0168,
        0.0357,
        0.0378,
        0.0168,
        0.0252,
        0.0105,
        0.0273,
        0.0252,
        0.0588,
        0.0441,
        0.0609,
        0.0546,
        0.0546,
        0.04095,
        0.02835,
        0.03465,
        0.05355,
        0.02205,
        0.05985,
        0.0693,
        0.0063,
        0.03465,
        0.0252,
        0.01575,
        0.0063,
        0.0567,
        0.0126,
        0.01575,
        0.0189,
        0.02835,
        0.0504,
        0.0315,
        0.0189,
        0.0315,
        0.1575,
        0.0189,
        0.13545,
        0.08505,
        0.01575,
        0.0189,
        0.0504,
        0.11655,
        0.1071,
        0.08505,
        0.1197,
        0.11655,
        0.12285,
        0.04095,
        0.1197,
        0.1197,
        0.1449,
        0.11025,
        0.09765,
        0.0378,
        0.0819,
        0.0882,
        0.0378,
        0.1008,
        0.0567,
        0.0126,
        0.0882,
        0.1008,
        0.0441,
        0.03465,
        0.0126,
        0.1197,
        0.1764,
        0.1617,
        0.0462,
        0.105,
        0.0735,
        0.1764,
        0.1617,
        0.1596,
        0.168,
        0.1176,
        0.1491,
        0.1533,
        0.1596,
        0.1806,
        0.1932,
        0.1743,
        0.1974,
        0.1995,
        0.1659,
        0.2247,
        0.1995,
        0.1806,
        0.1995,
        0.21,
        0.2268,
        0.2205,
        0.231,
        0.2142,
        0.2184,
        0.2163,
        0.2247,
        0.2184,
        0.21,
        0.0609,
        0.1365,
        0.1764,
        0.1407,
        0.2016,
        0.1806,
        0.2184,
        0.1239,
        0.1365,
        0.0756,
        0.1449,
        0.1722,
        0.2163,
        0.1575,
        0.1722,
        0.0546,
        0.126,
        0.1638,
        0.2163,
        0.2016,
        0.2226,
        0.2268,
        0.2184,
        0.084,
        0.1512,
        0.1974,
        0.1428,
        0.0525,
        0.1512,
        0.1995,
        0.1659,
        0.0546,
        0.2058,
        0.1449,
        0.1827,
        0.1134,
        0.0651,
        0.1701,
        0.2205,
        0.2205,
        0.168,
        0.126,
        0.0882,
        0.1554,
        0.2079,
        0.1722,
        0.0966,
        0.21,
        0.2016,
        0.2058,
        0.1533,
        0.1533,
        0.0462,
        0.1617,
        0.0819,
        0.189,
        0.1974,
        0.2058,
        0.1554,
        0.1785,
        0.1701,
        0.1764,
        0.168,
        0.1176,
        0.1155,
        0.1218,
        0.168,
        0.168,
        0.1176,
        0.1722,
        0.1722,
        0.0861,
        0.0294,
        0.0273,
        0.063,
        0.1407,
        0.1575,
        0.0315,
        0.1869,
        0.1449,
        0.1533,
        0.1029,
        0.1029,
        0.1239,
        0.1596,
        0.0672,
        0.1449,
        0.1302,
        0.1428,
        0.1281,
        0.1407,
        0.0966,
        0.0294,
        0.1008,
        0.1344,
        0.1176,
        0.1134,
        0.1239,
        0.0861,
        0.1239,
        0.1029,
        0.1281,
        0.1365,
        0.1407,
        0.0966,
        0.1155,
        0.1302,
        0.126,
        0.0819,
        0.1113,
        0.1218,
        0.0315,
        0.0399,
        0.0609,
        0.126,
        0.1176,
        0.1113,
        0.0672,
        0.105,
        0.0126,
        0.0903,
        0.0567,
        0.0105,
        0.0126,
        0.0336,
        0.0777,
        0.0714,
        0.0567,
        0.0798,
        0.0777,
        0.0819,
        0.0273,
        0.0798,
        0.0798,
        0.0966,
        0.0735,
        0.0651,
        0.0252,
        0.0546,
        0.0588,
        0.0252,
        0.0672,
        0.0378,
        0.0084,
        0.0588,
        0.0672,
        0.0294,
        0.0231,
        0.0084,
        0.0105,
        0.0273,
        0.0252,
        0.0588,
        0.0441,
        0.0609,
        0.0546,
        0.0546,
        0.0273,
        0.0189,
        0.0231,
        0.0357,
        0.0147,
        0.0399,
        0.0462,
        0.0042,
        0.0231,
        0.0168,
        0.0105,
        0.0042,
        0.0378,
        0.0084,
        0.0105,
        0.0126,
        0.0189,
        0.0336,
        0.021,
        0.0126,
        0.021,
        0.0273,
        0.0399,
        0.0126,
        0.0357,
        0.0042,
        0.0042,
        0.0231,
        0.0084,
        0.0336,
        0.0273,
        0.0336,
        0.0105,
        0.0168,
        0.0252,
        0.0273,
        0.0042,
        0.0147,
        0.0315,
        0.0021,
        0.0315,
        0.0042,
        0.0231,
        0.0252,
        0.0021,
        0.0042,
        0.0168,
        0.0357,
        0.0378,
        0.0168,
        0.0252,
        0.0357,
        0.0378,
        0.0168};
}
