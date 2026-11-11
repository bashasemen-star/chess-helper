import subprocess
import time
import os

print("Жди 5 секунд, открой игру...")
time.sleep(5)

subprocess.run(["su", "-c", "screencap -p /sdcard/screen.png"])

# КООРДИНАТЫ ДЛЯ e2-e4
coords = "540,1500,540,1200"

# Сохраняем в папку приложения (куда у него есть доступ)
app_dir = "/data/data/com.humanhelper.chesshelper/files"
os.makedirs(app_dir, exist_ok=True)

with open(os.path.join(app_dir, "move.txt"), "w") as f:
    f.write(coords)

print(f"Координаты {coords} записаны в {app_dir}/move.txt")
