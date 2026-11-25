package internato.projeto.api.infra;

/** Violação de uma regra de negócio. Vira resposta HTTP 400 com a mensagem. */
public class RegraDeNegocioException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
