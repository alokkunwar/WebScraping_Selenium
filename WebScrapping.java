package assignment;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebScrapping {

	public static void main(String[] args) {
        
		//Calling method to save the details related to match Series, Venue, Time & Date, and Match Score.
        getScore();
        
        //Calling the method to save the details related to the upcoming match schedule.
        getSchedules();
        

	}
	
	public static void getScore()
	{
		WebDriver driver = new ChromeDriver();
		
		driver.get("https://www.cricbuzz.com/");
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Maximize the browser window
        driver.manage().window().maximize();
        
        //Navigating to the required page to get the Match details like Match info, Team Name, Venue, time and score.
		driver.findElement(By.className("cb-hm-mnu-itm")).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("cb-lv-scrs-well-live")));
		driver.findElement(By.className("cb-lv-scrs-well-live")).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("matchCenter")));
		
		WebElement element = driver.findElement(By.id("matchCenter"));
		
		//Variable to prepare the content which I am going to save in the CSV file.
		StringBuilder contentBuilder = new StringBuilder();
		
		//Retrieving the Match info (like - which teams are playing this match) from the web page.
		String matchInfo = element.findElement(By.className("cb-nav-hdr")).getText();
		createResult(contentBuilder, matchInfo.replace(",", ""));
		createResult(contentBuilder, "\n");
		
		//Retrieving the series details for this match 
        String series = element.findElement(By.xpath("//*[@id=\"matchCenter\"]/div[2]/div[2]/a[1]/span")).getText();
        createResult(contentBuilder, "Series,");
        createResult(contentBuilder, series.replace(",", ""));
        createResult(contentBuilder, "\n");
		
		//Retrieving the venue details for this match
        String Venue = element.findElement(By.xpath("//*[@id=\"matchCenter\"]/div[2]/div[2]/a[2]/span/span[1]")).getText();
        createResult(contentBuilder, "Venue,");
        createResult(contentBuilder, Venue.replace(",", ""));
        createResult(contentBuilder, "\n");
		
		//Retrieving the date details.
        String date = element.findElement(By.xpath("//*[@id=\"matchCenter\"]/div[2]/div[2]/span[4]")).getText();
        createResult(contentBuilder, "Current Date & Time,");
        createResult(contentBuilder, date.replace(",", ""));
        createResult(contentBuilder, "\n");
		
        //Retrieving the score of the match
        String currScore = element.findElement(By.xpath("//*[@id=\"matchCenter\"]/div[3]/div[2]/div[1]/div[1]/div[1]/h2")).getText();
		createResult(contentBuilder, "Current Score,");
		createResult(contentBuilder, currScore);
		createResult(contentBuilder, "\n");
		
		//Calling the method to write it into the CSV file.
		writeToCSVFile(contentBuilder, "MatchDetails.csv", "Match Details saved to ");	
		
	}
	
	//Internal method to fetch the date from the string for internal manipulation.
	private static int isNumber(String value)
	{
		try 
		{
            return Integer.parseInt(value);
        } 
		catch (NumberFormatException e) {
            return -1; 
        }
	}
	
	//Internal method to count the number of occurrences of a word for internal manipulation. 
	private static int countOccurrences(String inputText, String targetWord) {
        // Split the text into words using space as delimiter
        String[] listOfWords = inputText.split("\\s+");
        
        int counter = 0;
        
        for (String word : listOfWords) 
        {
        	//if match found increase the counter.
            if (word.equals(targetWord)) 
            {
            	counter++;
            }
        }
        return counter;
    }
	
	// Internal method to create the required message which I am going to store in the CSV file.
	private static StringBuilder createResult(StringBuilder contentBuilder, String stringtoAppend) {
		contentBuilder.append(stringtoAppend);	
		
		return contentBuilder;
	}
	
	//Internal method to save the message to the CSV file.
	private static void writeToCSVFile(StringBuilder contentBuilder, String fileName, String messageContnt)
	{
		 try 
		 {
			 //Creating the FileWriter object 
            FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8);
            
            //Writing to the file
            writer.write(contentBuilder.toString());
            
            //closing it.
            writer.close();
            
            //Printing the message to the console.
            System.out.println( messageContnt + " " + fileName);
	     } 
		 catch (IOException e) 
		 {
			 System.out.println( "Error is : " + e.getMessage());
	     }
	}
	
	//Method to retrieve the upcoming match schedule.
	public static void getSchedules()
	{
		WebDriver driver = new ChromeDriver();
		
		driver.get("https://www.cricbuzz.com/");
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.manage().window().maximize();
		
		driver.findElement(By.xpath("//*[@id=\"cb-main-menu\"]/a[3]")).click();
		
		//Getting the elements under which all the upcoming schedule is located. 
        WebElement scheduleLists = driver.findElement(By.id("international-list"));
        
        //Retrieving all the schedule from the above element.
        List<WebElement> matchElements = scheduleLists.findElements(By.cssSelector("div.cb-col-100.cb-col"));
        
        
        //Variable to store the content which I am going to save in the CSV file.
		StringBuilder contentBuilder = new StringBuilder();
		
		
		//Header which is going to be reflected in the CSV file.
		createResult(contentBuilder, "Date,");
		createResult(contentBuilder, "Series,");
		createResult(contentBuilder, "Team,");
		createResult(contentBuilder, "Venue,");
		createResult(contentBuilder, "Local Start Time,");
		createResult(contentBuilder, "Match Start Time,");
		createResult(contentBuilder, "\n");
		
		//Iterating each schedule element.
		for (WebElement element : matchElements) 
		{
			int localWordOccurances = countOccurrences(element.getText(), "LOCAL");
			
			String inputString =  element.getText();
			
			
			//Splitting the lines of the each schedule element based on below condition.
			String[] lines = inputString.split("\\r?\\n");
			
			int NoOfLineCount = 0;
			
			boolean previousStringhasLocal = false;
			
			//Iterating over each line of a schedule and formating it so that it can be stored properly into the CSV file.
			for (String line : lines) 
			{
				
				if(NoOfLineCount == 0)
				{
					String extractDate = line.substring(line.length() - 7, line.length() - 5);
				
					int dateValue = isNumber(extractDate.trim());
					
					if(dateValue == -1)
					 break;
						
				}
				
				
				if(localWordOccurances > 1 && previousStringhasLocal)
				{
					createResult(contentBuilder, "\n,");
					previousStringhasLocal = false;
				}
				else
				{
					if(line.contains("LOCAL") && localWordOccurances > 1)
						previousStringhasLocal = true;
				}
				
				createResult(contentBuilder, line.replace(",", ""));	
				createResult(contentBuilder, ",");
				NoOfLineCount++;
			}
			createResult(contentBuilder, "\n");
        }
		 
		//Calling the method to save it into the CSV file.
		writeToCSVFile(contentBuilder, "MatchSchedule.csv", "Match schedule saved to ");
		
	}

}
