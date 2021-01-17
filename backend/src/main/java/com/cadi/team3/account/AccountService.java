package com.cadi.team3.account;

import com.cadi.team3.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


@RequiredArgsConstructor
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;

    private List<String> validCheck(SignupDto signupDto, Errors errors){
        List<String> errorList = new ArrayList<>();

        Iterator<ObjectError> it = errors.getAllErrors().iterator();
        while(it.hasNext()){
            errorList.add(it.next().getDefaultMessage());
        }

        if(accountRepository.existsByEmail(signupDto.getEmail())){
            errorList.add("해당 이메일은 중복됩니다.");
        }
        if(accountRepository.existsByNickname(signupDto.getNickname())){
            errorList.add("해당 닉네임은 중복됩니다.");
        }
        return errorList;
    }

    @Transactional
    public ResponseEntity<?> signUp(SignupDto dto, Errors errors){

        List<String> errorList = validCheck(dto,errors);

        if(!errorList.isEmpty()) return new ResponseEntity<>(errorList,HttpStatus.CONFLICT);

        Account account = Account.builder()
                .nickname(dto.getNickname())
                .email(dto.getEmail())
                .password(dto.getPassword()) // TODO password Encoding
                .role(Role.ROLE_USER)
                .emailVerified(false)
                .build();

        accountRepository.save(account);

        account.generateEmailCheckToken();
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(account.getEmail());
        simpleMailMessage.setSubject("이메일 인증 확인");
        simpleMailMessage.setText("/check-email-token?email="+account.getEmail()+"&token="+account.getEmailCheckToken());

        javaMailSender.send(simpleMailMessage);


        return new ResponseEntity<>(account,HttpStatus.CREATED);

    }

}
