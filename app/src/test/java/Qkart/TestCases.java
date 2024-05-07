package Qkart;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import Qkart.pages.Checkout;
import Qkart.pages.Home;
import Qkart.pages.Login;
import Qkart.pages.Register;
import Qkart.pages.SearchResult;
import io.github.bonigarcia.wdm.WebDriverManager;

public class TestCases {

    static WebDriver driver;
    public static String lastGeneratedUserName;

    @BeforeSuite(alwaysRun = true)
    public static void createDriver() throws MalformedURLException {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    /*
     * Testcase01: Verify a new user can successfully register
     */
    @Test(priority = 1, description = "Verify registration happens correctly", groups = { "Sanity_test" })
    @Parameters({ "USERNAME", "PASSWORD" })
    public void TestCase01(@Optional("USERNAME") String USERNAME, @Optional("PASSWORD") String PASSWORD)
            throws InterruptedException {
        Boolean status;
        // takeScreenshot(driver, "StartTestCase", "TestCase1");

        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser(USERNAME, PASSWORD, true);
        Assert.assertTrue(status, "Failed to register new user");

        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Visit the login page and login with the previuosly registered user
        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, PASSWORD);
        Assert.assertTrue(status, "Failed to login with registered user");

        // Visit the home page and log out the logged in user
        Home home = new Home(driver);
        status = home.PerformLogout();

    }

    @AfterSuite(alwaysRun = true)
    public static void quitDriver() {
        System.out.println("quit()");
        driver.quit();
    }

    @Test(priority = 2, description = "Verify re-registering an already registered user fails", groups = {"Sanity_test"})
    @Parameters({"USERNAME","PASSWORD"})
    public void TestCase02(@Optional("USERNAME") String USERNAME, @Optional("PASSWORD") String PASSWORD) throws InterruptedException {
        Boolean status;

        // Visit the Registration page and register a new user
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser(USERNAME, PASSWORD, true);
        Assert.assertTrue(status, "Failed to register new user");

        // Save the last generated username
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Visit the Registration page and try to register using the previously
        // registered user's credentials
        registration.navigateToRegisterPage();
        status = registration.registerUser(lastGeneratedUserName, PASSWORD, false);
        Assert.assertFalse(status,"User re-registered");
        
    }

    /*
     * Verify the functinality of the search text box
     */
    
     @Test(priority = 3, description = "Verify the functionality of search text box", groups = {"Sanity_test"})
     @Parameters({"SEARCH_PRODUCT_T3"})
    public void TestCase03(String SEARCH_PRODUCT_T3) throws InterruptedException {
        boolean status;

        // Visit the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Search for the "yonex" product
        status = homePage.searchForProduct(SEARCH_PRODUCT_T3);
       
        Assert.assertTrue(status,"Unable to search the given product");

        // Fetch the search results
        List<WebElement> searchResults = homePage.getSearchResults();

        // Verify the search results are available
        Assert.assertFalse(searchResults.size()==0,"No Results found");

        for (WebElement webElement : searchResults) {
            // Create a SearchResult object from the parent element
            SearchResult resultelement = new SearchResult(webElement);

            // Verify that all results contain the searched text
            String elementText = resultelement.getTitleofResult();
            
            Assert.assertFalse(!elementText.toUpperCase().contains(SEARCH_PRODUCT_T3), "Not found the element looking for");
        }

        // Search for product
        status = homePage.searchForProduct("Gesundheit");

        // Verify no search results are found
        searchResults = homePage.getSearchResults();
        Assert.assertTrue(searchResults.size()==0 && homePage.isNoResultFound(), "No result found is Not displayed");
    
    }

    /*
     * Verify the presence of size chart and check if the size chart content is as
     * expected
     */

