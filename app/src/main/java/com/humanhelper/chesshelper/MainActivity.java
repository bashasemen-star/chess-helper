package com.humanhelper.chesshelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    // Координаты доски (для нарезки)
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
        btnStart.setText("НАРЕЗАТЬ ДОСКУ (ТЕСТ)");
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
        Toast.makeText(this, "Анализ запущен. Жди скриншот...", Toast.LENGTH_LONG).show();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    doHack();
                    handler.postDelayed(this, 5000); // Каждые 5 секунд
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

                File imgFile = new File("/sdcard/screen.png");
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    
                    // 2. Сохраняем скриншот в папку Download (туда легко попасть)
                    File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File screenshotFile = new File(downloadDir, "chess_screen.png");
                    try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    }
                    
                    // 3. Нарезаем доску на 64 клетки и сохраняем
                    File cellsDir = new File(downloadDir, "ChessCells");
                    if (!cellsDir.exists()) cellsDir.mkdirs();
                    
                    for (int row = 0; row < 8; row++) {
                        for (int col = 0; col < 8; col++) {
                            int x = boardX + col * cellSize;
                            int y = boardY + row * cellSize;
                            
                            // Проверка, чтобы не выйти за границы картинки
                            if (x + cellSize > bitmap.getWidth() || y + cellSize > bitmap.getHeight()) continue;
                            
                            Bitmap cell = Bitmap.createBitmap(bitmap, x, y, cellSize, cellSize);
                            File cellFile = new File(cellsDir, "cell_" + row + "_" + col + ".png");
                            
                            try (FileOutputStream fos = new FileOutputStream(cellFile)) {
                                cell.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            }
                        }
                    }
                    
                    bitmap.recycle();
                    runOnUiThread(() -> Toast.makeText(this, "Клетки сохранены в Download/ChessCells", Toast.LENGTH_LONG).show());
                }
                
            } catch (Exception e) {
                Log.e("Hack", "Error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
