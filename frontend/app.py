import time
from flask import Flask, render_template, request, redirect, url_for, jsonify
import os
import subprocess
import threading

app = Flask(__name__)

# Configurations for the GUI
UPLOAD_FOLDER = "frontend/uploads"
LOG_FOLDER = "frontend/logs"
OUTPUT_FOLDER = "frontend/outputs"

os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(LOG_FOLDER, exist_ok=True)
os.makedirs(OUTPUT_FOLDER, exist_ok=True)

def get_unique_filename(filepath):
    # Helper: if filepath exists, add numerical postfix for uniqueness.
    base, ext = os.path.splitext(filepath)
    counter = 1
    unique_path = filepath
    while os.path.exists(unique_path):
        unique_path = f"{base}_{counter}{ext}"
        counter += 1
    return unique_path

@app.route("/start", methods=["POST"])
def start_execution():
    """Start the remote server (if needed) and then execute remote tasks concurrently."""
    selected_files = request.json.get("selected_files", [])
    if not selected_files:
        return jsonify({"status": "error", "message": "No files selected"}), 400

    # Global flag to ensure we start the server only once.
    if not hasattr(app, 'server_started'):
        app.server_started = False

    def start_server_once():
        if not app.server_started:
            try:
                # Compute the absolute path to the scripts folder.
                frontend_dir = os.path.dirname(os.path.abspath(__file__))
                script_dir = os.path.join(frontend_dir, "..", "scripts")
                if os.name == "nt":
                    # Windows: use the bat file.
                    script_path = os.path.join(script_dir, "runedgeserver.bat")
                    command = ["cmd.exe", "/c", script_path]
                else:
                    # Unix/WSL: use the sh file.
                    script_path = os.path.join(script_dir, "runedgeserver.sh")
                    command = ["sh", script_path]
                subprocess.Popen(command)
                app.server_started = True
            except Exception as e:
                print("Error starting server:", e)

    # Start the server thread (if not already running).
    server_thread = threading.Thread(target=start_server_once)
    server_thread.start()
    time.sleep(2)

    def run_remote_task(file):
        # Determine the language based on the file's extension.
        ext = os.path.splitext(file)[1].lower()
        if ext == ".py":
            lang = "python"
        elif ext == ".c":
            lang = "c"
        elif ext == ".java":
            lang = "java"
        else:
            lang = "unknown"

        # Compute the project root (assumes app.py is in frontend/).
        frontend_dir = os.path.dirname(os.path.abspath(__file__))
        project_root = os.path.join(frontend_dir, "..")
        # jar_path = os.path.join(project_root, "ComFaaS.jar")
        jar_path = "../ComFaaS.jar"

        # Build the remotetask command.
        command = [
            "java", "-jar", jar_path,
            "edge", "remotetask",
            "-server", "127.0.0.1", "-p", "12354",
            "-t", "cloud", "-np", "1",
            "-tn", file, "-lang", lang
        ]
        # Define output and log file paths.
        output_file_path = os.path.join(OUTPUT_FOLDER, f"{file}.txt")
        output_file_path = get_unique_filename(output_file_path)
        log_file = os.path.join(LOG_FOLDER, f"{file}.log")
        
        with open(log_file, "w") as log, open(output_file_path, "w") as out:
            log.write("Executing command: " + " ".join(command) + "\n")
            process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
            stdout, _ = process.communicate()
            out.write(stdout)
            log.write(stdout)
        with open(log_file, "a") as log:
            log.write("\nExecution completed")

    # For each selected file, start a thread to run the remotetask command.
    for file in selected_files:
        t = threading.Thread(target=run_remote_task, args=(file,))
        t.start()

    results = {file: "Started" for file in selected_files}
    return jsonify({"status": "success", "message": "Execution started", "results": results})


@app.route("/")
def index():
    """Render the main UI with uploaded files."""
    files = os.listdir(UPLOAD_FOLDER)
    return render_template("index.html", files=files)


@app.route("/upload", methods=["POST"])
def upload_file():
    """Handle file uploads."""
    if "file" not in request.files:
        return redirect(request.url)

    file = request.files["file"]
    if file.filename == "":
        return redirect(request.url)

    file.save(os.path.join(UPLOAD_FOLDER, file.filename))
    return redirect(url_for("index"))


@app.route("/run", methods=["POST"])
def run_application():
    """Render execution page with selected files."""
    selected_files = request.form.getlist("selected_files")

    if not selected_files:
        return "No files selected", 400

    # ✅ Ensure this renders
    return render_template("execution.html", selected_files=selected_files)


# @app.route("/start", methods=["POST"])
# def start_execution():
#     """Run the selected application concurrently in background threads."""
#     selected_files = request.json.get("selected_files", [])
#     if not selected_files:
#         return jsonify({"status": "error", "message": "No files selected"}), 400

#     def run_task(file):
#         # Construct full paths for file, log, and (optionally) output.
#         file_path = os.path.join(UPLOAD_FOLDER, file)
#         log_file = os.path.join(LOG_FOLDER, f"{file}.log")
#         # (Optional) Define an output file path if needed:
#         # output_file = os.path.join(OUTPUT_FOLDER, f"{file}_output.txt")

#         try:
#             with open(log_file, "w") as log:
#                 # Compute the absolute path of the scripts directory.
#                 # This assumes app.py is in frontend/ and scripts/ is at the project root.
#                 frontend_dir = os.path.dirname(os.path.abspath(__file__))
#                 script_dir = os.path.join(frontend_dir, "..", "scripts")
#                 if os.name == "nt":
#                     # Windows: use cmd.exe to run the bat file.
#                     script_path = os.path.join(script_dir, "runcloudserver.bat")
#                     command = ["cmd.exe", "/c", script_path, file_path]
#                 else:
#                     # Unix/WSL: use sh to run the shell script.
#                     script_path = os.path.join(script_dir, "runcloudserver.sh")
#                     command = ["sh", script_path, file_path]

#                 # Log the command for debugging purposes.
#                 log.write(f"Executing command: {command}\n")
#                 process = subprocess.Popen(command, stdout=log, stderr=log)
#                 process.wait()  # Wait for the process to complete.
#             # Append termination message to log after process finishes.
#             with open(log_file, "a") as log:
#                 log.write("\nExecution completed")
#             # Optionally, if your task produces an output file, you could move or copy it
#             # into the OUTPUT_FOLDER here.
#         except Exception as e:
#             with open(log_file, "a") as log:
#                 log.write(f"\nError: {str(e)}")

#     # Launch a background thread for each selected file.
#     for file in selected_files:
#         thread = threading.Thread(target=run_task, args=(file,))
#         thread.start()

#     # Immediately return a success response.
#     results = {file: "Started" for file in selected_files}
#     return jsonify({"status": "success", "message": "Execution started", "results": results})




@app.route("/logs/<filename>")
def get_logs(filename):
    """Fetch logs for a specific execution."""
    log_path = os.path.join(LOG_FOLDER, f"{filename}.log")
    if os.path.exists(log_path):
        with open(log_path, "r") as log_file:
            logs = log_file.readlines()
        return jsonify({"logs": logs})
    return jsonify({"logs": ["No logs available"]})


if __name__ == "__main__":
    app.run(debug=True)
