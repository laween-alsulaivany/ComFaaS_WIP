from PIL import Image
import os

def resize_images(inputFolder, outputFolder, newWidth, newHeight):
    inputFiles = [f for f in os.listdir(inputFolder) if f.lower().endswith(('.jpg', '.png', '.jpeg', '.bmp'))]

    for input_file in inputFiles:
        try:
            input_path = os.path.join(inputFolder, input_file)
            original_image = Image.open(input_path)

            resized_image = original_image.resize((newWidth, newHeight))

            output_file_name = "resized_" + input_file
            output_path = os.path.join(outputFolder, output_file_name)

            resized_image.save(output_path, "JPEG")

            print(f"Resized: {input_file}")
        except Exception as e:
            print(f"Error resizing: {input_file}")
            print(e)

if __name__ == "__main__":
    inputFolder = "Input" 
    outputFolder = "Output"
    newWidth = 400
    newHeight = 400

    if not os.path.exists(outputFolder):
        os.makedirs(outputFolder)

    resize_images(inputFolder, outputFolder, newWidth, newHeight)
