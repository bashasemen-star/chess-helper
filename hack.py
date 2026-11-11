import subprocess
import time
import os

print("Жди 5 секунд, открой игру...")
time.sleep(5)

# Делаем скриншот (пока просто для проверки)
subprocess.run(["su", "-c", "screencap -p /sdcard/screen.png"])

# КООРДИНАТЫ ДЛЯ e2-e4 (подгони под свой экран)
# Пример: start_x, start_y, end_x, end_y
coords = "540,1500,540,1200"

# Сохраняем координаты в файл
with open("/sdcard/move.txt", "w") as f:
    f.write(coords)

print(f"Координаты {coords} записаны в /sdcard/move.txt")
