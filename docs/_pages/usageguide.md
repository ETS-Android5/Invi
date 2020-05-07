---
layout: page
title: Users Guide
icon: fa fa-mobile-alt
include_in_header: true

---

This guide will cover installation of Invi Application and show a step by step guide on using its features


## Installation

To install Invi on your phone please click <a href="https://github.com/pk-development/apps/raw/master/app-release.apk"><b>Download</b></a>  and install the app on your device 

Once the file is downloaded it will be in the downloads folder on your phone, the file will be named app-release.apk

Because this app is not signed by google play store you will have to allow 3rd party app installation from the settings on your phone or when you are prompted by the package installer during open of the apk file.

### Test Account

user =      paulkinselladev[(AT)]gmail.com 
password =  @#Abc1234

please replace [(AT)] with @ as this is to avoid bots from scraping email

## User Registration

<div class="post-container">                
    <div class="post-thumb"><img src="/assets/screenshot/yourscreenshot.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Login & Registration Screen</h3>
        <p>Once app is installed "Invis'" Logo will appear on the apps section of your phone. Open the app and you will be presented this the the screen on the left.</p>
    <p>If you want to register with Google, Twitter or Facebook please slide the icon to the right. To begin registering with an email click on the registration link on the bottom left</p>
    </div>
</div>
<br>
<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_reg.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Registration Screen</h3>
        <p>Please enter your first name, last name, email and enter a password then click the register button</p>
    </div>
</div>
<br>
<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_ver1.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Verify Screen</h3>
        <p>In order to use Invi your email and phone must be verfied. If you click on (Verify Email) this will send an email with a link to verify to your email address. </p>
    </div>
</div>
<br>
<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_ver2.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Verify Number Screen</h3>
        <p>When you click the verify phone button you will be greeted by this screen. To verify your phone please select country code and enter your phone number then click send code which will send an sms code to your device. Once phone and email is verified your login should be automatic.<br><b>*** Please be aware this api from google is buggy and sometimes does not send a code. To bypass this manually contact Invi admin or if your an admin and have access to Firebase, go to Firebase Databse -> Collections: users -> Document: your email and set the filed - is_phone_verified = true ***</b></p>
       
 </div>
</div>

## User Login

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_login2.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Login Screen</h3>
        <p>To login to app simply enter your email & password and you should be greeted with the home screen</p>
       
 </div>
</div>

## Start Scanning

### Scan Rental Items
<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_cart2.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Scanner Screen</h3>
        <p>Invi allows 2 types of item scans. Items that can be purchased and items that can be rented. To start scanning click on the blue button on the bottom right of screen then click the QR icon which will bring you to the scanning screen as seen in image on the left. Please use the test barcodes below to start scanning rental items, purchase items or tesco items. You can also try tesco products you have at home. <br><br><b>Tesco api doesn't allow verbose product detection so some items will not be picked up from the tesco api</b></p>
 </div>
</div>

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_rent1.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Rental Checkout Screen</h3>
        <p>If you scan a rentable item and that current item is not out on rent you will be greeted with the rent dialog. The rent to currently scanned item click the rent item.
        <br><br></p>
 </div>
</div>
## Rental & Purcahse
<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_rent2.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Rental Items List</h3>
        <p>To view current rented items and your current cost please click on the side menu and click rentals. You will be greeted with this screen where you can see the details and total cost of your rented items
        <br><br>You can also search through your rented items if you have a large list by clicking the search icon on the top right and entering a search term</p>
 </div>
</div>

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_rent4.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Rental Check in Screen</h3>
        <p>To check back in a rented item just scan the barcode and you will be present with the screen on the left. When the item is checked back in you will have a Receipt of the cost in the Receipts page.
        <br><br></p>
 </div>
</div>

### Scan Purcahse Items
<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_cart2.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Scanner</h3>
        <p>To scan a purchase item please click the blue button on the bottom left screen the click the QR scan button.</p>
 </div>
</div>

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_p1.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Purcahse Item</h3>
        <p>When you scan a purchase item either invi or tesco you will be greeted with this screen. Choose the quantity and click add to cart. The items will be added to your cart which you can click the cart icon or side nav menu to open the cart list</p>
 </div>
</div>

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_plist.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Cart List</h3>
        <p>To open your cart to see current items click on the left nav menu or click on the cart icon button.<br><br>You can also search through your cart items if you have a large list by clicking the search icon on the top right and entering a search term</p>
 </div>
</div>

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_return.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Cart Item Return</h3>
        <p>To return an item in your list please swipe left on the item as see in image</p></div>
