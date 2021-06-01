THe original caffe implementation of PoseNet that was presented by Kendall et al can be found (here)[https://github.com/alexgkendall/caffe-posenet].
## Requirements
Matplotlib, Pytorch, Numpy
## How to train
In order to train this Pytorch based implementation of PoseNet:
1. Prepare your training data similar to the [Hacklab dataset](https://github.com/indoorpositioning/indoorpositioning/tree/main/Datasets/Hacklab). 
2. Rename training and testing pose text files to associate_train.txt and associate_test.txt respectively and add them to the same directory as the scripts in this folder.
3. Run posenet.py. We provide two loss functions based on [PoseNet: A Convolutional Network for Real-Time 6-DOF Camera Relocalization.](https://arxiv.org/abs/1505.07427) and [Modelling Uncertainty in Deep Learning for Camera Relocalization.](http://arxiv.org/abs/1509.05909)
4. Once done training, can also test using test_posenet.py. Which will produce a text file containing result information and post graphs using matplotlib
