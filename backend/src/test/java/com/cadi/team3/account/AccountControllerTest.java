package com.cadi.team3.account;

import com.cadi.team3.domain.Account;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import javax.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {

    private Account createUser() {
        Account account = Account.builder()
                .nickname("testUser")
                .email("testUser@naver.com")
                .password("123456789")
                .role(Role.ROLE_USER)
                .emailVerified(false)
                .build();

        return account;
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JavaMailSender javaMailSender;

    @Autowired
    AccountRepository accountRepository;

    @BeforeEach
    public void tearDown() {
        accountRepository.deleteAll();
    }


    @DisplayName("가입 Valid 확인 #1 - form 값이 잘 못 된 경우")
    @Test
    public void sign_up_form_test1() throws Exception {
        // given
        SignupDto signupDto = SignupDto.builder()
                .nickname("vividswan2131232131232131242145**")
                .password("1234")
                .email("vividswan")
                .build();

        // when
        final ResultActions perform = mockMvc.perform(post("/sign-up")
                .content(objectMapper.writeValueAsString(signupDto))
                .contentType(MediaType.APPLICATION_JSON));

        //then
        perform.andExpect(status().is4xxClientError());
    }

    @DisplayName("가입 Valid 확인 #2 - 올바른 form 값인 경우")
    @Test
    public void sign_up_form_test2() throws Exception {
        //given
        SignupDto signupDto = SignupDto.builder()
                .nickname("vividswan")
                .password("1234567890")
                .email("vividswan@naver.com")
                .build();

        //when
        final ResultActions perform = mockMvc.perform(post("/sign-up")
                .content(objectMapper.writeValueAsString(signupDto))
                .contentType(MediaType.APPLICATION_JSON));

        //then
        perform.andExpect(status().is2xxSuccessful());
    }

    @Transactional
    @DisplayName("가입 Valid 확인 #3 - 중복 된 회원 이메일인 경우")
    @Test
    public void sign_up_form_test3() throws Exception {
        //given
        Account testUser = createUser();
        accountRepository.save(testUser);

        SignupDto signupDto = SignupDto.builder()
                .nickname(testUser.getNickname())
                .password(testUser.getPassword())
                .email(testUser.getEmail())
                .build();


        //when
        final ResultActions perform = mockMvc.perform(post("/sign-up")
                .content(objectMapper.writeValueAsString(signupDto))
                .contentType(MediaType.APPLICATION_JSON));


        //then
        perform.andExpect(status().is4xxClientError());
    }

}