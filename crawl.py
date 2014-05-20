import time
import datetime
import requests
from bs4 import BeautifulSoup
from time import gmtime, strftime

counter = 0

def GetURLTextAuth (a_url):
  for i in range(0,5):
      try:
	 r = requests.get(a_url, timeout=5, auth=('igniipotentwebsite@gmail.com', 'SeekingAlpha'))
	 if len(r.text) == 0:
	    continue
	 return r.text
      except Exception as exc:
	 print(type(exc))
	 pass

def GetURLText (a_url):
  for i in range(0,5):
      try:
	 r = requests.get(a_url, timeout=5)
	 if len(r.text) == 0:
	    continue
	 return r.text
      except Exception as exc:
	 print(type(exc))
	 pass

def ProcessSeekingAlpha ():
   global counter
   seeking_alpha_start_url = 'http://www.seekingalpha.com'
   seeking_alpha_alt_start_url = 'http://seekingalpha.com/analysis/macro-view/all'
   seeking_alpha_base_url = 'http://www.seekingalpha.com'
   seeking_alpha_write_path = '/home/ubuntu/news_scrape/seekingalpha_data/'
   html = GetURLTextAuth(seeking_alpha_start_url)
   soup = BeautifulSoup (html, 'html5lib')

   divs = soup.findAll('div', attrs={'id':'hp_news_unit'})
   for a_div in divs:
      for a_link in a_div.findAll('a'):
	 a_url = a_link['href']
	 new_html = GetURLTextAuth (seeking_alpha_base_url + a_url)
         print (seeking_alpha_base_url + a_url)
	 time = strftime("%Y-%m-%d %H:%M:%S")
	 time = time.replace(' ', '_').replace(':','-')
	 f = open (seeking_alpha_write_path + str(counter) + '_' + time + '.html', 'w')
	 f.write(new_html.encode('utf-8'))
	 f.close()
	 counter += 1

   divs = soup.findAll('div', attrs={'class':'articles'})
   for a_div in divs:
      for a_list in a_div.findAll('li'):
	 for a_link in a_list.findAll('a'):
	    a_url = a_link['href']
	    new_html = GetURLTextAuth (seeking_alpha_base_url + a_url)
	    print (seeking_alpha_base_url + a_url)
	    time = strftime("%Y-%m-%d %H:%M:%S")
	    time = time.replace(' ', '_').replace(':','-')
	    f = open (seeking_alpha_write_path + str(counter) + '_' + time + '.html', 'w')
	    f.write(new_html.encode('utf-8'))
	    f.close()
	    counter += 1
            break
   
   html = GetURLTextAuth(seeking_alpha_alt_start_url)
   soup = BeautifulSoup (html, 'html5lib')

   for a_link in soup.findAll('a', attrs={'class':'article_title'}):
      a_url = a_link['href']
      new_html = GetURLTextAuth (seeking_alpha_base_url + a_url)
      print (seeking_alpha_base_url + a_url)
      time = strftime("%Y-%m-%d %H:%M:%S")
      time = time.replace(' ', '_').replace(':','-')
      f = open (seeking_alpha_write_path + str(counter) + '_' + time + '.html', 'w')
      f.write(new_html.encode('utf-8'))
      f.close()
      counter += 1


def ProcessDailyFX ():
   global counter
   dailyfx_start_url = 'http://www.dailyfx.com/forex_market_news/'
   dailyfx_base_url = 'http://www.dailyfx.com'
   dailyfx_write_path = '/home/ubuntu/news_scrape/dailyfx_data/'
   html = GetURLText(dailyfx_start_url)
   soup = BeautifulSoup (html, 'html5lib')

   divs = soup.findAll('div', attrs={'class':'secondary-box-content'})

   for a_div in divs:
      for a_link in a_div.findAll('a'):
	 a_url = a_link['href']
	 new_html = GetURLText (dailyfx_base_url + a_url)
	 time = strftime("%Y-%m-%d %H:%M:%S")
	 time = time.replace(' ', '_').replace(':','-')
	 f = open (dailyfx_write_path + str(counter) + '_' + time + '.html', 'w')
	 f.write(new_html.encode('utf-8'))
	 f.close()
	 counter += 1
         break

   divs = soup.findAll('div', attrs={'class':'main-article-non-home'})

   for a_div in divs:
      for a_link in a_div.findAll('a'):
	 a_url = a_link['href']
	 new_html = GetURLText (dailyfx_base_url + a_url)
	 time = strftime("%Y-%m-%d %H:%M:%S")
	 time = time.replace(' ', '_').replace(':','-')
	 f = open (dailyfx_write_path + str(counter) + '_' + time + '.html', 'w')
	 f.write(new_html.encode('utf-8'))
	 f.close()
	 counter += 1
         break


