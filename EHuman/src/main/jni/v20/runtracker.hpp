#include <opencv2/opencv.hpp>
#include <string>

int Tracker_init(cv::Rect roi, cv::Mat image);

cv::Rect Tracker_update( cv::Mat image, bool &g_bRedetection);

