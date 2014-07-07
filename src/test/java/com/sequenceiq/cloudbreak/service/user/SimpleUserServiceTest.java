package com.sequenceiq.cloudbreak.service.user;

import com.sequenceiq.cloudbreak.domain.User;
import com.sequenceiq.cloudbreak.domain.UserStatus;
import com.sequenceiq.cloudbreak.repository.UserRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

public class SimpleUserServiceTest {

    private static final String DUMMY_EMAIL = "gipszjakab@myemail.com";
    private static final String DUMMY_PASSWORD = "test123";
    private static final String DUMMY_TOKEN = "KJ3fws";
    private static final String DUMMY_NEW_PASSWORD = "newPass";


    @InjectMocks
    @Spy
    private SimpleUserService underTest;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MimeMessagePreparator mimeMessagePreparator;

    private User user;

    @Before
    public void setUp() {
        underTest = new SimpleUserService();
        MockitoAnnotations.initMocks(this);
        user = createUser();
    }

    @Test
    public void testDisableUser() {
        // GIVEN
        given(userRepository.findByEmail(anyString())).willReturn(user);
        given(userRepository.save(user)).willReturn(user);
        doReturn(mimeMessagePreparator).when(underTest).prepareMessage(any(User.class), anyString(), anyString());
        doNothing().when(underTest).sendConfirmationEmail(mimeMessagePreparator);
        // WHEN
        underTest.disableUser(DUMMY_EMAIL);
        // THEN
        verify(underTest, times(1)).sendConfirmationEmail(mimeMessagePreparator);
        Assert.assertEquals(user.getStatus(), UserStatus.DISABLED);

    }

    @Test
    public void testDisableUserWhenNoUserFoundForEmailShouldNotSendEmail() {
        // GIVEN
        given(userRepository.findByEmail(anyString())).willReturn(null);
        // WHEN
        underTest.disableUser(DUMMY_EMAIL);
        // THEN
        verify(underTest, times(0)).sendConfirmationEmail(mimeMessagePreparator);

    }

    @Test
    public void resetPassword() {
        // GIVEN
        user.setStatus(UserStatus.DISABLED);
        given(userRepository.findUserByConfToken(DUMMY_TOKEN)).willReturn(user);
        given(passwordEncoder.encode(anyString())).willReturn(DUMMY_NEW_PASSWORD);
        given(userRepository.save(user)).willReturn(user);
        // WHEN
        underTest.resetPassword(DUMMY_TOKEN, DUMMY_NEW_PASSWORD);
        // THEN
        verify(userRepository, times(1)).save(user);

    }

    @Test
    public void resetPasswordWhenUserStatusIsNotDisabledShouldNotCreateNewPassword() {
        // GIVEN
        given(userRepository.findUserByConfToken(DUMMY_TOKEN)).willReturn(user);
        // WHEN
        underTest.resetPassword(DUMMY_TOKEN, DUMMY_NEW_PASSWORD);
        // THEN
        verify(passwordEncoder, times(0)).encode(anyString());

    }

    private User createUser() {
        User user = new User();
        user.setEmail(DUMMY_EMAIL);
        user.setPassword(DUMMY_PASSWORD);
        user.setConfToken(DUMMY_TOKEN);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
