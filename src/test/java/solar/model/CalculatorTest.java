package solar.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 *
 * @author rocky
 */
public class CalculatorTest {

    private Calculator instance = new Calculator();

    public CalculatorTest() {
    }

    @ParameterizedTest
    @CsvSource(value = {
        "0,-23.01",
        "41,-14.59",
        "80,0.0",
        "86,1.61",
        "226,+14.11",
        "287,-9.23",
        "327,-20.82",
        "364,-23.01"})
    public void testSolarDeclination(int day, double dec) {
        assertEquals(dec, instance.solarDeclination(day), 0.6);
    }

    @Test
    public void testSolarTime() {
    }

    /**
     * read these values from graph so don't expect high accuracy
     */
    @ParameterizedTest
    @CsvSource(value = {
        "0,-3.2",
        "40,-14.6",
        "130,+3.5",
        "208,-6.5",
        "305,+16.5",
        "358,0.0",
        "364,-3.0"})
    public void testEquationOfTime(int day, double eot) {
        assertEquals(eot, instance.timeEquation(day), 0.5);
    }

    @Test
    public void testHourAngle() {
    }

    @Test
    public void testSunElevation() {
    }

    @Test
    public void testSolarZenith() {
    }

    @Test
    public void testSunAzimuth() {
    }

    @Test
    public void testHssNhsr() {
    }

    @Test
    public void testSolarIrradiance() {
    }

    @Test
    public void testSunriseOrSunset() {
    }

    @Test
    public void testBeamNormalIrradiance() {
    }

    @Test
    public void testCosI() {
    }

    @Test
    public void testDiffuseIrradiance() {
    }

    @Test
    public void testGroundReflected() {
    }

    @Test
    public void testDayNumber() {
        assertEquals(0, Calculator.dayNumber(0, 1));
        assertEquals(3, Calculator.dayNumber(0, 4));
        assertEquals(34, Calculator.dayNumber(1, 4));
        assertEquals(30 + 28 + 4, Calculator.dayNumber(2, 4));
        assertEquals(364, Calculator.dayNumber(11, 31));
    }

    @Test
    public void testWeatherSmoothed() {
        assertEquals(0.27, instance.getWeatherFactorSmoothed(Calculator.dayNumber(1, 14)),0.05); // on a knot
        assertEquals(0.49, instance.getWeatherFactorSmoothed(Calculator.dayNumber(5, 14)),0.05); // on a knot
    }

    @Test
    public void testWeatherRecorded() {
        assertEquals(0.1302, instance.getWeatherFactorRecorded(0), 0.01); // on a knot
        assertEquals(0.0746, instance.getWeatherFactorRecorded(364), 0.01); // on a knot
    }

    @Test
    public void testSunrise() {
    }

    @Test
    public void testSunset() {
    }

    @Test
    public void testInsSolarRadiation() {
    }

    @Test
    public void testTotalRad() {
    }

