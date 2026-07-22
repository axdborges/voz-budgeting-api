package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.domain.TransactionNotFoundException;
import com.axdborges.voz.budgeting.infrastructure.http.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

// Tratamento de erros centralizado: normaliza tanto as exceções da aplicação (domínio/validação)
// quanto as exceções nativas do Spring (multipart, part ausente, tipo inválido, corpo mal formado)
// para o mesmo JSON (ErrorResponse). Sem isso, os erros do Spring caíam na página whitelabel (HTML),
// inconsistente com o resto da API. Vale para TODOS os controllers (@RestControllerAdvice global).
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Recurso não encontrado (ex.: transação por id inexistente).
    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TransactionNotFoundException exception,
                                                         HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    // Dado inválido lançado pela própria aplicação (validação dos requests, UUID mal formado).
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidArgument(IllegalArgumentException exception,
                                                               HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    // Parâmetro de tipo inválido (ex.: ?category=XPTO, valor fora do enum Category).
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                            HttpServletRequest request) {
        String message = "Valor inválido para o parâmetro '" + exception.getName() + "': " + exception.getValue();
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // Falta algo na requisição: o arquivo (part) ou um parâmetro obrigatório.
    @ExceptionHandler({MissingServletRequestPartException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleMissing(Exception exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    // Corpo (JSON) ausente ou mal formado — ex.: data em formato inválido, JSON quebrado.
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception,
                                                             HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou mal formado.", request);
    }

    // Content-Type não suportado pela rota (ex.: enviar application/json numa rota multipart).
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException exception,
                                                                   HttpServletRequest request) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getMessage(), request);
    }

    // Arquivo acima do limite de upload (default do Spring, 1MB por arquivo).
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSize(MaxUploadSizeExceededException exception,
                                                       HttpServletRequest request) {
        return build(HttpStatus.CONTENT_TOO_LARGE, "Arquivo enviado excede o tamanho máximo permitido.", request);
    }

    // Body multipart quebrado (ex.: Content-Type multipart sem boundary).
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipart(MultipartException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Não foi possível processar o multipart da requisição.", request);
    }

    // Rede de segurança: qualquer erro inesperado vira um 500 em JSON (não HTML), mas o stack trace
    // completo ainda vai pro log pra não perder a causa real durante o desenvolvimento.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        log.error("Erro não tratado ao processar {} {}", request.getMethod(), request.getRequestURI(), exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno ao processar a requisição.", request);
    }

    // Content-Type fixado em JSON de propósito: mesmo que o cliente mande "Accept: audio/mpeg" (como na
    // rota /voice-commands), a resposta de erro precisa sair como JSON legível — sem isso, o Spring não
    // acha um conversor pra serializar o ErrorResponse como áudio e devolve o corpo vazio.
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(status, message, request.getRequestURI());
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
