package com.humanhelper.chesshelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    private SquareView squareView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    // ТОЧНЫЕ КООРДИНАТЫ (сдвинули на 10 пикселей влево и вверх)
    private int boardX = 57;
    private int boardY = 940;
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
        btnStart.setText("ЗАПУСТИТЬ ЧИТ (КВАДРАТЫ)");
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
        Toast.makeText(this, "Чит запущен. Жди квадраты...", Toast.LENGTH_LONG).show();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    doHack();
                    handler.postDelayed(this, 2000);
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
                    int e2x = boardX + 4 * cellSize;
                    int e2y = boardY + 6 * cellSize;
                    int e4x = boardX + 4 * cellSize;
                    int e4y = boardY + 4 * cellSize;
                    
                    runOnUiThread(() -> {
                        if (squareView != null) {
                            squareView.setSquares(e2x, e2y, e4x, e4y, cellSize);
                            squareView.setVisibility(View.VISIBLE);
                        }
                    });
                    
                    handler.postDelayed(() -> {
                        if (squareView != null) {
                            squareView.setVisibility(View.GONE);
                        }
                    }, 1500);
                }
                
            } catch (Exception e) {
                Log.e("Hack", "Error: " + e.getMessage());
            }
        }).start();
    }

    private void showOverlay() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        squareView = new SquareView(this);
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
            windowManager.addView(squareView, params);
            squareView.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public class SquareView extends View {
        private Paint paint;
        private Rect rect1, rect2;

        public SquareView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(Color.parseColor("#FFFF00"));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10f);
            paint.setAntiAlias(true);
            rect1 = new Rect();
            rect2 = new Rect();
        }

        public void setSquares(int x1, int y1, int x2, int y2, int size) {
            rect1.set(x1, y1, x1 + size, y1 + size);
            rect2.set(x2, y2, x2 + size, y2 + size);
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(rect1, paint);
            canvas.drawRect(rect2, paint);
        }
    }
}
