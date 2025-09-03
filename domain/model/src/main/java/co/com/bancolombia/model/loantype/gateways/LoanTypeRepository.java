package co.com.bancolombia.model.loantype.gateways;

import co.com.bancolombia.model.loantype.LoanType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanType> findById(String id);
    Flux<LoanType> findAll();
    Mono<Boolean> existsById(String id);
}