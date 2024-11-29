package app.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс {@code Atmosphere} предоставляет методы для вычисления свойств атмосферы
 * в зависимости от высоты над уровнем моря.
 */
public class Atmosphere {

    // Константы
    public static final double GAS_CONSTANT = 287.05; // Дж/(кг·К)
    public static final double ADIABATIC_INDEX = 1.4;
    public static final double STANDARD_GRAVITY = 9.80665; // м/с²

    /**
     * Внутренний класс {@code Layer} представляет слой атмосферы.
     */
    public static class Layer {
        double hBase;
        double hTop;
        double tBase;
        double pBase;
        double lapseRate;

        public Layer(double hBase, double hTop, double tBase, double lapseRate) {
            this.hBase = hBase;
            this.hTop = hTop;
            this.tBase = tBase;
            this.lapseRate = lapseRate;
            this.pBase = 0;
        }
    }

    private static final List<Layer> atmosphereLayers = new ArrayList<>();

    static {
        initializeLayers();
    }

    /**
     * Инициализирует слои атмосферы с их базовыми значениями.
     */
    private static void initializeLayers() {
        // Определение слоев атмосферы
        atmosphereLayers.add(new Layer(0, 11000, 288.15, -0.0065));
        atmosphereLayers.add(new Layer(11000, 20000, 216.65, 0));
        atmosphereLayers.add(new Layer(20000, 32000, 216.65, 0.001));
        atmosphereLayers.add(new Layer(32000, 47000, 228.65, 0.0028));
        atmosphereLayers.add(new Layer(47000, 51000, 270.65, 0));
        atmosphereLayers.add(new Layer(51000, 71000, 270.65, -0.0028));
        atmosphereLayers.add(new Layer(71000, 84852, 214.65, -0.002));

        // Начальные условия на уровне моря
        Layer firstLayer = atmosphereLayers.get(0);
        firstLayer.pBase = 101325; // Па

        // Расчет базовых значений для каждого слоя
        for (int i = 1; i < atmosphereLayers.size(); i++) {
            Layer prevLayer = atmosphereLayers.get(i - 1);
            Layer layer = atmosphereLayers.get(i);

            double h_b = layer.hBase;
            double L = prevLayer.lapseRate;
            double T_b = prevLayer.tBase;
            double p_b = prevLayer.pBase;
            double h_diff = h_b - prevLayer.hBase;

            double T, p;

            if (L == 0) {
                T = T_b;
                p = p_b * Math.exp(-STANDARD_GRAVITY * h_diff / (GAS_CONSTANT * T));
            } else {
                T = T_b + L * h_diff;
                p = p_b * Math.pow(T / T_b, -STANDARD_GRAVITY / (L * GAS_CONSTANT));
            }

            layer.tBase = T;
            layer.pBase = p;
        }
    }

    /**
     * Возвращает свойства атмосферы на заданной высоте.
     *
     * @param altitude высота в метрах
     * @return свойства атмосферы
     */
    public static AtmosphereProperties getAtmosphericProperties(double altitude) {
        if (altitude < 0) altitude = 0;

        Layer layer = null;
        for (Layer l : atmosphereLayers) {
            if (altitude >= l.hBase && altitude < l.hTop) {
                layer = l;
                break;
            }
        }

        if (layer == null) {
            // Высота выше верхнего слоя
            layer = atmosphereLayers.get(atmosphereLayers.size() - 1);
        }

        double h_b = layer.hBase;
        double T_b = layer.tBase;
        double p_b = layer.pBase;
        double L = layer.lapseRate;
        double delta_h = altitude - h_b;

        double T, p, rho, soundSpeed;

        if (L == 0) {
            T = T_b;
            p = p_b * Math.exp(-STANDARD_GRAVITY * delta_h / (GAS_CONSTANT * T_b));
        } else {
            T = T_b + L * delta_h;
            p = p_b * Math.pow(T / T_b, -STANDARD_GRAVITY / (L * GAS_CONSTANT));
        }

        rho = p / (GAS_CONSTANT * T);
        soundSpeed = Math.sqrt(ADIABATIC_INDEX * GAS_CONSTANT * T);

        return new AtmosphereProperties(T, p, rho, soundSpeed);
    }

    /**
     * Класс {@code AtmosphereProperties} содержит свойства атмосферы на определенной высоте.
     */
    public static class AtmosphereProperties {
        public double temperature;
        public double pressure;
        public double density;
        public double soundSpeed;

        public AtmosphereProperties(double temperature, double pressure, double density, double soundSpeed) {
            this.temperature = temperature;
            this.pressure = pressure;
            this.density = density;
            this.soundSpeed = soundSpeed;
        }
    }
}