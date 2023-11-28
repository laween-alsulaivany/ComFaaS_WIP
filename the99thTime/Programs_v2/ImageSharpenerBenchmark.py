from PIL import Image, ImageFilter
import os

def sharpen_images(input_folder, output_folder, kernel_size, kernel_data):
    input_files = [f for f in os.listdir(input_folder) if f.lower().endswith(('.jpg', '.png', '.jpeg', '.bmp'))]

    for input_file in input_files:
        try:
            input_path = os.path.join(input_folder, input_file)
            original_image = Image.open(input_path)

            kernel = ImageFilter.Kernel((kernel_size, kernel_size), kernel_data)
            sharpened_image = original_image.filter(kernel)

            output_file_name = "sharpened_" + input_file
            output_path = os.path.join(output_folder, output_file_name)

            sharpened_image.save(output_path, "JPEG")

            print(f"Sharpened: {input_file}")
        except Exception as e:
            print(f"Error sharpening: {input_file}")
            print(e)

if __name__ == "__main__":
    input_folder = "Input" 
    output_folder = "Output"

    kernel_data = [
        -0.5, -0.5, -0.5, -0.5, -0.5,
        -0.5, 0.0, 0.0, 0.0, -0.5,
        -0.5, 0.0, 9.0, 0.0, -0.5,
        -0.5, 0.0, 0.0, 0.0, -0.5,
        -0.5, -0.5, -0.5, -0.5, -0.5
    ]

    kernel_size = 5

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    sharpen_images(input_folder, output_folder, kernel_size, kernel_data)
