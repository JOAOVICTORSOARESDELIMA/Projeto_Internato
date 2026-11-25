package internato.projeto.api.infra;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converte exceções em respostas HTTP padronizadas, em vez do erro 500 genérico.
 *
 * <ul>
 *   <li>Registro inexistente: 404</li>
 *   <li>Campos inválidos: 400 com a lista de campos</li>
 *   <li>JSON malformado: 400</li>
 *   <li>Regra de negócio: 400 com a mensagem</li>
 *   <li>Violação de integridade (ex.: e-mail duplicado): 409</li>
 * </ul>
 */
@RestControllerAdvice
public class TratadorDeErros {

    public record Erro(String mensagem) {
    }

    public record ErroDeCampo(String campo, String mensagem) {
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Erro> naoEncontrado() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Erro("Registro não encontrado."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErroDeCampo>> camposInvalidos(MethodArgumentNotValidException ex) {
        var erros = ex.getFieldErrors().stream()
                .map(e -> new ErroDeCampo(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Erro> jsonInvalido() {
        return ResponseEntity.badRequest().body(new Erro("Corpo da requisição inválido ou mal formatado."));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<Erro> regraDeNegocio(RegraDeNegocioException ex) {
        return ResponseEntity.badRequest().body(new Erro(ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Erro> conflito() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new Erro("Operação viola uma restrição do banco (registro duplicado ou em uso)."));
    }
}
