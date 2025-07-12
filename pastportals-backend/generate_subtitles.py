# generate_subtitles.py
import sys
import os
from datetime import datetime

text = sys.argv[1] if len(sys.argv) > 1 else "This is a test subtitle for your AI-generated video."

# Create output folder
os.makedirs("output", exist_ok=True)
timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
filename = f"output/subtitles_{timestamp}.srt"

# Simple 1-minute subtitle
srt_content = f"""1
00:00:00,000 --> 00:01:00,000
{text}
"""

with open(filename, "w") as f:
    f.write(srt_content)

print("Subtitle generated at:", filename)