    /**
     * PVCDROM Graph Data Sunlight Hours Select, Copy and Paste into Excel etc.
     * http://www.pveducation.org/pvcdrom Latitude (degrees, South is negative)
     * -41 Day	Sunrise	Sunset	Hours of Sunlight
     */
    @ParameterizedTest
    @CsvSource(value = {
        "0,4.55011450145298,19.449885498547,14.899770997094",
        "5,4.58234585551132,19.4176541444887,14.8353082889774",
        "10,4.62637080607444,19.3736291939256,14.7472583878511",
        "15,4.68142329998507,19.3185767000149,14.6371534000299",
        "20,4.74660500583364,19.2533949941664,14.5067899883327",
        "25,4.82093255100714,19.1790674489929,14.3581348979857",
        "30,4.90338277198789,19.0966172280121,14.1932344560242",
        "35,4.99293245631344,19.0070675436866,14.0141350873731",
        "40,5.08859035636717,18.9114096436328,13.8228192872657",
        "45,5.18942051549439,18.8105794845056,13.6211589690112",
        "50,5.29455697089358,18.7054430291064,13.4108860582128",
        "55,5.40321060262546,18.5967893973745,13.1935787947491",
        "60,5.51466928985629,18.4853307101437,12.9706614202874",
        "65,5.62829267254463,18.3717073274554,12.7434146549107",
        "70,5.74350277744091,18.2564972225591,12.5129944451182",
        "75,5.85977162741237,18.1402283725876,12.2804567451753",
        "80,5.97660677506859,18.0233932249314,12.0467864498628",
        "85,6.09353553102543,17.9064644689746,11.8129289379491",
        "90,6.2100885241082,17.7899114758918,11.5798229517836",
        "95,6.32578315330444,17.6742168466956,11.3484336933911",
        "100,6.44010747804206,17.5598925219579,11.1197850439159",
        "105,6.55250514576448,17.4474948542355,10.894989708471",
        "110,6.66236206817014,17.3376379318299,10.6752758636597",
        "115,6.76899571594364,17.2310042840564,10.4620085681127",
        "120,6.87164808143078,17.1283519185692,10.2567038371384",
        "125,6.96948352038774,17.0305164796123,10.0610329592245",
        "130,7.06159277197896,16.938407228021,9.87681445604208",
        "135,7.14700439930791,16.8529956006921,9.70599120138417",
        "140,7.22470461161833,16.7752953883817,9.55059077676333",
        "145,7.29366585413713,16.7063341458629,9.41266829172575",
        "150,7.35288365100013,16.6471163489999,9.29423269799975",
        "155,7.40142000405087,16.5985799959491,9.19715999189825",
        "160,7.43845033444006,16.5615496655599,9.12309933111989",
        "165,7.4633097650037,16.5366902349963,9.07338046999259",
        "170,7.47553380967961,16.5244661903204,9.04893238064078",
        "175,7.47488856819076,16.5251114318092,9.05022286361847",
        "180,7.4613864788134,16.5386135211866,9.07722704237321",
        "185,7.43528547146978,16.5647145285302,9.12942905706043",
        "190,7.39707163882054,16.6029283611795,9.20585672235892",
        "195,7.34742779145841,16.6525722085416,9.30514441708318",
        "200,7.2871919810371,16.7128080189629,9.42561603792581",
        "205,7.2173109369037,16.7826890630963,9.56537812619261",
        "210,7.13879330685463,16.8612066931454,9.72241338629075",
        "215,7.05266680006885,16.9473331999312,9.89466639986231",
        "220,6.95994211435015,17.0400578856499,10.0801157712997",
        "225,6.8615852172568,17.1384147827432,10.2768295654864",
        "230,6.75849839146539,17.2415016085346,10.4830032170692",
        "235,6.65150958629537,17.3484904137046,10.6969808274093",
        "240,6.54136907387283,17.4586309261272,10.9172618522543",
        "245,6.42875215317903,17.571247846821,11.1424956936419",
        "250,6.3142666063695,17.6857333936305,11.371466787261",
        "255,6.19846371014012,17.8015362898599,11.6030725797198",
        "260,6.08185177061351,17.9181482293865,11.836296458773",
        "265,5.96491132922849,18.0350886707715,12.070177341543",
        "270,5.84811134188312,18.1518886581169,12.3037773162338",
        "275,5.73192574045054,18.2680742595495,12.5361485190989",
        "280,5.61684983175227,18.3831501682477,12.7663003364954",
        "285,5.50341596923903,18.496584030761,12.9931680615219",
        "290,5.39220784905872,18.6077921509413,13.2155843018826",
        "295,5.2838726444113,18.7161273555887,13.4322547111774",
        "300,5.17913001912146,18.8208699808785,13.6417399617571",
        "305,5.07877688481528,18.9212231151847,13.8424462303694",
        "310,4.98368663320582,19.0163133667942,14.0326267335884",
        "315,4.8948015497217,19.1051984502783,14.2103969005566",
        "320,4.81311727386627,19.1868827261337,14.3737654522675",
        "325,4.73965859273262,19.2603414072674,14.5206828145348",
        "330,4.67544659182026,19.3245534081797,14.6491068163595",
        "335,4.62145824035575,19.3785417596443,14.7570835192885",
        "340,4.57858076347062,19.4214192365294,14.8428384730588",
        "345,4.54756444450994,19.4524355554901,14.9048711109801",
        "350,4.52897850363011,19.4710214963699,14.9420429927398",
        "355,4.52317508073672,19.4768249192633,14.9536498385266",
        "360,4.53026586356719,19.4697341364328,14.9394682728656",
        "365,4.55011450145298,19.449885498547,14.899770997094"})