</div>

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_plist.jpg" width="150px" height="250px"/></div>
    <div class="post-thumb"><img src="../images/screen_cardpay.jpg" width="150px" height="250px"/></div>
    <div class="post-content">
        <h3 class="post-title">Cart Payment</h3>
        <p>To pay for items in your cart click on the dollar sign icon on the title bar at top of screen. You can pay for items with cash or card. <b>Please add a card or you will not be allowed to pay for items with card option</b>. A fake card number is ok for the test. <br><br>Once items are paid for they will appear in your Receipts list.</p>
    </div>
</div>

## Payments Screen

### Add Credit Card

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_payments3.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Add Card</h3>
        <p>To add a credit card simple take an image of the your credit card. Some cards with raised lettering my not be detected, if this is the case please add the credit card manually. The add card screen also has a log of your credit card transactions<br><br> <b>*** Remember in order to pay for items by card, you neeed to add one ***</b></p>
       
 </div>
</div>

### Receipts

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_reciepts.jpg" width="150px" height="250px"/></div>
    <div class="post-thumb"><img src="../images/screen_recieptslist.jpg" width="150px" height="250px"/></div>
    <div class="post-content">
        <h3 class="post-title">Receipt Screen</h3>
        <p>Every rental or purchase item will create receipt after payment of that item. A detail of all items of the order can be viewed by clicking on a recipet item in the list. You can also filter items by typing a price or Rental etc</p>
    </div>
</div>



## Misc Screens

### Map

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_maps1.jpg" width="150px" height="100%"/></div>
    <div class="post-thumb"><img src="../images/screen_maps2.jpg" width="150px" height="100%"/></div>
    <div class="post-content">
        <h3 class="post-title">Map Screen</h3>
        <p style="margin-left: 5px;">On the map screen you can view currently available stores. If you click on a map marker you can see the name of the store in a label, when you click on the label this will bring up infomation about the store like open times and phone number.<br><br><strong>Note</strong><br> On the first use a permission window will appear asking for access to Location and Phone. Once granted click on the maps icon again to see all the stores mapped</p>
    </div>
</div>

### Profile

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_acc.jpg" width="150px" height="250px"/></div>
    <div class="post-thumb"><img src="../images/screen_acc2.jpg" width="150px" height="250px"/></div>
    <div class="post-thumb"><img src="../images/screen_acc3.jpg" width="150px" height="250px"/></div>
    <div class="post-content">
        <h3 class="post-title">User Profile</h3>
        <p>You can access the user profile page by selecting the user icon in the title bar. The user profile allows you to set details about yourself, link to social media accounts, update user profile image and account deletion.</p>
    </div>
</div>

### Currency

<div class="post-container">                
    <div class="post-thumb"><img src="../images/screen_cur.jpg" width="200px" height="300px"/></div>
    <div class="post-content">
        <h3 class="post-title">Currency Screen</h3>
        <p>On this screen you can set your currency and prices will be reflected in your set currency, default is Euro.</p>
    </div>
</div>

## Test Barcodes

<p align="center">
  Here is a list of barcodes for the invi store and also some known barcodes that were taking from tesco products
<table>
  <tr>
    <th colspan="3">Invi Rental Items</th>
  </tr>
  <tr>
    <td><img src="../images/mask1.gif"/>Mask 1</td>
    <td><img src="../images/mask2.gif"/>Mask 2</td>
    <td><img src="../images/mask3.gif"/>Mask 3</td>
  </tr>
  <tr>
    <td><img src="../images/bike1.gif"/>Bike 1</td>
    <td><img src="../images/bike2.gif"/>Bike 2</td>
    <td><img src="../images/bike3.gif"/>Bike 3</td>
  </tr>
  <tr>
    <td><img src="../images/drill1.gif"/>Drill 1</td>
    <td><img src="../images/drill2.gif"/>Drill 2</td>
    <td><img src="../images/drill3.gif"/>Drill 3</td>
    </tr>
  <tr>
    <td><img src="../images/mask1.gif"/>Mask 1</td>
    <td><img src="../images/mask2.gif"/>Mask 2</td>
    <td><img src="../images/mask3.gif"/>Mask 3</td>
  </tr>
  <tr>
    <th colspan="3">Tesco Items</th>
  </tr>
  <tr>
    <td><img src="../images/wipes.gif"/>Tesco Cleaning wipes</td>
    <td><img src="../images/tuna.gif"/>Tesco Tuna</td>
    <td><img src="../images/sweetcorn.gif"/>Tesco Sweet Corn</td>
  </tr>
</table>
</p>
