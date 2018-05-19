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


	std::vector<Object> objects;
	detect_mobilenet(m, objects);
	
	
	for(int i = 0;i<objects.size();++i)
    {
        Object object = objects.at(i);
        if(object.prob > 0.5 && object.class_id == 15)
        {
            cv::rectangle(m, object.rec, cv::Scalar(0, 0, 255));
        }
    }
	
	cv::imwrite("mobile.jpg",m);

    return 0;
}