package com.axdborges.voz.budgeting.infrastructure.persistence;

import com.axdborges.voz.budgeting.domain.Category;
import com.axdborges.voz.budgeting.domain.Transaction;
import com.axdborges.voz.budgeting.domain.TransactionId;
import com.axdborges.voz.budgeting.infrastructure.persistence.repository.TransactionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// @DataJpaTest sobe só a fatia JPA (entidades + repositórios Spring Data) contra um banco H2
// embarcado (ver [testRuntimeOnly 'com.h2database:h2'] no build.gradle e application-test.properties)
// — não precisa do MySQL real do compose.yml pra validar o mapeamento entidade <-> domínio.
// Replace.NONE: usa o datasource H2 configurado em application-test.properties (com
// DB_CLOSE_DELAY=-1) em vez do datasource embutido substituto padrão do @DataJpaTest, que sem esse
// parâmetro fecha/recria o banco em memória entre a criação do schema e as queries dos testes.
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaTransactionRepositoryTest {

    @Autowired
    private TransactionJpaRepository transactionJpaRepository;

    private JpaTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaTransactionRepository(transactionJpaRepository);
    }

    @Test
    void shouldFindOnlyTransactionsFromTheGivenCategory() {
        Transaction mercado = new Transaction(TransactionId.generate(), "supermercado", Category.MERCADO,
                BigDecimal.TEN, LocalDateTime.now());
        Transaction transporte = new Transaction(TransactionId.generate(), "uber", Category.TRANSPORTE,
                BigDecimal.ONE, LocalDateTime.now());
        repository.save(mercado);
        repository.save(transporte);

        List<Transaction> found = repository.findByCategory(Category.MERCADO);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).id()).isEqualTo(mercado.id());
        assertThat(found.get(0).description()).isEqualTo("supermercado");
        assertThat(found.get(0).category()).isEqualTo(Category.MERCADO);
        assertThat(found.get(0).amount()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void shouldReturnAllSavedTransactions() {
        Transaction mercado = new Transaction(TransactionId.generate(), "supermercado", Category.MERCADO,
                BigDecimal.TEN, LocalDateTime.now());
        Transaction transporte = new Transaction(TransactionId.generate(), "uber", Category.TRANSPORTE,
                BigDecimal.ONE, LocalDateTime.now());
        repository.save(mercado);
        repository.save(transporte);

        List<Transaction> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(Transaction::id).containsExactlyInAnyOrder(mercado.id(), transporte.id());
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionMatchesTheCategory() {
        assertThat(repository.findByCategory(Category.LAZER)).isEmpty();
    }

    @Test
    void shouldPersistNullDescriptionAndAResolvedDate() {
        Transaction semDescricao = new Transaction(TransactionId.generate(), null, Category.OUTROS,
                BigDecimal.valueOf(25), LocalDateTime.of(2026, 7, 19, 10, 0));

        repository.save(semDescricao);

        List<Transaction> found = repository.findByCategory(Category.OUTROS);

        assertThat(found).hasSize(1);
        assertThat(found.get(0).description()).isNull();
        assertThat(found.get(0).occurredAt().toLocalDate()).isEqualTo(LocalDate.of(2026, 7, 19));
    }

    @Test
    void shouldFindAPersistedTransactionById() {
        Transaction mercado = new Transaction(TransactionId.generate(), "supermercado", Category.MERCADO,
                BigDecimal.TEN, LocalDateTime.now());
        repository.save(mercado);

        Optional<Transaction> found = repository.findById(mercado.id());

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo(mercado.id());
        assertThat(found.get().description()).isEqualTo("supermercado");
    }

    @Test
    void shouldReturnEmptyWhenFindingByAnUnknownId() {
        assertThat(repository.findById(TransactionId.generate())).isEmpty();
    }

    @Test
    void shouldDeleteAPersistedTransaction() {
        Transaction mercado = new Transaction(TransactionId.generate(), "supermercado", Category.MERCADO,
                BigDecimal.TEN, LocalDateTime.now());
        repository.save(mercado);

        repository.deleteById(mercado.id());

        assertThat(repository.findById(mercado.id())).isEmpty();
    }

    @Test
    void shouldOverwriteAnExistingTransactionWithTheSameIdAndPersistUpdatedAt() {
        TransactionId id = TransactionId.generate();
        Transaction original = new Transaction(id, "supermercado", Category.MERCADO, BigDecimal.TEN,
                LocalDateTime.of(2026, 7, 19, 10, 0));
        repository.save(original);

        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 21, 9, 0);
        Transaction edited = new Transaction(id, "supermercado atualizado", Category.MERCADO,
                BigDecimal.valueOf(20), original.occurredAt(), updatedAt);
        repository.save(edited);

        Optional<Transaction> found = repository.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().description()).isEqualTo("supermercado atualizado");
        assertThat(found.get().amount()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(found.get().updatedAt()).isEqualTo(updatedAt);
        assertThat(repository.findAll()).hasSize(1);
    }
}
