from example import LoginTest
from CheckAddNode import AddNode
from selenium import webdriver

def LoginTests(driver,url):
	login = LoginTest(driver,url)
	login.loadLoginPage()
	login.enterGamePage()

def AddNodeTests(driver,url):
	addnode = AddNode(driver,url)
	addnode.enterGene()

driver = webdriver.Firefox()
url = "http://localhost:8080/cure/"
LoginTests(driver,url)
AddNodeTests(driver,url)

