import time
from flask import Flask, render_template, request, redirect, url_for, jsonify
import os
import subprocess
import threading
import shutil
import sys

app = Flask(__name__)

# Configurations for the GUI
UPLOAD_FOLDER = "frontend/uploads"
LOG_FOLDER = "frontend/logs"
OUTPUT_FOLDER = "frontend/outputs"

frontend_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.join(frontend_dir, "..")
uploads_dir = os.path.join(project_root, "frontend", "uploads")
server_programs_dir = os.path.join(project_root, "server", "Programs")
server_output_dir = os.path.join(project_root, "server", "output")
client_output_dir = os.path.join(project_root, "client", "output")

jar_path = os.path.join(project_root, "ComFaaS.jar")

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
                    script_path = os.path.join(script_dir, "runserver.bat")
                    command = ["cmd.exe", "/c", script_path]
                else:
                    # Unix/WSL: use the sh file.
                    script_path = os.path.join(script_dir, "runserver.sh")
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
        # Determine project root and prepare directories.
        # frontend_dir = os.path.dirname(os.path.abspath(__file__))
        # project_root = os.path.join(frontend_dir, "..")
        # uploads_dir = os.path.join(project_root, "frontend", "uploads")
        # server_programs_dir = os.path.join(project_root, "server", "Programs")
        # server_output_dir = os.path.join(project_root, "server", "output")
        # client_output_dir = os.path.join(project_root, "client", "output")

        # Define source and destination paths.
        source_file = os.path.join(uploads_dir, file)
        dest_file = os.path.join(server_programs_dir, file)

        # Instead of copying, use the jar command to upload the file.
        # jar_path = os.path.join(project_root, "ComFaaS.jar")
        upload_command = [
            "java", "-jar", jar_path,
            "client", "upload",
            "-server", "127.0.0.1",
            "-p", "12353",
            "-l", "server",
            "-local", source_file,
            "-remote", dest_file
        ]
        # check if server is running, if not, start the server
        for attempt in range(3):
            if app.server_started == False:
                try:
                    start_server_once()
                except:
                    attempt += 1
                if attempt == 3:
                    print("Server did not start")
                    break
            elif (app.server_started == True):
                # ✅ Execute the upload command.
                subprocess.run(upload_command, check=True)

        # Run the program, capturing terminal output.
        ext = os.path.splitext(file)[1].lower()
        if ext == ".py":
            execution_command = [sys.executable, dest_file]
        else:
            execution_command = [dest_file]

        process = subprocess.Popen(
            execution_command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
        stdout, _ = process.communicate()

        # Save output into server/output, filename based on the application name.
        output_filename = f"{os.path.splitext(file)[0]}.txt"
        output_file_path = os.path.join(server_output_dir, output_filename)
        with open(output_file_path, "w") as out:
            out.write(stdout)

        # Copy the output file from server/output to client/output.
        client_output_file = os.path.join(OUTPUT_FOLDER, output_filename)
        shutil.copy(output_file_path, client_output_file)

        # Log execution details (optional)
        log_file = os.path.join(LOG_FOLDER, f"{file}.log")
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
