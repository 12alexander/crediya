package co.com.bancolombia.r2dbc.loantype;

import co.com.bancolombia.r2dbc.loantype.data.LoanTypeData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanTypeR2dbcRepository extends ReactiveCrudRepository<LoanTypeData, String> {
}