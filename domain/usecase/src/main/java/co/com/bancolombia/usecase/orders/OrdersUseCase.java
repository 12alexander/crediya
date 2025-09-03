package co.com.bancolombia.usecase.orders;

import co.com.bancolombia.model.loantype.LoanType;
import co.com.bancolombia.model.loantype.gateways.LoanTypeRepository;
import co.com.bancolombia.model.orders.Orders;
import co.com.bancolombia.model.orders.exceptions.InvalidLoanAmountException;
import co.com.bancolombia.model.orders.exceptions.LoanTypeNotFoundException;
import co.com.bancolombia.model.orders.exceptions.OrdersBusinessException;
import co.com.bancolombia.model.orders.gateways.OrdersRepository;
import co.com.bancolombia.usecase.orders.interfaces.IOrdersUseCase;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Use case implementation for loan request operations.
 * Implements business logic and orchestrates interactions between repositories.
 * 
 * @author Crediya Development Team
 */
@RequiredArgsConstructor
public class OrdersUseCase implements IOrdersUseCase {
    
    private final OrdersRepository ordersRepository;
    private final LoanTypeRepository loanTypeRepository;

    public Mono<Orders> createLoanRequest(String documentId, BigDecimal amount, Integer deadline, 
                                        String emailAddress, String loanTypeId) {
        
        return validateLoanType(loanTypeId)
                .flatMap(loanType -> validateLoanAmount(amount, loanType)
                        .then(getPendingStatusId())
                        .flatMap(pendingStatusId -> createAndValidateOrder(
                                documentId, amount, deadline, emailAddress, loanTypeId, pendingStatusId))
                        .flatMap(this::saveOrder));
    }

    private Mono<LoanType> validateLoanType(String loanTypeId) {
        return loanTypeRepository.findById(loanTypeId)
                .switchIfEmpty(Mono.error(new LoanTypeNotFoundException(loanTypeId)));
    }

    private Mono<Void> validateLoanAmount(BigDecimal amount, LoanType loanType) {
        return Mono.fromRunnable(() -> {
            if (!loanType.isAmountValid(amount)) {
                throw new InvalidLoanAmountException(amount, loanType.getMinimumAmount(), loanType.getMaximumAmount());
            }
        });
    }

    private Mono<String> getPendingStatusId() {
        return ordersRepository.findPendingStatusId()
                .switchIfEmpty(Mono.error(new OrdersBusinessException("PENDING_STATUS_NOT_FOUND", 
                                                                     "No se encontró el estado 'PENDING'")));
    }

    private Mono<Orders> createAndValidateOrder(String documentId, BigDecimal amount, Integer deadline,
                                              String emailAddress, String loanTypeId, String pendingStatusId) {
        return Mono.fromCallable(() -> {
            Orders order = Orders.createNew(documentId, amount, deadline, emailAddress, loanTypeId, pendingStatusId);
            order.validateForCreation();
            return order;
        });
    }

    private Mono<Orders> saveOrder(Orders order) {
        return ordersRepository.save(order);
    }

    public Mono<Orders> findById(String orderId) {
        return ordersRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new OrdersBusinessException("ORDER_NOT_FOUND", 
                                                                     "No se encontró la solicitud con ID: " + orderId)));
    }

    @Override
    public Mono<Orders> findByDocumentId(String documentId) {
        return ordersRepository.findByDocumentId(documentId)
                .switchIfEmpty(Mono.error(new OrdersBusinessException("ORDER_NOT_FOUND", 
                                                                     "No se encontró solicitud para el documento: " + documentId)));
    }

    @Override
    public Flux<Orders> findByEmailAddress(String emailAddress) {
        return ordersRepository.findByEmailAddress(emailAddress);
    }

    @Override
    public Mono<Boolean> existsByDocumentIdAndStatus(String documentId, String statusId) {
        return ordersRepository.existsByDocumentIdAndStatus(documentId, statusId);
    }
}