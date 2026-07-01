package com.waleria.techcase.useCase;

import com.waleria.techcase.repository.AccountEntity;
import com.waleria.techcase.repository.AccountRepository;
import com.waleria.techcase.service.AccountService;
import com.waleria.techcase.web.dto.AccountDTORequest;
import com.waleria.techcase.web.dto.AccountDTOResponse;
import com.waleria.techcase.web.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    @Override
    public AccountDTOResponse createAccount(AccountDTORequest dto) {
        AccountEntity account = new AccountEntity();
        account.setDocumentNumber(dto.getDocumentNumber());

        AccountEntity savedAccount = accountRepository.save(account);

        return new AccountDTOResponse(
                savedAccount.getAccountId(),
                savedAccount.getDocumentNumber()
        );
    }

    @Override
    public AccountDTOResponse retrieveAccount(Long accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return new AccountDTOResponse(
                account.getAccountId(),
                account.getDocumentNumber()
        );
    }
}
