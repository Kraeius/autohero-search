package main;

import java.util.Random;

public class DATA {
    
    //Randon number generator
    static Random rnd = new Random();
    
    // Get webdriver property
    public static String GetDriverProperty(String Browser) {
	if (Browser == "Chrome") {
	    return "webdriver.chrome.driver";
	} else if (Browser == "Firefox") {
	    return "webdriver.gecko.driver";
	} else {
	    return "Unset";
	}
    }

    // Get webdriver path
    public static String GetDriverPath(String Browser) {
	if (Browser == "Chrome") {
	    return "chromedriver-2.44/chromedriver.exe";
	} else if (Browser == "Firefox") {
	    return "geckodriver-0.23.0/geckodriver.exe";
	} else {
	    return "Unset";
	}
    }
    
    // Get Registration Year
    public static Integer GetRegistrationYear(String Type) {
	if (Type == "Normal") {
	    return 2015;
	} else if (Type == "Random") {
	    int year = rnd.nextInt(59) + 1960;
	    return year;
	} else {
	    return 2015;
	}
    }
}