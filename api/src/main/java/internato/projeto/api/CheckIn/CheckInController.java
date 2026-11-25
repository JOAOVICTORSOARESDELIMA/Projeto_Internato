package internato.projeto.api.CheckIn;

import internato.projeto.api.Plantao.Plantao;
import internato.projeto.api.PlantaoAluno.PlantaoAluno;
import internato.projeto.api.PlantaoAluno.PlantaoAlunoRepository;
import internato.projeto.api.PlantaoAluno.StatusPlantaoAluno;
import internato.projeto.api.infra.RegraDeNegocioException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("checkin")
public class CheckInController {

    private final CheckInRepository repository;
    private final PlantaoAlunoRepository plantaoAlunoRepository;

    public CheckInController(CheckInRepository repository, PlantaoAlunoRepository plantaoAlunoRepository) {
        this.repository = repository;
        this.plantaoAlunoRepository = plantaoAlunoRepository;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<DadosDetalhamentoCheckIn> realizarCheckIn(@RequestBody @Valid DadosCadastroCheckIn dados) {
        if (repository.existsByPlantaoAlunoId(dados.idPlantaoAluno())) {
            throw new RegraDeNegocioException("Check-in já registrado.");
        }

        PlantaoAluno pa = plantaoAlunoRepository.getReferenceById(dados.idPlantaoAluno());
        Plantao plantao = pa.getPlantao();

        var inicio = plantao.getData().atTime(plantao.getHoraInicio());
        if (!RegrasCheckIn.dentroDoHorario(inicio, dados.dataHoraCheckIn())) {
            throw new RegraDeNegocioException("Check-in fora do horário permitido (tolerância de "
                    + RegrasCheckIn.TOLERANCIA_MINUTOS + " minutos).");
        }

        var local = plantao.getLocalEspecialidade().getLocal();
        if (!RegrasCheckIn.dentroDoLocal(dados.latitude(), dados.longitude(),
                local.getLatitude(), local.getLongitude())) {
            throw new RegraDeNegocioException("Você está fora do local autorizado (limite de "
                    + (int) RegrasCheckIn.LIMITE_METROS + " metros).");
        }

        CheckIn check = new CheckIn(dados);
        repository.save(check);
        pa.atualizarStatus(StatusPlantaoAluno.PRESENTE);

        return ResponseEntity.ok(new DadosDetalhamentoCheckIn(check));
    }

    @PostMapping("/checkout")
    @Transactional
    public ResponseEntity<DadosDetalhamentoCheckIn> realizarCheckOut(@RequestBody @Valid DadosCheckOut dados) {
        CheckIn check = repository.getReferenceById(dados.idCheckIn());

        if (check.getDataHoraCheckOut() != null) {
            throw new RegraDeNegocioException("Check-out já realizado.");
        }
        if (dados.dataHoraCheckOut().isBefore(check.getDataHoraCheckIn())) {
            throw new RegraDeNegocioException("Check-out não pode ser antes do check-in.");
        }

        // Plantão longo demais indica que o aluno esqueceu o check-out: gera advertência.
        boolean advertencia = RegrasCheckIn.geraAdvertencia(check.getDataHoraCheckIn(), dados.dataHoraCheckOut());
        check.registrarCheckOut(dados.dataHoraCheckOut(), advertencia);
        return ResponseEntity.ok(new DadosDetalhamentoCheckIn(check));
    }
}