     @Test(priority = 4, description = "Verify the existence of size chart for certain items and validate contents of size chart", groups = {"Regression_Test"})
     @Parameters({"SEARCH_PRODUCT_T4"})
    public void TestCase04(String SEARCH_PRODUCT_T4) throws InterruptedException {
        // Visit home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        homePage.searchForProduct(SEARCH_PRODUCT_T4);
        List<WebElement> searchResults = homePage.getSearchResults();

        // Create expected values
        List<String> expectedTableHeaders = Arrays.asList("Size", "UK/INDIA", "EU", "HEEL TO TOE");
        List<List<String>> expectedTableBody = Arrays.asList(Arrays.asList("6", "6", "40", "9.8"),
                Arrays.asList("7", "7", "41", "10.2"), Arrays.asList("8", "8", "42", "10.6"),
                Arrays.asList("9", "9", "43", "11"), Arrays.asList("10", "10", "44", "11.5"),
                Arrays.asList("11", "11", "45", "12.2"), Arrays.asList("12", "12", "46", "12.6"));

        // Verify size chart presence and content matching for each search result
        for (WebElement webElement : searchResults) {
            SearchResult result = new SearchResult(webElement);

            Boolean isSizeChartExists = result.verifySizeChartExists();

            Assert.assertTrue(isSizeChartExists, "Size Chart Link does not exist");

            if (isSizeChartExists) {
                // Verify if size dropdown exists
                Boolean isSizeDropdownExist = result.verifyExistenceofSizeDropdown(driver);
                Assert.assertTrue(isSizeDropdownExist, "Size dropdown doesn't exist");

                // Open the size chart
                Boolean isSizeChartOpenSuccess = result.openSizechart();
                Assert.assertTrue(isSizeChartOpenSuccess, "Failed to open Size Chart");

                if (isSizeChartOpenSuccess) {
                    // Verify if the size chart contents matches the expected values
                    Boolean isChartContentMatching = result.validateSizeChartContents(expectedTableHeaders,
                            expectedTableBody, driver);
                    Assert.assertTrue(isChartContentMatching, "Failure while validating contents of Size Chart Link");

                    // Close the size chart modal
                    Boolean isSizeChartClosed = result.closeSizeChart(driver);
                    Assert.assertTrue(isSizeChartClosed, "Closing size chart failed");
                }

            }
        }

    }

    /*
     * Verify the complete flow of checking out and placing order for products is
     * working correctly
     */

