package org.bank.account.utilities.mappers;

import org.bank.account.dto.responses.GetTransactionResponse;
import org.bank.account.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public GetTransactionResponse toGetTransactionResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        return new GetTransactionResponse(
                transaction.getId(),
                transaction.getTransactionId(),
                transaction.getSenderId(),
                transaction.getReceiverId(),
                transaction.getAmount(),
                transaction.getCurrency().toString(),
                transaction.getTransactionType().toString(),
                transaction.getTransactionStatus().toString(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt()
        );
    }
}
