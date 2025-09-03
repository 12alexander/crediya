package co.com.bancolombia.usecase.orders.interfaces;

import co.com.bancolombia.model.orders.Orders;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Interface defining the contract for Orders use cases.
 * Provides abstraction for loan request operations in the Crediya system.
 * 
 * @author Crediya Development Team
 */
public interface IOrdersUseCase {
    
    /**
     * Creates a new loan request after validating business rules.
     * 
     * @param documentId the customer's document ID (8-12 digits)
     * @param amount the requested loan amount
     * @param deadline the loan deadline in months (1-360)
     * @param emailAddress the customer's email address
     * @param loanTypeId the UUID of the loan type
     * @return a Mono containing the created loan request
     */
    Mono<Orders> createLoanRequest(String documentId, BigDecimal amount, Integer deadline, 
                                  String emailAddress, String loanTypeId);
    
    /**
     * Finds a loan request by its unique identifier.
     * 
     * @param orderId the UUID of the loan request
     * @return a Mono containing the loan request if found
     */
    Mono<Orders> findById(String orderId);
    
    /**
     * Finds a loan request by customer document ID.
     * 
     * @param documentId the customer's document ID
     * @return a Mono containing the loan request if found
     */
    Mono<Orders> findByDocumentId(String documentId);
    
    /**
     * Finds all loan requests for a given email address.
     * 
     * @param emailAddress the customer's email address
     * @return a Flux containing all loan requests for the email
     */
    Flux<Orders> findByEmailAddress(String emailAddress);
    
    /**
     * Checks if a loan request exists for the given document and status.
     * 
     * @param documentId the customer's document ID
     * @param statusId the status UUID to check
     * @return a Mono containing true if exists, false otherwise
     */
    Mono<Boolean> existsByDocumentIdAndStatus(String documentId, String statusId);
}