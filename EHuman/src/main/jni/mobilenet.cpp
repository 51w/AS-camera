#include <string>
#include <iostream>
#include "net.h"
#include "mobilenet.hpp"
#include "runtracker.hpp"

ncnn::Net mobilenet;

int init_mobilenet(std::string fparam, std::string fbin)
{
	int result;
	result = mobilenet.load_param(fparam.c_str());
    result = mobilenet.load_model(fbin.c_str());
	
	return result;
}

int detect_mobilenet(cv::Mat& raw_img, std::vector<Object>& objects)
{
    int img_h = raw_img.size().height;
    int img_w = raw_img.size().width;
	//mobilenet.load_param("ncnn.proto");
    //mobilenet.load_model("ncnn.bin");
	
    int input_size = 300;   //PIXEL_BGR
    ncnn::Mat in = ncnn::Mat::from_pixels_resize(raw_img.data, ncnn::Mat::PIXEL_BGR, raw_img.cols, raw_img.rows, input_size, input_size);
	

    const float mean_vals[3] = {127.5f, 127.5f, 127.5f};
    const float norm_vals[3] = {1.0/127.5,1.0/127.5,1.0/127.5};
    in.substract_mean_normalize(mean_vals, norm_vals);

    ncnn::Mat out;
	
    ncnn::Extractor ex = mobilenet.create_extractor();
    ex.set_light_mode(true);
    ex.set_num_threads(4);
	ex.input("data", in);
	
    ex.extract("detection_out",out);
	

    //printf("%d %d %d\n", out.w, out.h, out.c);
    
    for (int iw=0;iw<out.h;iw++)
    {
        Object object;
        const float *values = out.row(iw);
        object.class_id = values[0];
        object.prob = values[1];
        object.rec.x = values[2] * img_w;
        object.rec.y = values[3] * img_h;
        object.rec.width = values[4] * img_w - object.rec.x;
        object.rec.height = values[5] * img_h - object.rec.y;
        objects.push_back(object);
    }
	
    return 0;
}


static bool flag_detect = 1;
cv::Mat gray;
#define TRACK_NUM 20
static int trackNum = TRACK_NUM;
static bool init_center = 1;
int center_y;
int center_x;

int detect_track(cv::Mat& raw_img, cv::Rect& result)
{
	if(init_center)
	{
		center_y = raw_img.size().height / 2;
		center_x = raw_img.size().width / 2;
		init_center = 0;
	}
	
	
	if(flag_detect)
	{
		std::cout << "--------flag_detect 1--------" << std::endl;
		
		std::vector<Object> objects;
		detect_mobilenet(raw_img, objects);
		
		int min_len = 0;
		for(int i = 0;i<objects.size();++i)
		{
			Object object = objects.at(i);
			if(object.prob > 0.5 && object.class_id == 15)
			{
				cv::Rect rect = object.rec;
				int rect_cenx = object.rec.x + object.rec.width;
				int rect_ceny = object.rec.y + object.rec.height;
				int len = abs(rect_cenx - center_x) + abs(rect_ceny - center_y);
				
				if(min_len < len)
				{
					min_len = len;
					result = rect;
									
					cvtColor(raw_img, gray,CV_RGB2GRAY);
					Tracker_init(result, gray);
					
					flag_detect = 0;
				}
			}
		}
		std::cout << "--------flag_detect 1 exit--------" << std::endl;
	}
	else
	{
		if(trackNum--)
		{
			cvtColor(raw_img, gray,CV_RGB2GRAY);
			result = Tracker_update(gray);
		}
		else
		{
			trackNum = TRACK_NUM;
			flag_detect = 1;
		}
	}
	
	int center_y = result.x + result.width/2;
	int center_x = result.y + result.height/2;
	
	return 0;
}