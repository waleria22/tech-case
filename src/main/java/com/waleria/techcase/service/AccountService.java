package com.waleria.techcase.service;

import com.waleria.techcase.web.dto.AccountDTORequest;
import com.waleria.techcase.web.dto.AccountDTOResponse;


public interface AccountService {
    AccountDTOResponse createAccount(AccountDTORequest dto) ;
    AccountDTOResponse retrieveAccount(Long accountId);
}
