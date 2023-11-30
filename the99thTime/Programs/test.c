#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <opencv2/opencv.hpp>

int main() {
    const char* inputFolderPath = "Input";
    const char* outputFolderPath = "Output";
    float kernelData[] = {
        -0.5f, -0.5f, -0.5f, -0.5f, -0.5f,
        -0.5f, -0.0f, -0.0f, -0.0f, -0.5f,
        -0.5f, -0.0f, 9.0f, -0.0f, -0.5f,
        -0.5f, -0.0f, -0.0f, -0.0f, -0.5f,
        -0.5f, -0.5f, -0.5f, -0.5f, -0.5f
    };
    int kernelSize = 5;

    cv::String inputFolderPattern = inputFolderPath;
    inputFolderPattern += "/*.*";

    std::vector<cv::String> inputFiles;
    cv::glob(inputFolderPattern, inputFiles, false);

    for (const auto& inputFile : inputFiles) {
        cv::Mat originalImage = cv::imread(inputFile);

        if (originalImage.empty()) {
            fprintf(stderr, "Error reading image: %s\n", inputFile.c_str());
            continue;
        }

        cv::Mat kernel = cv::Mat(kernelSize, kernelSize, CV_32F, kernelData);
        cv::filter2D(originalImage, originalImage, originalImage.depth(), kernel);

        std::string outputFilePath = outputFolderPath + "/sharpened" + inputFile.substr(inputFile.find_last_of('/'));
        cv::imwrite(outputFilePath, originalImage);
    }

    return 0;
}
