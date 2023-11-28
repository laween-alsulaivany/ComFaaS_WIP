from PIL import Image
import os

def convert_to_grayscale(color_image):
    return color_image.convert("L")

def save_gray_image(gray_image, output_folder, file_name):
    output_file_path = os.path.join(output_folder, file_name)
    gray_image.save(output_file_path)

def main(input_folder, output_folder):
    image_files = [f for f in os.listdir(input_folder) if f.lower().endswith(('.jpg', '.png'))]

    for image_file in image_files:
        try:
            input_path = os.path.join(input_folder, image_file)
            color_image = Image.open(input_path)
            
            gray_image = convert_to_grayscale(color_image)

            output_file_name = os.path.splitext(image_file)[0] + "_gray" + os.path.splitext(image_file)[1]
            output_path = os.path.join(output_folder, output_file_name)
            
            save_gray_image(gray_image, output_folder, output_file_name)

            print(f"Image processing completed for: {image_file}")
        except Exception as e:
            print(f"Error processing image: {image_file}")
            print(e)

    print("All image processing completed.")

if __name__ == "__main__":
    input_folder = "Input" 
    output_folder = "Output"

    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    main(input_folder, output_folder)
