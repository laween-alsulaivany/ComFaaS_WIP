import random
import datetime

NUM_POINTS = 36000000

def simulateMonteCarlo(num_points):
    pointsInsideCircle = 0

    for _ in range(num_points):
        x = random.random()
        y = random.random()

        if is_inside_circle(x, y):
            pointsInsideCircle += 1

    return pointsInsideCircle

def is_inside_circle(x, y):
    distance = (x ** 2 + y ** 2) ** 0.5
    return distance <= 1.0

def calculate_pi_approximation(pointsInsideCircle, num_points):
    return 4.0 * pointsInsideCircle / num_points

def main():
    current_datetime = datetime.datetime.now()
    date_time_string = current_datetime.strftime("%Y%m%d_%H%M%S")

    pi_approximation = calculate_pi_approximation(simulateMonteCarlo(NUM_POINTS), NUM_POINTS)

    with open(f"Output/montecarloResult_{date_time_string}.txt", "w") as file:
        file.write(str(pi_approximation))

if __name__ == "__main__":
    main()