     @Test(priority = 5, description = "Verify that a new user can add multiple products in to the cart and Checkout", groups = {"Sanity_test"})
     @Parameters({"SEARCH_PRODUCT_1","SEARCH_PRODUCT_2", "ADDRESS"})
    public void TestCase05(String SEARCH_PRODUCT_1,String SEARCH_PRODUCT_2,String ADDRESS) throws InterruptedException {
        Boolean status;

        // Go to the Register page
        Register registration = new Register(driver);
        registration.navigateToRegisterPage();

        // Register a new user
        status = registration.registerUser("testUser", "abc@123", true);
        
        Assert.assertTrue(status, "Registration failed");

        // Save the username of the newly registered user
        lastGeneratedUserName = registration.lastGeneratedUsername;

        // Go to the login page
        Login login = new Login(driver);
        login.navigateToLoginPage();

        // Login with the newly registered user's credentials
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue(status, "Login failed");

        // Go to the home page
        Home homePage = new Home(driver);
        homePage.navigateToHome();

        // Find required products by searching and add them to the user's cart
        status = homePage.searchForProduct(SEARCH_PRODUCT_1);
        homePage.addProductToCart(SEARCH_PRODUCT_1);
        status = homePage.searchForProduct(SEARCH_PRODUCT_2);
        homePage.addProductToCart(SEARCH_PRODUCT_2);

        // Click on the checkout button
        homePage.clickCheckout();

        // Add a new address on the Checkout page and select it
        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(ADDRESS);
        checkoutPage.selectAddress(ADDRESS);

        // Place the order
        checkoutPage.placeOrder();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));

        // Check if placing order redirected to the Thansk page
        status = driver.getCurrentUrl().endsWith("/thanks");
        Assert.assertTrue(status, "Placing order didn't redirect to Thanks page");

        // Go to the home page
        homePage.navigateToHome();

        // Log out the user
        homePage.PerformLogout();

    }

    /*
     * Verify the quantity of items in cart can be updated
     */
    
     @Test(priority = 6, description = "Verify that the contents of the cart can be edited", groups = {"Regression_Test"})
     @Parameters({"SEARCH_PRODUCT_3","SEARCH_PRODUCT_4"})
    public void TestCase06(String SEARCH_PRODUCT_3, String SEARCH_PRODUCT_4) throws InterruptedException {
        Boolean status;
        Home homePage = new Home(driver);
        Register registration = new Register(driver);
        Login login = new Login(driver);

        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        
        Assert.assertTrue(status, "Registration failed");
        lastGeneratedUserName = registration.lastGeneratedUsername;

        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue(status, "Login failed");

        homePage.navigateToHome();
        status = homePage.searchForProduct(SEARCH_PRODUCT_3);
        homePage.addProductToCart(SEARCH_PRODUCT_3);

        status = homePage.searchForProduct(SEARCH_PRODUCT_4);
        homePage.addProductToCart(SEARCH_PRODUCT_4);

        // update watch quantity to 2
        homePage.changeProductQuantityinCart(SEARCH_PRODUCT_3, 2);

        // update table lamp quantity to 0
        homePage.changeProductQuantityinCart(SEARCH_PRODUCT_4, 0);

        // update watch quantity again to 1
        homePage.changeProductQuantityinCart(SEARCH_PRODUCT_3, 1);

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
        checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

        checkoutPage.placeOrder();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.urlToBe("https://crio-qkart-frontend-qa.vercel.app/thanks"));
        } catch (TimeoutException e) {
            Assert.assertTrue(false, "Error while placing order: " + e.getMessage());
        }

        status = driver.getCurrentUrl().endsWith("/thanks");
        Assert.assertTrue(status, "Wasn't redirected to the Thanks page");

        homePage.navigateToHome();
        homePage.PerformLogout();

    }



    @Test(priority = 7, description = "Verify that insufficient balance error is thrown when the wallet balance is not enough", groups = {"Sanity_test"})
    @Parameters({"SEARCH_PRODUCT_5", "ITEM_QTY"})
    public void TestCase07(String SEARCH_PRODUCT_5, int ITEM_QTY) throws InterruptedException {
        Boolean status;

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
    
        Assert.assertTrue(status, "Registration failed");

        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
       
        Assert.assertTrue(status, "Login failed");

        Home homePage = new Home(driver);
        homePage.navigateToHome();
        status = homePage.searchForProduct(SEARCH_PRODUCT_5);
        homePage.addProductToCart(SEARCH_PRODUCT_5);

        homePage.changeProductQuantityinCart(SEARCH_PRODUCT_5, ITEM_QTY);

        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress("Addr line 1 addr Line 2 addr line 3");
        checkoutPage.selectAddress("Addr line 1 addr Line 2 addr line 3");

        checkoutPage.placeOrder();
        Thread.sleep(3000);

        status = checkoutPage.verifyInsufficientBalanceMessage();
        Assert.assertTrue(status, "Insufficient balance not displayed");

    }

    @Test(priority = 8, description = "Verify that a product added to a cart is available when a new tab is added", groups = {"Regression_Test"})
    public void TestCase08() throws InterruptedException {
        Boolean status = false;

        // takeScreenshot(driver, "StartTestCase", "TestCase09");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);

        //     takeScreenshot(driver, "Failure", "TestCase09");

        Assert.assertTrue(status, "Registration failed");
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        
        Assert.assertTrue(status, "Login failed");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        status = homePage.searchForProduct("YONEX");
        homePage.addProductToCart("YONEX Smash Badminton Racquet");

        String currentURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);

        driver.get(currentURL);
        Thread.sleep(2000);

        List<String> expectedResult = Arrays.asList("YONEX Smash Badminton Racquet");
        status = homePage.verifyCartContents(expectedResult);
        Assert.assertTrue(status, "Cart contents cannot be verified");

        driver.close();

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

        // takeScreenshot(driver, "EndTestCase", "TestCase08");

    }



    @Test(priority = 9, description = "Verify that privacy policy and about us links are working fine", groups = {"Regression_Test"})
    public void TestCase09() throws InterruptedException {
        Boolean status = false;

        // takeScreenshot(driver, "StartTestCase", "TestCase09");

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        
        Assert.assertTrue(status, "Registration failed");
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        
        Assert.assertTrue(status, "Login failed");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        String basePageURL = driver.getCurrentUrl();

        driver.findElement(By.linkText("Privacy policy")).click();
        status = driver.getCurrentUrl().equals(basePageURL);
        Assert.assertTrue(status, "Verifying parent page url didn't change on privacy policy link click failed");

        Set<String> handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]);
        WebElement PrivacyPolicyHeading = driver.findElement(By.xpath("//h2[text()='Privacy Policy']"));
        status = PrivacyPolicyHeading.getText().equals("Privacy Policy");
        Assert.assertTrue(status, "Verifying new tab opened has Privacy Policy page heading failed");

        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);
        driver.findElement(By.linkText("Terms of Service")).click();

        handles = driver.getWindowHandles();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[2]);
        WebElement TOSHeading = driver.findElement(By.xpath("//h2[text()='Terms of Service']"));
        status = TOSHeading.getText().equals("Terms of Service");
        Assert.assertTrue(status,"Verifying new tab opened has Terms Of Service page heading failed");

        driver.close();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[1]).close();
        driver.switchTo().window(handles.toArray(new String[handles.size()])[0]);

    }



    @Test(priority = 10, description = "Verify that the contact us dialog works fine", groups = {"Sanity_test"})
    @Parameters({"INPUT_1_T9", "INPUT_2_T9", "INPUT_3_T9"})
    public void TestCase10(String INPUT_1_T9, String INPUT_2_T9, String INPUT_3_T9) throws InterruptedException {

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        driver.findElement(By.xpath("//*[text()='Contact us']")).click();

        WebElement name = driver.findElement(By.xpath("//input[@placeholder='Name']"));
        name.sendKeys(INPUT_1_T9);
        WebElement email = driver.findElement(By.xpath("//input[@placeholder='Email']"));
        email.sendKeys(INPUT_2_T9);
        WebElement message = driver.findElement(By.xpath("//input[@placeholder='Message']"));
        message.sendKeys(INPUT_3_T9);

        WebElement contactUs = driver.findElement(By.xpath("//button[text()=' Contact Now']"));

        contactUs.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        wait.until(ExpectedConditions.invisibilityOf(contactUs));

    }



    @Test(priority = 11, description = "Ensure that the Advertisement Links on the QKART page are clickable", groups = {"Sanity_test", "Regression_Test"})
    @Parameters({"SEARCH_PRODUCT_1", "ADDRESS"})
    public void TestCase11(String SEARCH_PRODUCT_1, String ADDRESS) throws InterruptedException {
        Boolean status = false;

        Register registration = new Register(driver);
        registration.navigateToRegisterPage();
        status = registration.registerUser("testUser", "abc@123", true);
        
        Assert.assertTrue(status, "Registration Failed");
        lastGeneratedUserName = registration.lastGeneratedUsername;

        Login login = new Login(driver);
        login.navigateToLoginPage();
        status = login.PerformLogin(lastGeneratedUserName, "abc@123");
        Assert.assertTrue(status,"Login failed");

        Home homePage = new Home(driver);
        homePage.navigateToHome();

        status = homePage.searchForProduct(SEARCH_PRODUCT_1);
        homePage.addProductToCart(SEARCH_PRODUCT_1);
        homePage.changeProductQuantityinCart(SEARCH_PRODUCT_1, 1);
        homePage.clickCheckout();

        Checkout checkoutPage = new Checkout(driver);
        checkoutPage.addNewAddress(ADDRESS);
        checkoutPage.selectAddress(ADDRESS);
        checkoutPage.placeOrder();
        Thread.sleep(3000);

        String currentURL = driver.getCurrentUrl();

        List<WebElement> Advertisements = driver.findElements(By.xpath("//iframe"));

        status = Advertisements.size() == 3;

        WebElement Advertisement1 = driver.findElement(By.xpath("(//iframe)[1]"));
        driver.switchTo().frame(Advertisement1);
        driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
        driver.switchTo().parentFrame();

        status = !driver.getCurrentUrl().equals(currentURL);

        driver.get(currentURL);
        Thread.sleep(3000);

        WebElement Advertisement2 = driver.findElement(By.xpath("(//iframe)[2]"));
        driver.switchTo().frame(Advertisement2);
        driver.findElement(By.xpath("//button[text()='Buy Now']")).click();
        driver.switchTo().parentFrame();

        status = !driver.getCurrentUrl().equals(currentURL);

    }



}
