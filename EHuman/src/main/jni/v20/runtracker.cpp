#include <iostream>
#include <stdio.h>
#include <fstream>
#include <sstream>
#include <algorithm>
#include <opencv2/opencv.hpp>

#include "fdssttracker.hpp"

cv::Rect initRect, result; 
FDSSTTracker _tracker;
//

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

cv::Rect Tracker_update( cv::Mat image, bool &g_bRedetection)
{
	//std::cout << "start Tracker" << std::endl;
	bool tmp;
	result = _tracker.update(image, tmp);
	g_bRedetection = tmp;
	//std::cout << "end Tracker" << std::endl;

	return result;
}