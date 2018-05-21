#include <stdio.h>
#include <vector>
#include <opencv2/opencv.hpp>

struct Object{
    cv::Rect rec;
    int class_id;
    float prob;
};

int init_mobilenet(std::string fparam, std::string fbin);

//int detect_mobilenet(cv::Mat& raw_img, std::vector<Object>& objects);

int detect_track(cv::Mat& raw_img, cv::Rect& objects);