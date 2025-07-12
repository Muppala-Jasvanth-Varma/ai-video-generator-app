# generate_image.py
from diffusers import StableDiffusionPipeline
import torch
import sys
from PIL import Image
import os
from datetime import datetime

# Get prompt from command-line
prompt = sys.argv[1] if len(sys.argv) > 1 else "A futuristic city with glowing lights"

# Load the pipeline
pipe = StableDiffusionPipeline.from_pretrained(
    "runwayml/stable-diffusion-v1-5", 
    torch_dtype=torch.float16 if torch.cuda.is_available() else torch.float32
)
pipe = pipe.to("cuda" if torch.cuda.is_available() else "cpu")

# Generate image
image = pipe(prompt).images[0]

# Save to output folder
os.makedirs("output", exist_ok=True)
timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
image.save(f"output/image_{timestamp}.png")
