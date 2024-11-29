package app.model;

/**
 * Класс {@code Physics} содержит физические расчеты, необходимые для симуляции.
 */
public class Physics {

    // Константы
    public static final double EARTH_RADIUS = 6_371_000; // м
    public static final double STANDARD_GRAVITY = 9.80665; // м/с²
    private static final double BASE_DRAG_COEFFICIENT = 0.5;

    /**
     * Вычисляет ускорение свободного падения на заданной высоте.
     *
     * @param altitude высота над уровнем моря в метрах
     * @return ускорение свободного падения в м/с²
     */
    public static double calculateGravity(double altitude) {
        return STANDARD_GRAVITY * Math.pow(EARTH_RADIUS / (EARTH_RADIUS + altitude), 2);
    }

    /**
     * Вычисляет коэффициент сопротивления в зависимости от числа Маха.
     *
     * @param machNumber число Маха
     * @return коэффициент сопротивления
     */
    public static double calculateDragCoefficient(double machNumber) {
        double dragCoefficient;
        if (machNumber < 0.8) {
            dragCoefficient = BASE_DRAG_COEFFICIENT;
        } else if (machNumber < 1.0) {
            dragCoefficient = BASE_DRAG_COEFFICIENT + 0.5 * (machNumber - 0.8) / 0.2;
        } else if (machNumber < 1.2) {
            dragCoefficient = 1.0;
        } else {
            dragCoefficient = 0.8;
        }
        return dragCoefficient;
    }
}