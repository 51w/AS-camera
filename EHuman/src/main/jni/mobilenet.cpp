#include <string>
#include "net.h"
#include "mobilenet.hpp"

ncnn::Net mobilenet;

int init_mobilenet(std::string fparam, std::string fbin)
{
	mobilenet.load_param(fparam.c_str());
    mobilenet.load_model(fbin.c_str());
	
	return 0;
}

int detect_mobilenet(cv::Mat& raw_img, float show_threshold)
{
    int img_h = raw_img.size().height;
    int img_w = raw_img.size().width;
	//mobilenet.load_param("ncnn.proto");
    //mobilenet.load_model("ncnn.bin");
	
    int input_size = 300;
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
    std::vector<Object> objects;
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

    for(int i = 0;i<objects.size();++i)
    {
        Object object = objects.at(i);
        if(object.prob > show_threshold && object.class_id == 15)
		//if(object.prob > show_threshold)
        {
            cv::rectangle(raw_img, object.rec, cv::Scalar(0, 0, 255));
        }
    }
	
    return 0;
}