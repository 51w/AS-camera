#include <stdio.h>
#include <vector>
#include <opencv2/opencv.hpp>
#include <sys/time.h>
#include "mobilenet.hpp"

double get_current_time()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);

    return tv.tv_sec * 1000.0 + tv.tv_usec / 1000.0;
}

int main(int argc, char** argv)
{
	std::string fparam = "ncnn.proto";
	std::string fbin = "ncnn.bin";
	init_mobilenet(fparam, fbin);

		
	//cv::Mat m = cv::imread(argv[1], 3);
	//if (m.empty())
	//	fprintf(stderr, "cv::imread failed\n");
	cv::Mat m;
	cv::VideoCapture camera("11.mp4");
	while(1)
	{
		camera >> m;
		if (m.empty()) break;
		
		// std::vector<Object> objects;
		// detect_mobilenet(m, objects);
		
		
		// for(int i = 0;i<objects.size();++i)
		// {
			// Object object = objects.at(i);
			// if(object.prob > 0.5 && object.class_id == 15)
			// {
				// cv::rectangle(m, object.rec, cv::Scalar(0, 0, 255));
			
			// fprintf(stderr, "prob: %f   %d %d %d %d\n", object.prob, object.rec.x, object.rec.y, object.rec.width, object.rec.height);
			// }
		// }
		
		cv::Rect result;
		
		double time_start = get_current_time();
		detect_track(m, result);
		double time_end = get_current_time();
		std::cout << " Total time > " << (time_end-time_start) << "ms" << std::endl;
			
		cv::rectangle(m, result, cv::Scalar(0, 0, 255));
		
		cv::imshow("zz",m);
		cv::waitKey(30);
	}
	//cv::imwrite("mobile.jpg",m);

    return 0;
}

/*
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
        
		fprintf(stderr, "prob: %f   %d %d %d %d\n", object.prob, object.rec.x, object.rec.y, object.rec.width, object.rec.height);
		}
	}
	
	cv::imwrite("mobile.jpg",m);

    return 0;
}
*/