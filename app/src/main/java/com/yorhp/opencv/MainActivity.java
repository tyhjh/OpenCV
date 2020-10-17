package com.yorhp.opencv;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.core.Mat;
import org.opencv.photo.PhotoMatch;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * @author tyhj
 */
public class MainActivity extends AppCompatActivity {

    private ImageView ivBg;

    private static final String TEMPLE_SAVE_PATH = Environment.getExternalStorageDirectory() + "/OPenCVTest/temple.png";

    private static final String SRC_SAVE_PATH = Environment.getExternalStorageDirectory() + "/OPenCVTest/src.jpg";

    /**
     * 模板资源名字
     */
    private static final String TEMPLE_ASSET_NAME="temple_1080x2220.jpg";

    /**
     * 识别资源名字
     */
    private static final String SRC_ASSET_NAME="src_1080x1920.jpg";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (lacksPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        ivBg = findViewById(R.id.ivBg);
        long startTime = SystemClock.uptimeMillis();
        PhotoMatch.init();
        PhotoMatch.setConfidence(0.9F);
        Log.i("MainActivity", "init time is " + (SystemClock.uptimeMillis() - startTime));
        //复制文件
        copyFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bitmap bitmap = BitmapFactory.decodeFile(SRC_SAVE_PATH);
        ivBg.setImageBitmap(bitmap);
        findViewById(R.id.btnHello).setOnClickListener(v -> {
            Mat temple = PhotoMatch.pathToMat(TEMPLE_SAVE_PATH);
            Mat src = PhotoMatch.pathToMat(SRC_SAVE_PATH);
            List<Rect> rects = PhotoMatch.Matching(temple, src);
            if (rects == null) {
                return;
            }
            Bitmap bitmap1 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            for (Rect rect : rects) {
                FileUtil.drawRect(bitmap1, rect, Color.argb(150, 180, 52, 217));
            }
            ivBg.setImageBitmap(bitmap1);
        });
    }

    /**
     * 判断是否缺少权限
     *
     * @param permission
     * @return
     */
    private boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            copyFile();
        } else {
            finish();
        }

    }


    /**
     * 复制文件到本地
     */
    private void copyFile() {
        FileUtil.copyFileFromAssets(this, TEMPLE_ASSET_NAME, TEMPLE_SAVE_PATH);
        FileUtil.copyFileFromAssets(this,SRC_ASSET_NAME, SRC_SAVE_PATH);
    }

}