#pragma once
#ifndef MYLBP_H
#define MYLBP_H

#include <opencv2/opencv.hpp>
#include <string>
#include <iostream>
using namespace cv;
using namespace std;


//------------------------------------------------------------------------------
// LBPH
//------------------------------------------------------------------------------

template <typename _Tp> static
void olbp_(InputArray _src, OutputArray _dst);

//------------------------------------------------------------------------------
// cv::elbp
//------------------------------------------------------------------------------
template <typename _Tp> static
inline void elbp_(InputArray _src, OutputArray _dst, int radius, int neighbors);

static void elbp(InputArray src, OutputArray dst, int radius, int neighbors);

static Mat histc_(const Mat& src, int minVal = 0, int maxVal = 255, bool normed = false);

static Mat histc(InputArray _src, int minVal, int maxVal, bool normed);


static Mat spatial_histogram(InputArray _src, int numPatterns,int grid_x, int grid_y, bool /*normed*/);

//------------------------------------------------------------------------------
// wrapper to cv::elbp (extended local binary patterns)
//------------------------------------------------------------------------------

static Mat elbp(InputArray src, int radius, int neighbors);

int getHopCount(uchar i);
void lbp59table(uchar* table);
void LBP(IplImage* src, IplImage* dst);

Mat getLBPHistogramint(Mat src, int radius = 1, int neighbors = 8, int grid_x = 8, int grid_y = 8, double threshold = DBL_MAX);

double cmpLBP(InputArray src1, InputArray src2, Size size = Size(200, 200), int radius = 1, int neighbors = 8, int grid_x = 8, int grid_y = 8, double threshold = DBL_MAX);

// 
// void lbphtest()
// {
// 	Mat src = imread("lbp/17.jpg");
// 	cvtColor(src, src, CV_RGB2GRAY);
// 	imshow("src", src);
// 
// 	Mat src2 = imread("lbp/17.jpg");
// 	cvtColor(src2, src2, CV_RGB2GRAY);
// 	//flip(src2, src2, 1);
// 	imshow("src2", src2);
// 
// 	Mat src3 = imread("lbp/17.jpg");
// 	cvtColor(src3, src3, CV_RGB2GRAY);
// 	imshow("src3", src3);
// 
// 	cout << cmpLBP(src, src2) << " " << cmpLBP(src, src3) << endl;
// 
// 	waitKey(0);
// 
// 	system("pause");
// }


#endif // !MYLBP_H