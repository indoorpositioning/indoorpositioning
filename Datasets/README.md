## This directory contains data used in our research
## Hacklab
Hacklab is a image to pose dataset that was generated using a Microsost Kinect to test PoseNet. We present depth and rgb images and the associaated poses for each image.
## TARoom
TARoom contains all Fingerprinting and Trilateration data used in this research. We present the trialteratioon results for RSSI, RTT and Marker-based positioning. We also provide fingerprinting data captured to train algorithms for fingerprinting solutions.
## Utils
- image_to_pose.py can be used to map the poses in the hacklab_poses.txt to rgb and depth images and create train, test splits. Edit line 52 to set custom split percentage.
