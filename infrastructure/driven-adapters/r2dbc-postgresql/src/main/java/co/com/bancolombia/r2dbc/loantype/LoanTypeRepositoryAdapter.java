package co.com.bancolombia.r2dbc.loantype;

import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.r2dbc.loantype.mapper.LoanTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LoanTypeRepositoryAdapter implements LoanTypeRepository {

    private final LoanTypeR2dbcRepository repository;
    private final LoanTypeMapper loanTypeMapper;

    @Override
    public Mono<LoanType> findById(String id) {
        log.debug("Buscando tipo de préstamo con ID: {}", id);
        return repository.findById(id)
                .map(loanTypeMapper::toDomain)
                .doOnNext(loanType -> log.debug("Tipo de préstamo encontrado: {} - {}", loanType.getId(), loanType.getName()));
    }

    @Override
    public Flux<LoanType> findAll() {
        log.debug("Obteniendo todos los tipos de préstamo");
        return repository.findAll()
                .map(loanTypeMapper::toDomain)
                .doOnNext(loanType -> log.debug("Tipo de préstamo: {} - {}", loanType.getId(), loanType.getName()));
    }

    @Override
    public Mono<Boolean> existsById(String id) {
        log.debug("Verificando si existe tipo de préstamo con ID: {}", id);
        return repository.existsById(id)
                .doOnNext(exists -> log.debug("Existe tipo de préstamo con ID {}: {}", id, exists));
    }
}