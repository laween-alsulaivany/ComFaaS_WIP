import math
import os
from datetime import datetime

NUM_ITERATIONS = 10000 * 7

currentDatetime = datetime.now()
dateTimeString = currentDatetime.strftime("%Y%m%d_%H%M%S")

outputDir = os.path.abspath("Output") 
os.makedirs(outputDir, exist_ok=True)

filePath = os.path.join(outputDir, f"sineResult_{dateTimeString}.txt")

with open(filePath, "w") as writer:
    for i in range(NUM_ITERATIONS):
        for degrees in range(360):
            radians = math.radians(degrees)
            writer.write(f"{math.sin(radians)}\n")

print("Done")
