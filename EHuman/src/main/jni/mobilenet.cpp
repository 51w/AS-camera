#include <string>
#include <iostream>
#include "net.h"
#include "mobilenet.hpp"
#include "runtracker.hpp"
#include "lbp.h"

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

cv::Rect CutRct(cv::Rect in)
{
	cv::Rect out;

	out.x = in.x + in.width/4;
	out.y = in.y + in.height/8;
	out.width = in.width*1/2;
	out.height = in.height*3/4;

	return out;
}

////////////////////////
static bool flag_detect = 1;
cv::Mat gray;
bool g_bRedetection = false;
////////////////////////

int Num_notfound = 0;
int Num_notcomp = 0;

bool select_init = true;
cv::Mat pre_frame;

int detect_track(cv::Mat& raw_img, cv::Rect& result)
{
	if(flag_detect)
	{
		std::vector<Object> objects;
		detect_mobilenet(raw_img, objects);

        if(select_init)
        {
            for(int i = 0;i<objects.size();++i)
            {
                Object object = objects.at(i);
                if(object.prob > 0.5 && object.class_id == 15)
                {
                    cv::Rect rect = CutRct(object.rec);

                    result = rect;
                    cvtColor(raw_img, gray,CV_RGB2GRAY);
                    Tracker_init(result, gray);

                    pre_frame = gray(result);

                    flag_detect = 0;
                    select_init = false;
                    break;
                }
            }
		}
		else
		{
		    double minDist = DBL_MAX;
            int RedetectPos = -1;

            for(int i = 0;i<objects.size();++i)
            {
                Object object = objects.at(i);
                if(object.prob > 0.5 && object.class_id == 15)
                {
                    cv::Rect rect = CutRct(object.rec);

                    cvtColor(raw_img, gray,CV_RGB2GRAY);
                    cv::Mat src_gray = gray(rect);

                    double dist = cmpLBP(pre_frame, src_gray);

                    if (dist < minDist)
                    {
                        RedetectPos = i;
                        minDist = dist;
                    }
                }
            }

            if ((minDist < 0.4) && (minDist < DBL_MAX) && RedetectPos != -1)
            {
                Object object = objects.at(RedetectPos);

                cv::Rect rect = CutRct(object.rec);
                result = rect;
                cvtColor(raw_img, gray,CV_RGB2GRAY);
                Tracker_init(result, gray);

                //pre_frame = gray(result);
                flag_detect = 0;
            }
            else
            {
                Num_notcomp++;
                if(Num_notfound >= 60)
                {
                    Num_notfound = 0;
                    //select_init = true;
                }
            }
		}
	}
	else
	{
		cvtColor(raw_img, gray,CV_RGB2GRAY);
        result = Tracker_update(gray, g_bRedetection);

        if(g_bRedetection)
        {
            Num_notfound++;
            if(Num_notfound >= 50)
            {
                Num_notfound = 0;
                flag_detect = 1;
            }
        }
	}

	return 0;
}