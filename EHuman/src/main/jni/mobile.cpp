#include <stdio.h>
#include <vector>
#include <opencv2/opencv.hpp>
#include <sys/time.h>

#include "mobilenet.hpp"

int main(int argc, char** argv)
{
	std::string fparam = "ncnn.proto";
	std::string fbin = "ncnn.bin";
	init_mobilenet(fparam, fbin);

		
	cv::Mat m = cv::imread(argv[1], 3);
	if (m.empty())
		fprintf(stderr, "cv::imread failed\n");


	detect_mobilenet(m, 0.5);
	
	cv::imwrite("mobile.jpg",m);

    return 0;
}