def ProcessCNBC ():
   global counter
   cnbc_start_url = 'http://www.cnbc.com/id/15839121/'
   cnbc_base_url = 'http://www.cnbc.com'
   cnbc_write_path = '/home/ubuntu/news_scrape/cnbc_data/'
   html = GetURLText(cnbc_start_url)
   soup = BeautifulSoup (html, 'html5lib')

   divs = soup.findAll('div', attrs={'class':'asset cnbcnewsstory big'})

   for a_div in divs:
      for a_link in a_div.findAll('a'):
	 a_url = a_link['href']
	 new_html = GetURLText (cnbc_base_url + a_url)
	 time = strftime("%Y-%m-%d %H:%M:%S")
	 time = time.replace(' ', '_').replace(':','-')
	 f = open (cnbc_write_path + str(counter) + '_' + time + '.html', 'w')
	 f.write(new_html.encode('utf-8'))
	 f.close()
	 counter += 1

   divs = soup.findAll('div', attrs={'class':'asset cnbcnewsstory'})

   for a_div in divs:
      for a_link in a_div.findAll('a'):
	 a_url = a_link['href']
	 new_html = GetURLText (cnbc_base_url + a_url)
	 time = strftime("%Y-%m-%d %H:%M:%S")
	 time = time.replace(' ', '_').replace(':','-')
	 f = open (cnbc_write_path + str(counter) + '_' + time + '.html', 'w')
	 f.write(new_html.encode('utf-8'))
	 f.close()
	 counter += 1
   
   

def ProcessBloomberg():
   global counter
   bloomberg_start_url = 'http://www.bloomberg.com/news/'
   bloomberg_base_url = 'http://www.bloomberg.com'
   bloomberg_write_path = '/home/ubuntu/news_scrape/bloomberg_data/'

   html = GetURLText(bloomberg_start_url)
   soup = BeautifulSoup (html, 'html5lib')

   divs = soup.findAll('div', attrs={'id':'markets_news'})

   for a_div in divs:
      for a_link in a_div.findAll('a'):
	 a_url = a_link['href']
	 if a_url != '/news/markets/':
	    new_html = GetURLText (bloomberg_base_url + a_url)
	    time = strftime("%Y-%m-%d %H:%M:%S")
	    time = time.replace(' ', '_').replace(':','-')
	    f = open (bloomberg_write_path + str(counter) + '_' + time + '.html', 'w')
	    f.write(new_html.encode('utf-8'))
	    f.close()
	    counter += 1

   divs = soup.findAll('div', attrs={'id':'industries_news'})

   for a_div in divs:
      for a_link in a_div.findAll('a'):
	 a_url = a_link['href']
	 if a_url != '/news/industries/':
	    new_html = GetURLText (bloomberg_base_url + a_url)
	    time = strftime("%Y-%m-%d %H:%M:%S")
	    time = time.replace(' ', '_').replace(':','-')
	    f = open (bloomberg_write_path + str(counter) + '_' + time + '.html', 'w')
	    f.write(new_html.encode('utf-8'))
	    f.close()
	    counter += 1

   divs = soup.findAll('div', attrs={'id':'economy_news'})

   for a_div in divs:
      for a_link in a_div.findAll('a'):
	 a_url = a_link['href']
	 if a_url != '/news/economy/':
	    new_html = GetURLText (bloomberg_base_url + a_url)
	    time = strftime("%Y-%m-%d %H:%M:%S")
	    time = time.replace(' ', '_').replace(':','-')
	    f = open (bloomberg_write_path + str(counter) + '_' + time + '.html', 'w')
	    f.write(new_html.encode('utf-8'))
	    f.close()
	    counter += 1




while 1==1:

   t = datetime.datetime.today()

   if t.hour == 9 and t.minute == 25:
      ProcessBloomberg()
      ProcessCNBC()
      ProcessDailyFX()
      ProcessSeekingAlpha()
   elif t.hour == 12 and t.minute == 5:
      ProcessBloomberg()
      ProcessCNBC()
      ProcessDailyFX()
      ProcessSeekingAlpha()
   elif t.hour == 17 and t.minute == 5:
      ProcessBloomberg()
      ProcessCNBC()
      ProcessDailyFX()
      ProcessSeekingAlpha()
   elif t.hour == 22 and t.minute == 5:
      ProcessBloomberg()
      ProcessCNBC()
      ProcessDailyFX()
      ProcessSeekingAlpha()
   else:
      time.sleep(30)



























