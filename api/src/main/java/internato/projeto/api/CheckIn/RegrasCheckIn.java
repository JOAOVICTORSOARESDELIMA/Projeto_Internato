package internato.projeto.api.CheckIn;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Regras de check-in e check-out, sem dependência do Spring ou do banco,
 * para poderem ser testadas isoladamente.
 */
public final class RegrasCheckIn {

    /** Tolerância, para mais ou para menos, em relação ao início do plantão. */
    public static final long TOLERANCIA_MINUTOS = 15;

    /** Distância máxima entre o aluno e o local do plantão. */
    public static final double LIMITE_METROS = 100.0;

    /** Acima disso, considera que o aluno esqueceu o check-out. */
    public static final long LIMITE_HORAS_PLANTAO = 12;

    private static final double RAIO_TERRA_METROS = 6_371_000;

    private RegrasCheckIn() {
    }

    public static boolean dentroDoHorario(LocalDateTime inicioPlantao, LocalDateTime checkIn) {
        long minutos = Math.abs(Duration.between(inicioPlantao, checkIn).toMinutes());
        return minutos <= TOLERANCIA_MINUTOS;
    }

    public static boolean dentroDoLocal(double latAluno, double lonAluno, double latLocal, double lonLocal) {
        return distanciaEmMetros(latAluno, lonAluno, latLocal, lonLocal) <= LIMITE_METROS;
    }

    public static boolean geraAdvertencia(LocalDateTime checkIn, LocalDateTime checkOut) {
        return Duration.between(checkIn, checkOut).toMinutes() > LIMITE_HORAS_PLANTAO * 60;
    }

    /** Distância entre dois pontos GPS pela fórmula de Haversine, em metros. */
    public static double distanciaEmMetros(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RAIO_TERRA_METROS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