    public void testSunriseAndSunsetAndDaylength(int day, double sunrise, double sunset, double daylength) {
        double lat = -41.0;
        double meridian = 0.0;
        double lon = 0.0;
        double sr = instance.sunrise(day, lat, meridian, lon);
        double ss = instance.sunset(day, lat, meridian, lon);
        assertEquals(sunrise, sr, 0.3); // BUG
        assertEquals(sunset, ss, 0.3); // BUG
        assertEquals(daylength, ss - sr, 0.05);
    }

//
//PVCDROM Graph Data
//Select, Copy and Paste into Excel etc.
//http://www.pveducation.org/pvcdrom
//Latitude (degrees, South is negative)	36
//Day: 	224	Aug 12
//Hour (24h)	 direct radiation (kW/m2)
//0	0
//0.125	0
//0.25	0
//0.375	0
//0.5	0
//0.625	0
//0.75	0
//0.875	0
//1	0
//1.125	0
//1.25	0
//1.375	0
//1.5	0
//1.625	0
//1.75	0
//1.875	0
//2	0
//2.125	0
//2.25	0
//2.375	0
//2.5	0
//2.625	0
//2.75	0
//2.875	0
//3	0
//3.125	0
//3.25	0
//3.375	0
//3.5	0
//3.625	0
//3.75	0
//3.875	0
//4	0
//4.125	0
//4.25	0
//4.375	0
//4.5	0
//4.625	0
//4.75	0
//4.875	0
//5	0
//5.125	0
//5.25	0
//5.375	0.000025784198379098948
//5.5	0.008390867478189425
//5.625	0.04929717699504934
//5.75	0.11624070794047073
//5.875	0.1927535326766277
//6	0.26898113440266397
//6.125	0.3405446552636061
//6.25	0.4059245199589444
//6.375	0.46491962776968054
//6.5	0.5178872585598511
//6.625	0.5653871964752519
//6.75	0.6080203359250258
//6.875	0.6463591566750707
//7	0.6809212455245356
//7.125	0.7121626582107181
//7.25	0.7404800464425639
//7.375	0.7662162687369708
//7.5	0.7896670005042187
//7.625	0.8110872187402955
//7.75	0.8306970977000777
//7.875	0.8486871689883156
//8	0.8652227461834834
//8.125	0.8804476764965039
//8.25	0.894487503213582
//8.375	0.9074521245173326
//8.5	0.9194380276461062
//8.625	0.9305301676516216
//8.75	0.9408035498469423
//8.875	0.9503245655627226
//9	0.9591521224908782
//9.125	0.9673386037814855
//9.25	0.9749306841004783
//9.375	0.981970025918684
//9.5	0.988493875237441
//9.625	0.9945355726196402
//9.75	1.000124992660529
//9.875	1.0052889227913806
//10	1.0100513904703823
//10.125	1.014433946303963
//10.25	1.0184559093971417
//10.375	1.0221345802035713
//10.5	1.0254854252944785
//10.625	1.0285222377579826
//10.75	1.0312572763498347
//10.875	1.0337013860220028
//11	1.0358641020392503
//11.125	1.0377537395416843
//11.25	1.0393774701115543
//11.375	1.0407413866457282
//11.5	1.0418505576134067
//11.625	1.0427090715850786
//11.75	1.0433200727479899
//11.875	1.0436857879707129
//12	1.043807545840734
//12.125	1.0436857879707129
//12.25	1.0433200727479899
//12.375	1.0427090715850786
//12.5	1.0418505576134067
//12.625	1.0407413866457282
//12.75	1.0393774701115543
//12.875	1.0377537395416843
//13	1.0358641020392503
//13.125	1.0337013860220028
//13.25	1.0312572763498347
//13.375	1.0285222377579826
//13.5	1.0254854252944785
//13.625	1.0221345802035713
//13.75	1.0184559093971417
//13.875	1.014433946303963
//14	1.0100513904703823
//14.125	1.0052889227913806
//14.25	1.000124992660529
//14.375	0.9945355726196402
//14.5	0.988493875237441
//14.625	0.981970025918684
//14.75	0.9749306841004783
//14.875	0.9673386037814855
//15	0.9591521224908782
//15.125	0.9503245655627226
//15.25	0.9408035498469423
//15.375	0.9305301676516216
//15.5	0.9194380276461062
//15.625	0.9074521245173326
//15.75	0.894487503213582
//15.875	0.8804476764965039
//16	0.8652227461834834
//16.125	0.8486871689883156
//16.25	0.8306970977000777
//16.375	0.8110872187402955
//16.5	0.7896670005042187
//16.625	0.7662162687369708
//16.75	0.7404800464425639
//16.875	0.7121626582107181
//17	0.6809212455245356
//17.125	0.6463591566750707
//17.25	0.6080203359250258
//17.375	0.5653871964752519
//17.5	0.5178872585598511
//17.625	0.46491962776968054
//17.75	0.4059245199589444
//17.875	0.3405446552636061
//18	0.26898113440266397
//18.125	0.1927535326766277
//18.25	0.11624070794047073
//18.375	0.04929717699504934
//18.5	0.008390867478189425
//18.625	0.000025784198379098948
//18.75	0
//18.875	0
//19	0
//19.125	0
//19.25	0
//19.375	0
//19.5	0
//19.625	0
//19.75	0
//19.875	0
//20	0
//20.125	0
//20.25	0
//20.375	0
//20.5	0
//20.625	0
//20.75	0
//20.875	0
//21	0
//21.125	0
//21.25	0
//21.375	0
//21.5	0
//21.625	0
//21.75	0
//21.875	0
//22	0
//22.125	0
//22.25	0
//22.375	0
//22.5	0
//22.625	0
//22.75	0
//22.875	0
//23	0
//23.125	0
//23.25	0
//23.375	0
//23.5	0
//23.625	0
//23.75	0
//23.875	0
//24	0
//24.125	0
//24.25	0
//24.375	0
//24.5	0
//24.625	0
//24.75	0
//24.875	0
//
//PVCDROM Graph Data
//Select, Copy and Paste into Excel etc.
//http://www.pveducation.org/pvcdrom
//Latitude (degrees, South is negative)	65
//Day: 	309	Nov 5
//Hour (24h)	 direct radiation (kW/m2)
//0	0
//0.125	0
//0.25	0
//0.375	0
//0.5	0
//0.625	0
//0.75	0
//0.875	0
//1	0
//1.125	0
//1.25	0
//1.375	0
//1.5	0
//1.625	0
//1.75	0
//1.875	0
//2	0
//2.125	0
//2.25	0
//2.375	0
//2.5	0
//2.625	0
//2.75	0
//2.875	0
//3	0
//3.125	0
//3.25	0
//3.375	0
//3.5	0
//3.625	0
//3.75	0
//3.875	0
//4	0
//4.125	0
//4.25	0
//4.375	0
//4.5	0
//4.625	0
//4.75	0
//4.875	0
//5	0
//5.125	0
//5.25	0
//5.375	0
//5.5	0
//5.625	0
//5.75	0
//5.875	0
//6	0
//6.125	0
//6.25	0
//6.375	0
//6.5	0
//6.625	0
//6.75	0
//6.875	0
//7	0
//7.125	0
//7.25	0
//7.375	0
//7.5	0
//7.625	0
//7.75	0
//7.875	0
//8	0
//8.125	0
//8.25	0
//8.375	0
//8.5	0
//8.625	0
//8.75	4.021655321495207e-12
//8.875	0.000003840944625489706
//9	0.0002761117440814081
//9.125	0.002208034835635094
//9.25	0.007528566538158266
//9.375	0.01687628130714525
//9.5	0.02983853212663701
//9.625	0.04552595808161388
//9.75	0.06298201929572583
//9.875	0.08136837030531921
//10	0.10001772171867693
//10.125	0.11842738002170256
//10.25	0.13623235433636757
//10.375	0.15317532195946734
//10.5	0.16907995576524432
//10.625	0.1838293310940435
//10.75	0.19734923527858328
//10.875	0.20959559137710654
//11	0.22054512162023523
//11.125	0.2301884787843875
//11.25	0.23852522386653577
//11.375	0.24556017191275095
//11.5	0.25130074778756206
//11.625	0.2557550881608168
//11.75	0.2589306984099857
//11.875	0.26083352814744126
//12	0.2614673711264527
//12.125	0.26083352814744126
//12.25	0.2589306984099857
//12.375	0.2557550881608168
//12.5	0.25130074778756206
//12.625	0.24556017191275095
//12.75	0.23852522386653577
//12.875	0.2301884787843875
//13	0.22054512162023523
//13.125	0.20959559137710654
//13.25	0.19734923527858328
//13.375	0.1838293310940435
//13.5	0.16907995576524432
//13.625	0.15317532195946734
//13.75	0.13623235433636757
//13.875	0.11842738002170256
//14	0.10001772171867693
//14.125	0.08136837030531921
//14.25	0.06298201929572583
//14.375	0.04552595808161388
//14.5	0.02983853212663701
//14.625	0.01687628130714525
//14.75	0.007528566538158266
//14.875	0.002208034835635094
//15	0.0002761117440814081
//15.125	0.000003840944625489706
//15.25	4.021655321495207e-12
//15.375	0
//15.5	0
//15.625	0
//15.75	0
//15.875	0
//16	0
//16.125	0
//16.25	0
//16.375	0
//16.5	0
//16.625	0
//16.75	0
//16.875	0
//17	0
//17.125	0
//17.25	0
//17.375	0
//17.5	0
//17.625	0
//17.75	0
//17.875	0
//18	0
//18.125	0
//18.25	0
//18.375	0
//18.5	0
//18.625	0
//18.75	0
//18.875	0
//19	0
//19.125	0
//19.25	0
//19.375	0
//19.5	0
//19.625	0
//19.75	0
//19.875	0
//20	0
//20.125	0
//20.25	0
//20.375	0
//20.5	0
//20.625	0
//20.75	0
//20.875	0
//21	0
//21.125	0
//21.25	0
//21.375	0
//21.5	0
//21.625	0
//21.75	0
//21.875	0
//22	0
//22.125	0
//22.25	0
//22.375	0
//22.5	0
//22.625	0
//22.75	0
//22.875	0
//23	0
//23.125	0
//23.25	0
//23.375	0
//23.5	0
//23.625	0
//23.75	0
//23.875	0
//24	0
//24.125	0
//24.25	0
//24.375	0
//24.5	0
//24.625	0
//24.75	0
//24.875	0
//
//PVCDROM Graph Data Sunlight Hours
//Select, Copy and Paste into Excel etc.
//http://www.pveducation.org/pvcdrom
//Latitude (degrees, South is negative)	46
//Array Tilt (degrees)	7
//Day	Incident Power (kWh/m2/day)	Power on the Horizontal (kWh/m2/day)Module Power (kWh/m2/day)
//0	3.7879438287386638	1.3521736297640605	1.7733149848260312
//5	3.896808444653308	1.4189035108036214	1.8506278016858861
//10	4.046893210407136	1.5133975646473061	1.9595244527493407
//15	4.232055270149635	1.6352555681560494	2.098766724774705
//20	4.4424022084341575	1.7826419458427112	2.2652462135256055
//25	4.6668888375436435	1.9528305382282067	2.4548380913620482
//30	4.917851330543702	2.152543402761094	2.675373689440925
//35	5.2238175948687156	2.3968455985109114	2.9446348462897918
//40	5.551180976317857	2.673421928330978	3.2463915126842138
//45	5.867337284421831	2.96732472330526	3.5620706704058844
//50	6.173472124040029	3.2779877584237376	3.891089867263628
//55	6.537379986755664	3.641588986969343	4.276097658945493
//60	6.907423465345586	4.0313825223816595	4.684893715495795
//65	7.2393157478763825	4.419293658140791	5.085138764145279
//70	7.567540237766744	4.822315306811698	5.497119994949941
//75	7.947386245619799	5.274638244139778	5.959795310939474
//80	8.29802669548554	5.722098223179411	6.411827981630288
//85	8.597427434818407	6.144035448161279	6.8311446671441844
//90	8.932444447942181	6.598160157155309	7.282757890877134
//95	9.279340177968908	7.066245127102799	7.746558826531077
//100	9.576351141159865	7.498043231247238	8.168129631463357
//105	9.836635186095084	7.898546853805484	8.554167095949536
//110	10.139735670197322	8.32889221723915	8.971587757910992
//115	10.425941226169055	8.739428680616074	9.367150090031219
//120	10.66267454555682	9.099873291227262	9.709323533846408
//125	10.866645908557738	9.421336417054379	10.01103581886496
//130	11.09398673203421	9.751109009237474	10.32321031657135
//135	11.308779381858475	10.057484243327776	10.612659591699122
//140	11.486913991156925	10.318052493587384	10.856396251865624
//145	11.626980631046713	10.53049060393971	11.052696927169665
//150	11.73675542234105	10.701199829956389	11.208887294261258
//155	11.83328779014544	10.845543134696634	11.341504618205027
//160	11.914172399348756	10.961402183217446	11.448648158104975
//165	11.970727251012304	11.040838646129988	11.52228732121714
//170	11.998865820750499	11.080082294148573	11.558681946466153
//175	11.997377690624269	11.07801018892483	11.556760301913263
//180	11.966313759070694	11.034669633440632	11.51656641896285
//185	11.907072189762452	10.951357237712536	11.439343055887337
//190	11.824166219157185	10.832233817477848	11.329230026695377
//195	11.726558621019203	10.685485095434835	11.194512530836818
//200	11.614564419540567	10.511324047063109	11.035089865525197
//205	11.47086936610626	10.294197059051955	10.834205844964954
//210	11.288810839049738	10.028763640765668	10.585632064819368
//215	11.071083668929793	9.718525748069004	10.29232791492346
//220	10.845256925346737	9.388871944471912	9.980457223473923
//225	10.641289532172094	9.066237934894476	9.677641061090123
//230	10.399420224782725	8.700460968167112	9.329835709190013
//235	10.109080133841296	8.28579813852703	8.929817875876228
//240	9.80872652466522	7.857198883718041	8.514199048501236
//245	9.549244128597683	7.456932648244446	8.128310589531441
//250	9.246520916660037	7.020688176368593	7.701683328130816
//255	8.896596545630235	6.550754449605712	7.2355456188956895
//260	8.568291867788465	6.102107028804727	6.789663811929226
//265	8.26535812870207	5.678453874776365	6.368067416786842
//270	7.909787311174643	5.228930148935702	5.913236727130393
//275	7.531088357576524	4.778994741261627	5.452716292780772
//280	7.208204176554578	4.3809459517821505	5.045885474202896
//285	6.871805850941445	3.9922170646330497	4.644099647680138
//290	6.499479259134709	3.603352491035135	4.235705209152963
//295	6.140615617749907	3.244785371063821	3.855941045258057
//300	5.836745002682359	2.9374461160498373	3.530224367243095
//305	5.518575454581541	2.6447902745776446	3.2153539352872946
//310	5.191544041201305	2.3706644071784297	2.9158676821623972
//315	4.890116978403846	2.130556740583303	2.6510944656047273
//320	4.64401151105036	1.9348335341191496	2.434914764143851
//325	4.420556092452517	1.7668298939362739	2.247488690781326
//330	4.2122228638914665	1.6218809332786788	2.083553538541394
//335	4.030169486596481	1.5026982499121695	1.9472327252166097
//340	3.8840057739289677	1.4109872860064447	1.8414722966379686
//345	3.779426279460574	1.3470025838159403	1.7673119775059016
//350	3.717960869984928	1.30985526740396	1.7241465517935248
//355	3.699023627330416	1.2984615958696644	1.7108940864849072
//360	3.7221794971115987	1.3123963622485406	1.727101449136125
    /**
     * Test solarIrradiance
     * @param dayNumber
     * @param irradiance
     */
    @ParameterizedTest
    @CsvSource(value = {
        "3 1397.6",
        "40 1388.4",
        "80 1363.1",
        "120 1333.2",
        "160 1312.3",
        "200 1309.9",
        "240 1327.2",
        "280 1356.3",
        "320 1383.8",
        "365 1397.6"}, delimiter = ' ')
    public void testsolarIrradiance(int dayNumber, double irradiance) {
        assertEquals(irradiance, instance.solarIrradiance(dayNumber), 0.05);
    }
    
    @Test
    public void testFormatHours()
    {
        assertEquals("7:32 AM", instance.formatHours(7.533));
        assertEquals("8:32 PM", instance.formatHours(20.533));
    }
}
