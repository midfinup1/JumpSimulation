package app.model;

public class Physics {

    // Константы
    public static final double EARTH_RADIUS = 6371000; // м
    public static final double STANDARD_GRAVITY = 9.80665; // м/с²
    public static final double BASE_DRAG_COEFFICIENT = 0.5;

    public static double calculateGravity(double altitude) {
        return STANDARD_GRAVITY * Math.pow(EARTH_RADIUS / (EARTH_RADIUS + altitude), 2);
    }

    public static double calculateDragCoefficient(double machNumber) {
        double dragCoefficient;
        if (machNumber < 0.8) {
            dragCoefficient = BASE_DRAG_COEFFICIENT;
        } else if (machNumber < 1.0) {
            dragCoefficient = BASE_DRAG_COEFFICIENT + 0.6 * (machNumber - 0.8) / 0.2;
        } else if (machNumber < 1.2) {
            dragCoefficient = BASE_DRAG_COEFFICIENT + 0.6 * (1.2 - machNumber) / 0.2;
        } else {
            dragCoefficient = BASE_DRAG_COEFFICIENT;
        }
        return dragCoefficient;
    }
}