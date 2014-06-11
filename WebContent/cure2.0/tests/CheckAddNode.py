from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support.ui import WebDriverWait # available since 2.4.0
from selenium.webdriver.support import expected_conditions as EC # available since 2.26.0

# Create a new instance of the Firefox driver
class AddNode():
	def __init__(self, driver,url):
		self.driver = driver
		self.url = url
	

	
	def enterGene(self):
		driver = self.driver
		def clearTour(driver):
			WebDriverWait(driver,15).until(EC.element_to_be_clickable((By.CSS_SELECTOR,".popover-navigation > .btn")))
			driver.find_element_by_css_selector(".popover-navigation > .btn").click()
                def newTree(driver):
                        driver.find_element_by_id("gene_query").click()
		clearTour(driver)
		WebDriverWait(driver,10).until(EC.presence_of_element_located((By.ID,"gene_query")))
		geneElement = driver.find_element_by_id("gene_query")
		geneElement.send_keys("AURKA")
		def check_results(driver):
			try:
				listElement = driver.find_element_by_css_selector("li.ui-menu-item:nth-child(1) > a")
				print "First Result: "+listElement.get_attribute('innerHTML')
				return "AURKA" in listElement.get_attribute('innerHTML')
			except:	
				return False
		WebDriverWait(driver, 10).until(check_results)
		driver.find_element_by_css_selector("li.ui-menu-item:nth-child(1) > a").click()
		try:
			WebDriverWait(driver,10).until(EC.presence_of_element_located((By.CSS_SELECTOR,"div.node:nth-child(5)")))
		except:
			print "Tree not rendered."
                clearTour(driver)
                newTree(driver)



		


