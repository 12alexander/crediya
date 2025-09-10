package co.com.bancolombia.api.util;

import co.com.bancolombia.api.dto.response.PendingRequestResponseDTO;
import co.com.bancolombia.api.dto.response.UserReportResponseDTO;
import co.com.bancolombia.model.orders.PendingRequest;
import org.springframework.stereotype.Component;

/**
 * Builder pattern implementation for creating enriched reports.
 * Follows Single Responsibility Principle by focusing only on report building.
 */
@Component
public class ReportBuilder {

    public PendingRequestResponseDTO buildFrom(PendingRequest pendingRequest, UserReportResponseDTO user) {
        validateInputs(pendingRequest, user);

        return PendingRequestResponseDTO.builder()
                .amount(pendingRequest.getAmount())
                .deadline(pendingRequest.getDeadline())
                .emailAddress(pendingRequest.getEmailAddress())
                .name(buildFullName(user))
                .loanType(pendingRequest.getLoanType())
                .interestRate(pendingRequest.getInterestRate())
                .baseSalary(user.getBaseSalary())
                .build();
    }

    private void validateInputs(PendingRequest pendingRequest, UserReportResponseDTO user) {
        if (pendingRequest == null) {
            throw new IllegalArgumentException("PendingRequest cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("UserReportResponseDTO cannot be null");
        }
    }

    private String buildFullName(UserReportResponseDTO user) {
        String name = user.getName() != null ? user.getName().trim() : "";
        String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
        
        if (name.isEmpty() && lastName.isEmpty()) {
            return "N/A";
        }
        
        return (name + " " + lastName).trim();
    }
}