# RTT Fingerprinting App

### Note: This app can only run if you have an RTT compatible phone.

## How to use the app

### Floorplan
1. Make a floorplan bmp (use magicplan from the Google Play Store). Add it to the app/res/drawable directory. Change the bmp name in the MyCanvas and AccuracyCanvas classes (look for TODO).
2. Reduce the image to fit on the app screen. Find the pixels per metre value and set the variable in the MainActivity class (look for TODO).

### Before
1. Turn on wifi and location. Make sure to allow location permissions for this app (if a popup for this does not appear, you will have to go to settings to allow location permissions for this app).
2. Go to developer options and disable "Wi-Fi scan throttling".
3. In the DataCollectionManager class set numAps to the number of APs you plan on using (look for TODO).

### Offline portion
1. On the "Data Collection" screen press "Scan" to see a list of all access points and their details.
2. Add the access points you want to use by entering the SSID and pressing "Add AP". Similarly, use "Remove AP" to remove an access point. APs can be filtered by typing in the beginning of the ssid or by selecting a frequency from the drop-down list.
3. Enter your current position x and y coordinates and press "Add Data Point" to collect the data at that position.
4. Go to the "Accuracy" screen. Click the "Create" button to create a file. Next, click the "Download Offline Data" button and select the file that you created. This will download the data to this file.

### Online portion
1. Train the model with the data collected during the offline portion and add the model to app "assests" directory.
2. On the main screen enter your actual current position and then click "Scan". This will get a prediction and add an entry.
3. Go to the "Accuracy" screen. Click the "Create" button to create a file. Next, click the "Download Online Data" button and select the file that you created. This will download the results to this file.

### Notes
- If phone is plugged in and you run the app, you can see print statements and errors from the app in android studio.
- The debugging screen displays a list of added APs.