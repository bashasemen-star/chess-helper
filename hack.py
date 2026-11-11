import subprocess
import time
import os

print("Жди 5 секунд, открой игру...")
time.sleep(5)

# Делаем скриншот (пока просто для проверки, что он работает)
subprocess.run(["su", "-c", "screencap -p /sdcard/screen.png"])

# ТЕСТОВЫЙ ХОД (пока просто e2-e4)
move = "e2e4"

# Сохраняем ход в файл, который прочитает APK
with open("/sdcard/move.txt", "w") as f:
    f.write(move)

print(f"Ход {move} записан в /sdcard/move.txt")
