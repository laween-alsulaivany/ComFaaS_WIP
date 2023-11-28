import math
import os
import random
from datetime import datetime

NUM_ITERATIONS = 3600000

currentDatetime = datetime.now()
dateTimeString = currentDatetime.strftime("%Y%m%d_%H%M%S")

outputDir = os.path.abspath("Output") 
os.makedirs(outputDir, exist_ok=True)

filePath = os.path.join(outputDir, f"sqrtResult_{dateTimeString}.txt")

with open(filePath, "w") as writer:
    for i in range(NUM_ITERATIONS):
        a = random.randrange(20000, 40001)
        writer.write(f"{math.sqrt(a)}\n")

print("Done")
