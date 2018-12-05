package main;

public class TOOL_QueryStringParser {    
    public static String GetSortingChoice(String URL) {
	Integer indexOfSort = URL.indexOf("sort=");
	Integer indexOfSortEnding = URL.indexOf("&", indexOfSort);
	String sortingChoice = URL.substring(indexOfSort + 5, indexOfSortEnding);
	return sortingChoice;
    }
    
    public static String GetMinYearChoice(String URL) {
	Integer indexOfYearMin = URL.indexOf("yearMin=");	
	String minYearChoice = URL.substring(indexOfYearMin + 8);
	return minYearChoice;
    }
}