package com.humanhelper.chesshelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private ArrowView arrowView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV load failed");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            finish();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        Button btnStart = new Button(this);
        btnStart.setText("ЗАПУСТИТЬ АВТО-ЧИТ");
        btnStart.setOnClickListener(v -> {
            if (!isRunning) {
                startAutoHack();
            } else {
                Toast.makeText(this, "Уже запущено", Toast.LENGTH_SHORT).show();
            }
        });

        layout.addView(btnStart);
        setContentView(layout);
    }

    private void startAutoHack() {
        isRunning = true;
        showOverlay();
        Toast.makeText(this, "Чит запущен. Смотри на стрелку!", Toast.LENGTH_LONG).show();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    doHack();
                    handler.postDelayed(this, 3000); // Каждые 3 секунды
                }
            }
        }, 1000);
    }

    private void doHack() {
        new Thread(() -> {
            try {
                // 1. Делаем скриншот с рутом
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("screencap -p /sdcard/screen.png\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();

                // 2. Открываем скриншот (в реальности тут нужно OpenCV, но для теста просто двигаем стрелку)
                // Для теста: просто двигаем стрелку туда-сюда
                int newX = 540 + (int)(Math.random() * 100 - 50);
                int newY = 1200 + (int)(Math.random() * 100 - 50);
                
                runOnUiThread(() -> {
                    if (arrowView != null) {
                        arrowView.updatePosition(newX, newY, newX, newY - 200);
                    }
                });
                
            } catch (Exception e) {
                Log.e("Hack", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void showOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        arrowView = new ArrowView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        try {
            windowManager.addView(arrowView, params);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class ArrowView extends View {
        private Paint paint;
        private int startX = 540, startY = 1500, endX = 540, endY = 1200;

        public ArrowView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(Color.parseColor("#5C9CE6"));
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setStrokeWidth(25f);
            paint.setAntiAlias(true);
        }

        public void updatePosition(int sx, int sy, int ex, int ey) {
            startX = sx; startY = sy; endX = ex; endY = ey;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawLine(startX, startY, endX, endY, paint);
            paint.setStyle(Paint.Style.FILL);
            Path path = new Path();
            path.moveTo(endX, endY);
            path.lineTo(endX - 40, endY + 60);
            path.lineTo(endX + 40, endY + 60);
            path.close();
            canvas.drawPath(path, paint);
        }
    }
}
