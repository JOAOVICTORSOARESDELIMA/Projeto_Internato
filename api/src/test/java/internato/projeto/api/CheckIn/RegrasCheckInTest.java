package internato.projeto.api.CheckIn;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class RegrasCheckInTest {

    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 5, 10, 7, 0);

    // Um ponto em João Pessoa
    private static final double LAT = -7.1366;
    private static final double LON = -34.8442;

    @Test
    void aceitaCheckInDentroDaTolerancia() {
        assertTrue(RegrasCheckIn.dentroDoHorario(INICIO, INICIO));
        assertTrue(RegrasCheckIn.dentroDoHorario(INICIO, INICIO.minusMinutes(15)));
        assertTrue(RegrasCheckIn.dentroDoHorario(INICIO, INICIO.plusMinutes(15)));
    }

    @Test
    void recusaCheckInForaDaTolerancia() {
        assertFalse(RegrasCheckIn.dentroDoHorario(INICIO, INICIO.minusMinutes(16)));
        assertFalse(RegrasCheckIn.dentroDoHorario(INICIO, INICIO.plusMinutes(16)));
        assertFalse(RegrasCheckIn.dentroDoHorario(INICIO, INICIO.plusDays(1)));
    }

    @Test
    void distanciaZeroNoMesmoPonto() {
        assertEquals(0.0, RegrasCheckIn.distanciaEmMetros(LAT, LON, LAT, LON), 0.001);
    }

    @Test
    void distanciaConhecida() {
        // 0,001 grau de latitude equivale a cerca de 111 metros
        assertEquals(111.2, RegrasCheckIn.distanciaEmMetros(LAT, LON, LAT + 0.001, LON), 0.5);
    }

    @Test
    void localPertoEAceitoELongeERecusado() {
        assertTrue(RegrasCheckIn.dentroDoLocal(LAT, LON, LAT + 0.0005, LON));  // ~56 m
        assertFalse(RegrasCheckIn.dentroDoLocal(LAT, LON, LAT + 0.0010, LON)); // ~111 m
    }

    @Test
    void advertenciaQuandoPassaDeDozeHoras() {
        assertFalse(RegrasCheckIn.geraAdvertencia(INICIO, INICIO.plusHours(12)));
        assertTrue(RegrasCheckIn.geraAdvertencia(INICIO, INICIO.plusHours(12).plusMinutes(1)));
    }
}
