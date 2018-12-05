package main;

//JAVA References
import java.util.ArrayList;
import java.util.List;

//Selenium and TestNG References
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class SUIT_BasicFlow {
    public String baseURL = "https://www.autohero.com/de/search/";
    private static WebDriver driver = null;
    public String filterYear; //Registration year choice will be dynamic in this automation task, so we set it as global
    //These lists will be populated to use on final verification
    public ArrayList<Double> defaultPriceList = new ArrayList<>();
    public ArrayList<Integer> defaultYearList = new ArrayList<>();
    public ArrayList<Double> newPriceList = new ArrayList<>();
    public ArrayList<Integer> newYearList = new ArrayList<>();

    @BeforeSuite
    public void BeforeSuit() {
	System.setProperty(DATA.GetDriverProperty("Chrome"), DATA.GetDriverPath("Chrome")); //Script is also a little parametric here. "Chrome" and "Firefox" can be used to easily switch between browsers
	
	driver = new ChromeDriver();
	driver.manage().window().maximize(); //For a better view of the show
    }

    @Test(priority = 0)
    public void SearchLanding() {
	String expectedTitle = "Premium Gebrauchtwagen Angebote | AutoHero.com";
	String expectedURL = baseURL;
	
	driver.get(baseURL);
	
	//To verify that we are on the target page and if its loaded as expected, we are checking the URL and Page Title
	Assert.assertEquals(driver.getCurrentUrl(), expectedURL);
	Assert.assertEquals(driver.getTitle(), expectedTitle);	
	System.out.println("Case 1: User successfully accessed the target page.");
    }

    @Test(priority = 1)
    public void SearchFilter() throws InterruptedException {
	WebDriverWait wait = new WebDriverWait(driver, 10);
	
	//Luckily, AutoHero page specifically designed as QA friendly. Custom attributes make our job easier to find the required elements
	//To prevent, element not found error, we are waiting until such elements are visible on the page
	wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-qa-selector='filter-year']")));
	driver.findElement(By.cssSelector("div[data-qa-selector='filter-year']")).click();
	
	//I wanted make minYear a dynamic random value. If you send it as "Random", a random year between 1960 and 2018 will be selected.
	wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select[data-qa-selector='select']")));
	filterYear = DATA.GetRegistrationYear("Random").toString(); //This "Random" also can be sent as "Normal" which would choose 2015 as min. year
	String csFilterYear = "option[data-qa-selector-value=\"" + filterYear + "\"]";
	driver.findElement(By.cssSelector(csFilterYear)).click();	
	
	//Waiting for "Reset Filters" hyperlink to appear. I set it as a checkpoint for an applied filter. When there is no filtering, this link is hidden.
	wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[data-qa-selector='reset-active-filters-button']")));	
	System.out.println("Case 2: User successfully applied a filter to the list with selecting " + filterYear + " as the min. registration year.");
    }

    @Test(priority = 2)
    public void SearchListOrder() throws InterruptedException {
	WebDriverWait wait = new WebDriverWait(driver, 10);
	
	//We are gathering the web elements that are specifically includes price data
	List<WebElement> priceList = driver.findElements(By.cssSelector("div[data-qa-selector='price']"));	
	for (WebElement we : priceList) {
	    defaultPriceList.add(Double.parseDouble(we.getText().replaceAll(" €", ""))); //Removing space and € symbol to have numbers only.
	}
	
	//This one is a bit tricky and dirty. I forced it to check list for final verification.
	List<WebElement> yearList = driver.findElements(By.cssSelector("li[data-qa-selector='spec']"));
	for (WebElement we : yearList) {
	    String eText = we.getText();	    
	    //Specs are combined so there is no registration date variable as a single entity, also its not possible to use it as a sorting option.
	    //I worked on a format to isolate the year from other unwanted characters. Dirty but looks like working.
	    if (eText.length() == 9 && Character.toString(eText.charAt(4)).equals("/")) {
		defaultYearList.add(Integer.parseInt(eText.substring(5)));
	    }	    
	}
	
	//System.out.println("Default Price List: " + defaultPriceList);
	//System.out.println("Default Year List: " + defaultYearList);
	
	//Sorting the list by descending price amount
	driver.findElement(By.cssSelector("option[data-qa-selector-value='offerPrice.amountMinorUnits.desc'")).click();	

	Thread.sleep(1000); //Giving it a time to breathe
	wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[data-qa-selector='reset-active-filters-button']")));	
	
	//Populating the new prices by updated sort
	List<WebElement> priceList2 = driver.findElements(By.cssSelector("div[data-qa-selector='price']"));	
	for (WebElement we : priceList2) {
	    newPriceList.add(Double.parseDouble(we.getText().replaceAll(" €", "")));
	}
	
	//Populating the new years by updated sort
	List<WebElement> yearList2 = driver.findElements(By.cssSelector("li[data-qa-selector='spec']"));
	for (WebElement we : yearList2) {
	    String eText = we.getText();	    
	    //System.out.println(eText + " | " + eText.length() + " | " + eText.charAt(4));
	    if (eText.length() == 9 && Character.toString(eText.charAt(4)).equals("/")) {
		newYearList.add(Integer.parseInt(eText.substring(5)));
	    }	    
	}
	
	//NOTE: Normally, we would need to jump on pages to get whole data for a better sorting and verification. For this task, I just get the data of the first page of whole pagination, so the only first 24 entries will be taken. In a real life scenario, we would need to improve that technique
	
	//System.out.println("Sorted Price List: " + newPriceList);
	//System.out.println("Updated Year List: " + newYearList);

	Assert.assertTrue(newPriceList.size() >= 1); //Just making sure that we have at least one results after filter and sorting
	System.out.println("Case 3: List is updated successfully and has at least one result to continue to test.");
    }
    
    @Test(priority = 3)
    public void ResultVerifications() throws InterruptedException {
	//VERIFICATION 1 - QUERY STRINGS
	String currentURL = driver.getCurrentUrl();
	//I wrote a small tool to return sort and minYear values. It should also work when there are more than two filters applied since the sorting controls the "&" character and minYear always appears at the end of the query.
	Assert.assertEquals(TOOL_QueryStringParser.GetSortingChoice(currentURL), "PRICE_DESC"); //We check that if the price is descending
	Assert.assertEquals(TOOL_QueryStringParser.GetMinYearChoice(currentURL), filterYear); //We check that if the minYear is same with the we chosen
	System.out.println("Verification 1: Sorting and registration year is as expected on query string of updated URI.");
	
	//VERIFICATION 2 - Active Filter Check
	//This a task specific approach. It needs to be improved for multiple filtering cases since it'd only work for only minYear filter. If other filters added, xpath can't find the button below. I just added it for our specific scenario.
	String activeMinYearFilter = driver.findElement(By.xpath("//*[@id=\"app\"]/div/main/div[4]/div/div[2]/div/div[1]/ul/li[1]/button")).getText().substring(19);
	Assert.assertEquals(activeMinYearFilter, filterYear); //We check that if the year on the active filter pin is the same with what we chose
	System.out.println("Verification 2: Active registration year filter value matches with our selection.");
	
	//VERIFICATION 3 - List Inspection for Prices
	Assert.assertNotEquals(defaultPriceList, newPriceList); //That assures us that the pricing order has changed and not the same as we first checked
	Integer sortedPriceListCount = newPriceList.size();
	boolean fTPOCOK = false;
	if (sortedPriceListCount >= 3) {
	    if (newPriceList.get(0) >= newPriceList.get(1) && newPriceList.get(1) >= newPriceList.get(2) && newPriceList.get(2) >= newPriceList.get(3)) {
		//System.out.println(newPriceList.get(0) + " € >= " + newPriceList.get(1) + " € >= " + newPriceList.get(2) + " € ");
		fTPOCOK = true;		
	    }
	    Assert.assertTrue(fTPOCOK);
	    System.out.println("Verification 3: Descending price amount test is successful for the first 3 items on the list.");
	}
	else {
	    System.out.println("Verification 3: Since there are less than 3 items in the list, it's not optimal to use this method for price sorting test.");
	}
	
	//VERIFICATION 4 - List Inspection for Registration Years
	boolean noUnwantedYears = true;
	//That is also a task specific approach. We just check our two different year lists to see if we can catch an item with a year that less than what we specified
	//Normally, it's better to iterate whole list with jumping pages to search for an unwanted year. I just put it to give the idea.
	for (int i = 0; i < defaultYearList.size(); i++) {
	    if (defaultYearList.get(i) < Integer.parseInt(filterYear)) {
		noUnwantedYears = false;
	    }
	}
	
	for (int i = 0; i < newYearList.size(); i++) {
	    if (newYearList.get(i) < Integer.parseInt(filterYear)) {
		noUnwantedYears = false;
	    }
	}
	Assert.assertTrue(noUnwantedYears);
	System.out.println("Verification 4: We just checked " + (defaultYearList.size() + newYearList.size()) + " items and couldn't find any years below " + filterYear + ".");
	System.out.println("Test Result: Passed");
    }

    @AfterSuite
    public void afterSuit() {
	driver.quit();
    }
}