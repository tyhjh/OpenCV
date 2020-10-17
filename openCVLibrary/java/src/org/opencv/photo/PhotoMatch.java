package org.opencv.photo;

import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tyhj
 * @date 2020/10/17
 * @Description: java类作用描述
 */

public class PhotoMatch {

    private static final String TAG = "PhotoMatch";

    /**
     * 可信度设置
     */
    private static float confidence=0.8F;

    /**
     * 初始化OpenCV
     */
    public static void  init(){
        OpenCVLoader.initDebug();
    }

    public static Mat pathToMat(String filePath) {
        if (filePath == null) {
            return null;
        }
        return Imgcodecs.imread(filePath);
    }


    /**
     * 获取多个匹配
     * @param muban
     * @param src
     * @return
     */
    public synchronized static List<android.graphics.Rect> Matching(Mat muban, Mat src) {
        long startTime = SystemClock.uptimeMillis();
        List<android.graphics.Rect> rects = new ArrayList<android.graphics.Rect>();
        Mat clone = src.clone();
        if (muban == null || muban.empty() || src == null) {
            Log.e(TAG, "未找到资源");
            return rects;
        }

        int templatW, templatH, resultH, resultW;
        templatW = muban.width();
        templatH = muban.height();

        resultH = src.rows() - muban.rows() + 1;
        resultW = src.cols() - muban.cols() + 1;
        Mat result = new Mat(new Size(resultH, resultW), CvType.CV_32FC1);
        //是标准相关性系数匹配  值越大越匹配
        Imgproc.matchTemplate(clone, muban, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr;
        //查找匹配的结果
        mmr = Core.minMaxLoc(result);
        if (mmr.maxVal > confidence) {
            rects.add(new android.graphics.Rect((int) mmr.maxLoc.x, (int) mmr.maxLoc.y, (int) mmr.maxLoc.x + templatW, (int) mmr.maxLoc.y + templatH));
            Log.e(TAG, "匹配的值：" + mmr.maxVal + "   ------坐标：" + mmr.maxLoc.x + "," + mmr.maxLoc.y);
        }
        //一直获取相似的图像，一个个进行获取
        while (true) {
            //这里是判断相似程度的
            if (mmr.maxVal >confidence) {
                //再次获取匹配的图像位置
                mmr = getMaxLoc(clone, muban, templatW, templatH, mmr.maxLoc);
                if (mmr.maxVal > confidence) {
                    rects.add(new android.graphics.Rect((int) mmr.maxLoc.x, (int) mmr.maxLoc.y, (int) mmr.maxLoc.x + templatW, (int) mmr.maxLoc.y + templatH));
                    Log.e(TAG, "匹配的值：" + mmr.maxVal + "   ------坐标：" + mmr.maxLoc.x + "," + mmr.maxLoc.y);
                }
            } else {
                break;
            }
        }
        Log.i(TAG, "spend time " + (SystemClock.uptimeMillis() - startTime));
        return rects;
    }


    /**
     * 获取单个匹配
     * @param muban
     * @param src
     * @return
     */
    public synchronized static Rect MatchingSingle(Mat muban, Mat src){
        long startTime = SystemClock.uptimeMillis();
        Rect rect=null;
        Mat clone = src.clone();
        if (muban == null || muban.empty() || src == null) {
            Log.e(TAG, "未找到资源");
            return rect;
        }

        int templatW, templatH, resultH, resultW;
        templatW = muban.width();
        templatH = muban.height();

        resultH = src.rows() - muban.rows() + 1;
        resultW = src.cols() - muban.cols() + 1;
        Mat result = new Mat(new Size(resultH, resultW), CvType.CV_32FC1);
        //是标准相关性系数匹配  值越大越匹配
        Imgproc.matchTemplate(clone, muban, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr;
        //查找匹配的结果
        mmr = Core.minMaxLoc(result);
        Log.e(TAG, "匹配的值：" + mmr.maxVal + "   ------坐标：" + mmr.maxLoc.x + "," + mmr.maxLoc.y);
        if (mmr.maxVal > confidence) {
            rect=new android.graphics.Rect((int) mmr.maxLoc.x, (int) mmr.maxLoc.y, (int) mmr.maxLoc.x + templatW, (int) mmr.maxLoc.y + templatH);
        }
        Log.i(TAG, "spend time " + (SystemClock.uptimeMillis() - startTime));
        return rect;
    }



    private synchronized static Core.MinMaxLocResult getMaxLoc(Mat clone, Mat result, int templatW, int templatH, Point maxLoc) {
        int startY, startX, endY, endX;

        //计算大矩形的坐标
        startY = (int) maxLoc.y;
        startX = (int) maxLoc.x;

        //计算大矩形的的坐标
        endY = (int) maxLoc.y + templatH;
        endX = (int) maxLoc.x + templatW;

        //将大矩形内部 赋值为最大值 使得 以后找的最小值 不会位于该区域  避免找到重叠的目标
        //通道数 (灰度: 1, RGB: 3, etc.)
        int ch = clone.channels();
        for (int i = startX; i < endX; i++) {
            for (int j = startY; j < endY; j++) {
                //读取像素值，并存储在double数组中
                double[] data = clone.get(j, i);
                //RGB值或灰度值
                for (int k = 0; k < ch; k++) {
                    //对每个像素值（灰度值或RGB通道值，取值0~255）进行处理
                    data[k] = 255;
                }
                //把处理后的像素值写回到Mat
                clone.put(j, i, data);
            }
        }

        int resultH = clone.rows() - result.rows() + 1;
        int resultW = clone.cols() - result.cols() + 1;
        Mat result2 = new Mat(new Size(resultH, resultW), CvType.CV_32FC1);
        //是标准相关性系数匹配  值越大越匹配
        Imgproc.matchTemplate(clone, result, result2, Imgproc.TM_CCOEFF_NORMED);
        //查找result中的最大值 及其所在坐标
        return Core.minMaxLoc(result2);
    }

    /**
     *
     * @param confidence
     */
    public static void setConfidence(float confidence) {
        PhotoMatch.confidence = confidence;
    }
}
