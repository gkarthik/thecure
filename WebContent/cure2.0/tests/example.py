from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support.ui import WebDriverWait # available since 2.4.0
from selenium.webdriver.support import expected_conditions as EC # available since 2.26.0

# Create a new instance of the Firefox driver
class LoginTest():
    def __init__(self, driver, url):
        self.driver = driver
        self.url = url
    
    def loadLoginPage(self):
        driver = self.driver
        try:
            driver.get(self.url)
            print "Server Running..."
            print "Home Page Loaded: "+driver.title
        except: 
            print "Server could not be loaded"

        try:
            inputElement = driver.find_element_by_css_selector(".playnow")
            inputElement.click()
        except:
            print ".playnow not found."
        try:
            WebDriverWait(driver,0).until(EC.title_contains("Login"))
            print "Login Page Loaded: "+driver.title
        except:
            print "Login not in title of page. Most likely Login Page not loaded."
    
    def enterGamePage(self):
        driver = self.driver
        try:
            userElement = driver.find_element_by_id("usernameinput")
            userElement.send_keys("gk")
            passElement = driver.find_element_by_id("passwordinput")
            passElement.send_keys("123")
            passElement.submit()
        except:
            print "Couldn't Login"
        try:
            WebDriverWait(driver, 0).until(EC.presence_of_element_located((By.ID,"gene_query")))
        except:
            print "Gene Query Imput not Found. Most Likely Login Failed."
        


