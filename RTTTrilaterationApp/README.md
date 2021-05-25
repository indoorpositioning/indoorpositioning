# RTT Trilateration App

### Note: This app can only run if you have an RTT compatible phone.

## How to use the app

### Floorplan
1. Make a floorplan bmp (use magicplan from the Google Play Store). Add it to the app/res/drawable directory. Change the bmp name in the MyCanvas class (look for TODO).
2. Reduce the image to fit on the app screen. Find the pixels per metre value and set the variable in the MainActivity class (look for TODO).

### Before
1. Turn on wifi and location. Make sure to allow location permissions for this app (if a popup for this does not appear, you will have to go to settings to allow location permissions for this app).
2. Go to developer options and disable "Wi-Fi scan throttling".

### Prepare access points
1. The top left corner of the floorplan will be the origin of the coordinate system. The coordinate system is a grid of squares 1m x 1m. Going down will be positive y and going right will be positive x.
2. On the "Add APs" screen press "Scan" to see a list of all access points and their details. APs can be filtered by typing in the beginning of the ssid or by selecting a frequency from the drop-down list.
3. Place the access points and go to the "Add APs" screen to enter their locations.

### Get a prediction
1. Once at least 3 access points have been placed and entered, go to the main screen and click "Scan" to get a prediction.

### Gather results
1. Once at least 3 access points have been placed and entered, on the main screen enter your actual current position and then click "Scan". This will get a prediction and add an entry.
2. Go to the "Accuracy" screen. Click the "Create" button to create a file. Next, click the "Download" button and select the file that you created. This will download the results to this file.

### Notes
- If phone is plugged in and you run the app, you can see print statements and errors from the app in android studio.
- Debugging screen displays the coordinates and distances array values.