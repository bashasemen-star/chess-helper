package com.humanhelper.chesshelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
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

import java.io.DataOutputStream;
import java.io.File;

public class MainActivity extends Activity {
    private WindowManager windowManager;
    private GridView gridView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private int boardX = 62;
    private int boardY = 936;
    private int cellSize = 135;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        btnStart.setText("ЗАПУСТИТЬ АНАЛИЗ ЦВЕТА");
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
        Toast.makeText(this, "Анализ запущен. Смотри логи...", Toast.LENGTH_LONG).show();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    doHack();
                    handler.postDelayed(this, 3000);
                }
            }
        }, 1000);
    }

    private void doHack() {
        new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(process.getOutputStream());
                os.writeBytes("screencap -p /sdcard/screen.png\n");
                os.writeBytes("exit\n");
                os.flush();
                process.waitFor();

                File imgFile = new File("/sdcard/screen.png");
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    
                    // Анализируем цвет в центре каждой клетки
                    StringBuilder log = new StringBuilder("Анализ доски:\n");
                    for (int row = 0; row < 8; row++) {
                        for (int col = 0; col < 8; col++) {
                            int centerX = boardX + col * cellSize + cellSize / 2;
                            int centerY = boardY + row * cellSize + cellSize / 2;
                            
                            // Берем пиксель в центре
                            int pixel = bitmap.getPixel(centerX, centerY);
                            int r = (pixel >> 16) & 0xff;
                            int g = (pixel >> 8) & 0xff;
                            int b = pixel & 0xff;
                            
                            // Определяем цвет (упрощенно)
                            String colorType;
                            if (r > 200 && g > 200 && b > 200) {
                                colorType = "БЕЛАЯ";
                            } else if (r < 80 && g < 80 && b < 80) {
                                colorType = "ЧЕРНАЯ";
                            } else {
                                colorType = "ПУСТО";
                            }
                            
                            log.append(String.format("%c%d: %s (%d,%d,%d)\n", 
                                (char)('a' + col), 8 - row, colorType, r, g, b));
                        }
                    }
                    
                    Log.d("ColorAnalysis", log.toString());
                    bitmap.recycle();
                    
                    runOnUiThread(() -> Toast.makeText(this, "Анализ завершен. Смотри логи!", Toast.LENGTH_SHORT).show());
                }
                
            } catch (Exception e) {
                Log.e("Hack", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void showOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        gridView = new GridView(this);
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
            windowManager.addView(gridView, params);
            gridView.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class GridView extends View {
        public GridView(Context context) {
            super(context);
        }
    }
}
