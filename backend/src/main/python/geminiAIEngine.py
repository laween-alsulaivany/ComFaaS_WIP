import google.generativeai as genai
import os
import argparse
import re

def extract_numbers(input_string):
    """
    Extracts three numbers from a string in the format "{#, #, #}" and returns them as a list.

    Args:
        input_string: The string containing the numbers.

    Returns:
        A list containing the three extracted numbers as floats.
    """

    try:
        # Use regular expression to find the numbers
        pattern = r"{(\d+\.?\d*), (\d+\.?\d*), (\d+\.?\d*)}"
        match = re.search(pattern, input_string)

        if match:
            # Extract the numbers and convert them to floats
            num1 = float(match.group(1))
            num2 = float(match.group(2))
            num3 = float(match.group(3))
            return [num1, num2, num3]
        else:
            print("No numbers found in the string.")
            return None

    except ValueError:
        print("Invalid number format found.")
        return None

genai.configure(api_key=os.environ['GEMINI_API_KEY'])


parser = argparse.ArgumentParser(
        description="Run LLaMA3.2:1b with a prompt and a program file and return exactly three numbers."
    )
parser.add_argument("--program_file", type=str, required=True, help="Path to the program file to be provided to the model.")
args = parser.parse_args()

try:
    with open(args.program_file, 'r') as f:
        program_text = f.read()
except Exception as e:
    print(f"Error reading program file: {e}")
    exit(-1)

prompt = """
           Read the following program and determine its priorities for 
           load balancing. For load balancing, the program cares about 
           three things: CPU usage, memory usage, and file size. Based 
           on your analysis, please output exactly three numbers that 
           add up to 1. The first number is the weight for CPU, the 
           second for memory, and the third for file size.
           Note that no value can be absolute zero, lowest number is 0.001
           Output the numbers in the following format: {#, #, #}
         """
prompt = """
            Analyze the program and determine its load balancing priorities,
            considering CPU usage, memory usage, and file size. Output three 
            numbers (cannot be 0) representing the weights for each factor, 
            adding up to 1. Output format: {#, #, #}
             Note that no value can be absolute zero, lowest number is 0.001
        """

input_text = f"{prompt}\n{program_text}"

model = genai.GenerativeModel(model_name='gemini-2.0-flash')
response = model.generate_content(input_text)

numbers = extract_numbers(response.text)
print(numbers)
