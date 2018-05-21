#include <iostream>
#include <stdio.h>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <opencv2/opencv.hpp>

#include "fdssttracker.hpp"

cv::Rect initRect, result; 
FDSSTTracker _tracker;

int Tracker_init(cv::Rect roi, cv::Mat image)
{
	bool HOG = true;
	bool FIXEDWINDOW = false;
	bool MULTISCALE = true;
	bool SILENT = false;
	bool LAB = false;
	_tracker.FDSSTTracker00(HOG, FIXEDWINDOW, MULTISCALE, LAB);

	initRect = roi;
	_tracker.init(initRect, image);
	result = initRect;
				
	return 0;
}

cv::Rect Tracker_update( cv::Mat image)
{
	//std::cout << "start Tracker" << std::endl;
	result = _tracker.update(image);
	//std::cout << "end Tracker" << std::endl;

	return result;
}