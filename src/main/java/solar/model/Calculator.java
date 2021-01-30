package solar.model;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

/**
 * Solar irradiance sums.
 *
 * @threadsafety Immutable
 */
public class Calculator {

    public static final double tropic = 23.45; // degrees
    public static final double springEquinox = 81.0; // julian days
    public static final double daysPerYear = 365.0;
    private static final String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};


//    public static final double sunnyDays(int month) {
//        return sunnyDays[month];
//    }

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
     * Calendar data. TODO: Remove. Use Date?
     */
    private static final int daysPerMonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    public static int daysPerMonth(int month) {
        return daysPerMonth[month];
    }

    private static PolynomialSplineFunction attenuation;
    private static PolynomialSplineFunction diffusion;
    private static PolynomialSplineFunction weather;

    public Calculator() {
        LinearInterpolator li = new LinearInterpolator();
        attenuation = li.interpolate(months, attmosphericAttenuations);
        diffusion = li.interpolate(months, skyDiffusionFactors);
        weather = li.interpolate(months, sunnyDayFactors);
    }

    public double getAttenuation(int yearDay) {
        return attenuation.value(yearDay / 30.5);
    }

    public double getDiffusion(int yearDay) {
        return diffusion.value(yearDay / 30.5);
    }

    
    public double getWeatherFactor(int yearDay) {
        return weather.value(yearDay / 30.5);
    }
    /**
     *
     * KA: Returns the solar declination angle in degrees. This is the offset to
     * be applied to the latitude due to the tilt of the earth.
     *
     * https://www.pveducation.org/pvcdrom/properties-of-sunlight/declination-angle
     *
     * @param day
     * @return The solar declination angle in degrees
     */
    public double solarDeclination(int day) {
        return 23.45 * Math.sin(Math.toRadians((360.0 * (284 + day)) / 365.0));
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
     * @param day Day number 0-364
     * 
     * @return
     */
    public double timeEquation(int day) {
        double b = (360.0 * (day - 81)) / 364.0;
        return 9.87 * Math.sin(Math.toRadians(2 * b)) - 7.53 * Math.cos(Math.toRadians(b)) - 1.5 * Math.sin(Math.toRadians(b));

    }

    //returns hour angle
    public double hourAngle(double solarTimeInMinutes) {
        return (solarTimeInMinutes - 720) / 4;
    }

    /**
     * Elevation angle of the sun. For maximum elevation angle at solar noon,
     * set hAngle = 0. Was alpha.
     *
     * https://www.pveducation.org/pvcdrom/properties-of-sunlight/elevation-angle
     *
     * @param latitude
     * @param sDec
     * @param hAngle
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
     * @param day_number Day number, 0-364
     * @return the extraterrestrial solar radiation in W/m2
     */
    public double solarIrradiance(int day_number) {
        return (1353 * (1 + 0.033 * Math.cos(Math.toRadians(360.0 * (day_number / 365.25)))));
    }

    /**
     *
     * @param solarTime
     * @param et
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
     * @param panelAzimuth
     * @param panelTilt
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
     * @return Day number, 1-365
     */
    public static int dayNumber(int month, int date) {
        int out = 0;
        for (int i = 0; i < month; i++) {
            out += daysPerMonth[i];
        }
        out += date;
        return out;
    }

    /**
     * Print time of sunset
     *
     * @param month Month number, 0-11
     * @param date Date number, 0-31
     * @param latitude
     * @param standardMeridian
     * @param longitude
     * @return Formatted string like "7:32 PM"
     */
    public String sunrise(int month, int date, double latitude, double standardMeridian, double longitude) {
        int n = dayNumber(month, date);
        double sDec = solarDeclination(n);
        double et = timeEquation(n);
        double hsr = hssNhsr(latitude, sDec);// hsr is in MINUTES
        double solarSunrise = 720 - hsr;
        double sunrise = solarSunrise - et - 4 * (standardMeridian - longitude);
        int outHours = (int) (sunrise / 60);
        int outMinutes = (int) (((sunrise / 60) - outHours) * 60);
        return outHours + ":" + outMinutes + " AM";
    }

    /**
     * Time of sunset
     *
     * @param month Month number, 0-11
     * @param date Date number, 0-31
     * @param latitude
     * @param standardMeridian
     * @param longitude
     * @return Formatted string like "6:32 AM"
     */
    public String sunset(int month, int date, double latitude, double standardMeridian, double longitude) {
        int n = dayNumber(month, date);
        double sDec = solarDeclination(n);
        double et = timeEquation(n);
        double hss = hssNhsr(latitude, sDec);// hss is in MINUTES
        double solarSunset = hss;
        double sunset = solarSunset - et - 4 * (standardMeridian - longitude);
        int outHours = (int) (sunset / 60);
        int outMinutes = (int) (((sunset / 60) - outHours) * 60);
        return outHours + ":" + outMinutes + " PM";
    }

    /**
     * returns the instantaneous solar radiation
     *
     * @param localSolarTime Local solar time in hours
     * @param longitude Longitude, degrees
     * @param latitude Latitude, degrees
     * @param standardMeridian Closest meridian
     * @param panelTilt Panel tilt (relative to horizontal?)
     * @param panelAzimuth Panel direction (relative to south?)
     * @param day day number 0-364
     * @param cn Scaling for irradiance. Clouds?
     * @param rho Scaling for reflection. Surface color?
     *
     * @return The instantaneous solar radiation
     */
    public double insSolarRadiation(double localSolarTime, double latitude, double longitude, double standardMeridian, double panelTilt, double panelAzimuth, int day, double cn, double rho) {
        // close enough
        double earthTilt = solarDeclination(day);
        // Offset due to wobble
        double eqTime = timeEquation(day);
        // Absolute solar time
        double solarTime = solarTime(localSolarTime, eqTime, standardMeridian, longitude);
        // solar angle relative to solar noon
        double hourAngle = hourAngle(solarTime);
        double sunElevation = sunElevation(latitude, earthTilt, hourAngle);
        double sunAzimuth = sunAzimuth(earthTilt, hourAngle, sunElevation);
        double extraterrestialRadiance = solarIrradiance(day);

        double attenuationFactor = getAttenuation(day);

        double directIrradiance = beamNormalIrradiance(cn, extraterrestialRadiance, attenuationFactor, sunElevation);

        // cos law for the actual incidence angle (in 3D)
        double cosI = cosI(sunElevation, sunAzimuth, panelAzimuth, panelTilt);

        double incidentIrradiance = directIrradiance * cosI;

        double skyDiffusionFactor = getDiffusion(day);

        double skyDiffuse = diffuseIrradiance(skyDiffusionFactor, directIrradiance, panelTilt);

        double groundReflected = groundReflected(rho, directIrradiance, sunElevation, skyDiffusionFactor, panelTilt);

        return (incidentIrradiance + skyDiffuse + groundReflected);
    }

    public static String monthName(int month) {
        return monthNames[month];
    }
}
