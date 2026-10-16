package com.example.demo.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class CycleCalculator {

    private CycleCalculator() {
        // Clase de utilidades: no se instancia
    }

    // ==================== Duración de un ciclo ====================

    /**
     * Duración en días de un ciclo, contando el día de inicio y el de fin.
     * Ejemplo: 01/01 → 05/01 = 5 días.
     */
    public static int calcularDuracionCiclo(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            return 0;
        }
        long dias = ChronoUnit.DAYS.between(inicio, fin);
        return (int) dias + 1;
    }

    // ==================== Diferencia entre dos fechas ====================

    /**
     * Días entre dos fechas (sin contar el día de inicio).
     * Ejemplo: 01/01 → 29/01 = 28 días.
     * Útil para calcular intervalos entre inicios de ciclos consecutivos.
     */
    public static long diasEntre(LocalDate desde, LocalDate hasta) {
        return ChronoUnit.DAYS.between(desde, hasta);
    }

    // ==================== Promedio ====================

    /**
     * Promedio aritmético de una lista de valores.
     * Devuelve 0 si la lista está vacía.
     */
    public static double calcularPromedio(List<Long> valores) {
        if (valores == null || valores.isEmpty()) {
            return 0.0;
        }
        long suma = 0;
        for (Long v : valores) {
            suma += v;
        }
        return (double) suma / valores.size();
    }

    // ==================== Desviación estándar ====================

    /**
     * Desviación estándar poblacional de una lista de valores.
     * Fórmula: √( Σ(xi - media)² / n )
     * Devuelve 0 si la lista tiene menos de 2 valores.
     */
    public static double calcularDesviacion(List<Long> valores, double media) {
        if (valores == null || valores.size() < 2) {
            return 0.0;
        }
        double sumaCuadrados = 0.0;
        for (Long v : valores) {
            double diff = v - media;
            sumaCuadrados += diff * diff;
        }
        double varianza = sumaCuadrados / valores.size();
        return Math.sqrt(varianza);
    }

    // ==================== Redondeo ====================

    /**
     * Redondea un double al entero más cercano.
     * Ejemplo: 4.67 → 5, 4.4 → 4, 4.5 → 5.
     */
    public static int redondear(double valor) {
        return (int) Math.round(valor);
    }

    // ==================== Desviación mínima ====================

    /**
     * Si la desviación es menor a 1, devuelve 1.
     * Sirve para que el rango de predicción tenga al menos ±1 día.
     */
    public static double desviacionConMinimo(double desviacion) {
        return Math.max(desviacion, 1.0);
    }
}