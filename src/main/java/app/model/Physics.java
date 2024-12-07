package app.model;

/**
 * Класс {@code Physics} содержит физические расчёты, необходимые для симуляции:
 * - Вычисление ускорения свободного падения на высоте
 * - Вычисление коэффициента сопротивления в зависимости от числа Маха с интерполяцией
 */
public class Physics {

    // Константы
    public static final double EARTH_RADIUS = 6_371_000; // м
    public static final double STANDARD_GRAVITY = 9.80665; // м/с²

    // Табличные точки для Cd(M)
    // Формат: {M, Cd}
    private static final double[][] CD_TABLE = {
            {0.0, 1.15},
            {0.8, 1.15},
            {1.0, 1.08},
            {1.2, 1.10},
            {5.0, 1.00}
    };

    /**
     * Вычисляет ускорение свободного падения на заданной высоте.
     *
     * g(h) = g0 * (R/(R+h))^2
     *
     * @param altitude высота над уровнем моря в метрах
     * @return ускорение свободного падения в м/с²
     */
    public static double calculateGravity(double altitude) {
        return STANDARD_GRAVITY * Math.pow(EARTH_RADIUS / (EARTH_RADIUS + altitude), 2);
    }

    /**
     * Вычисляет коэффициент сопротивления в зависимости от числа Маха, используя линейную интерполяцию
     * между заданными точками.
     *
     * @param machNumber число Маха
     * @return коэффициент сопротивления Cd
     */
    public static double calculateDragCoefficient(double machNumber) {
        if (machNumber <= CD_TABLE[0][0]) {
            return CD_TABLE[0][1];
        }

        for (int i = 0; i < CD_TABLE.length - 1; i++) {
            double M1 = CD_TABLE[i][0];
            double C1 = CD_TABLE[i][1];
            double M2 = CD_TABLE[i + 1][0];
            double C2 = CD_TABLE[i + 1][1];

            if (machNumber >= M1 && machNumber <= M2) {
                // Линейная интерполяция Cd между (M1, C1) и (M2, C2)
                double fraction = (machNumber - M1) / (M2 - M1);
                return C1 + fraction * (C2 - C1);
            }
        }

        // Если M больше максимального значения таблицы, берём крайнее значение
        return CD_TABLE[CD_TABLE.length - 1][1];
    }
}