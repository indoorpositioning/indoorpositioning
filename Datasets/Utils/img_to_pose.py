import os
import random


if __name__ == "__main__":
    rgb_imgs = []
    depth_imgs = []
    data_points = []
    rgb_extension = ""
    depth_extension = ""

    rgb_dir = ".\\UTM_Hacklab_Images\\rgb"
    depth_dir = ".\\UTM_Hacklab_Images\\depth"
    pose_file = ".\\UTM_Hacklab_Images\\hacklab_poses_v2.1.txt"

    for filename in os.listdir(rgb_dir):
        if filename.endswith(".jpg") or filename.endswith(".png"):
            rgb_imgs.append(int(filename[:-4]))
            rgb_extension = filename[-4:]
        else:
            continue

    rgb_imgs.sort()

    for filename in os.listdir(depth_dir):
        if filename.endswith(".jpg") or filename.endswith(".png"):
            depth_imgs.append(int(filename[:-4]))
            depth_extension = filename[-4:]
        else:
            continue

    depth_imgs.sort()

    if depth_imgs == rgb_imgs:
        print("RGB and Depth Image names match\n")
    else:
        print("Unable to associate rgb and depth images. Exit\n")
        exit

    with open(pose_file, 'r') as poses:
        for img in rgb_imgs:
            img_dir = "UTM_Hacklab_Images/rgb"
            rgb_loc = img_dir + "/" + str(img) + rgb_extension

            img_dir = "UTM_Hacklab_Images/depth"
            depth_loc = img_dir + "/" + str(img) + depth_extension
            #Add dummy values to be consistent with other datasets= solutions like TUM 
            data_points.append(poses.readline()[:-1] + " 1000000000.000000 " + depth_loc + " 1000000000.000000 " + rgb_loc)
    
            
    random.shuffle(data_points)
    train_cnt = int(len(data_points) * 0.85)
    test_cnt = len(data_points) - train_cnt

    train_data = data_points[:train_cnt]
    test_data = data_points[train_cnt:]

    with open('associate_train.txt', 'w') as writer:
        for data in train_data:
            writer.write(data)
            writer.write("\n")

    with open('associate_test.txt', 'w') as writer:
        for data in test_data:
            writer.write(data)
            writer.write("\n")
