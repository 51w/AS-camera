#include <iostream>
#include <stdio.h>
#include <opencv2/opencv.hpp>
#include "runtracker.hpp"
#include <sys/time.h>

double get_current_time();

int main()
{
	cv::Mat frame;
	cv::Rect result;
	cv::Rect initrect = cv::Rect(100,100,150,150);

	char filename[30];
	char outname[30];
	for(int i=0; i<200; i++)
	{
		if(i<9){
			sprintf(filename, "./pics_201805071700/p_e0000%d.bmp", i+1);
			sprintf(outname, "./out/p_e0000%d.bmp", i+1);
		}else if(i<99){
			sprintf(filename, "./pics_201805071700/p_e000%d.bmp", i+1);
			sprintf(outname, "./out/p_e000%d.bmp", i+1);
		}else{
			sprintf(filename, "./pics_201805071700/p_e00%d.bmp", i+1);
			sprintf(outname, "./out/p_e00%d.bmp", i+1);
		}
		cv::Mat src = cv::imread(filename, 3);
		if(src.empty()) break;

		cvtColor(src,frame,CV_RGB2GRAY);
		
		if(i == 0)
		{	
			std::cout << "init" << std::endl;
			Tracker_init(initrect, frame);
			std::cout << "init successed!" << std::endl;
		}
		else{
			double time_start = get_current_time();
			result = Tracker_update(frame);
			double time_end = get_current_time();
			std::cout <<i << " Total time > " << (time_end-time_start) << "ms" << std::endl;
			
			cv::rectangle(src, result, cv::Scalar(0, 255, 255), 2, 8);
		}

		imwrite(outname, src);
	}
	
	return 0;
}

double get_current_time()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);

    return tv.tv_sec * 1000.0 + tv.tv_usec / 1000.0;
}