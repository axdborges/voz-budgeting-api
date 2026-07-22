package com.axdborges.voz.budgeting.infrastructure.http;

import com.axdborges.voz.budgeting.application.DeleteTransactionUseCase;
import com.axdborges.voz.budgeting.application.FindTransactionByIdUseCase;
import com.axdborges.voz.budgeting.application.ListAllTransactionsUseCase;
import com.axdborges.voz.budgeting.application.ListTransactionsByCategoryUseCase;
import com.axdborges.voz.budgeting.application.PersistTransactionUseCase;
import com.axdborges.voz.budgeting.application.UpdateTransactionUseCase;
import com.axdborges.voz.budgeting.application.output.TransactionOutput;
import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.infrastructure.http.request.PersistTransactionRequest;
import com.axdborges.voz.budgeting.infrastructure.http.request.UpdateTransactionRequest;
import com.axdborges.voz.budgeting.infrastructure.http.response.TransactionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final ListAllTransactionsUseCase listAllTransactionsUseCase;
    private final ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase;
    private final PersistTransactionUseCase persistTransactionUseCase;
    private final FindTransactionByIdUseCase findTransactionByIdUseCase;
    private final UpdateTransactionUseCase updateTransactionUseCase;
    private final DeleteTransactionUseCase deleteTransactionUseCase;

    public TransactionController(ListAllTransactionsUseCase listAllTransactionsUseCase,
                                  ListTransactionsByCategoryUseCase listTransactionsByCategoryUseCase,
                                  PersistTransactionUseCase persistTransactionUseCase,
                                  FindTransactionByIdUseCase findTransactionByIdUseCase,
                                  UpdateTransactionUseCase updateTransactionUseCase,
                                  DeleteTransactionUseCase deleteTransactionUseCase) {
        this.listAllTransactionsUseCase = listAllTransactionsUseCase;
        this.listTransactionsByCategoryUseCase = listTransactionsByCategoryUseCase;
        this.persistTransactionUseCase = persistTransactionUseCase;
        this.findTransactionByIdUseCase = findTransactionByIdUseCase;
        this.updateTransactionUseCase = updateTransactionUseCase;
        this.deleteTransactionUseCase = deleteTransactionUseCase;
    }

    @GetMapping
    public List<TransactionResponse> list(@RequestParam(required = false) Category category) {
        List<TransactionOutput> outputs = category != null
                ? listTransactionsByCategoryUseCase.execute(category)
                : listAllTransactionsUseCase.execute();

        return outputs.stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TransactionResponse findById(@PathVariable String id) {
        return TransactionResponse.from(findTransactionByIdUseCase.execute(TransactionId.of(id)));
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody PersistTransactionRequest request) {
        TransactionOutput output = persistTransactionUseCase.execute(request.toInput());
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(output));
    }

    @PatchMapping("/{id}")
    public TransactionResponse update(@PathVariable String id, @RequestBody UpdateTransactionRequest request) {
        TransactionOutput output = updateTransactionUseCase.execute(TransactionId.of(id), request.toInput());
        return TransactionResponse.from(output);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteTransactionUseCase.execute(TransactionId.of(id));
        return ResponseEntity.noContent().build();
    }
}
