package com.ndzindo.registrationproject.appuser;

import com.ndzindo.registrationproject.email.EmailService;
import com.ndzindo.registrationproject.registration.token.ConfirmationToken;
import com.ndzindo.registrationproject.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String USER_NOT_FOUND_MSG = "User with email %s not found.";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailService emailService;


    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return appUserRepository.findByEmail(s)
                .orElseThrow(() -> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, s)));
    }

    @Transactional
    public String signUpUser(AppUser appUser){
        boolean userExists = appUserRepository.findByEmail(appUser.getEmail()).isPresent();

        if(userExists){

            AppUser userByEmail = appUserRepository.findByEmail(appUser.getEmail()).get();

            if(userByEmail.getEnabled()){
                throw new IllegalStateException("Email already taken.");
            }

            ConfirmationToken confirmationToken = confirmationTokenService.getTokenByUserId(userByEmail.getId()).get();

            if(confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())){
                confirmationToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
                confirmationTokenService.updateToken(confirmationToken);

                String link = "http://localhost:8080/api/v1/registration/confirm?token="+confirmationToken.getToken();

                emailService.send(appUser.getEmail(), appUser.getFirstName(), link);

                return "Updated expire time for token " + confirmationToken.getToken() + ". Check your email.";
            }

            return "Email not confirmed. Token still valid.";
        }

        String encodedPassword = bCryptPasswordEncoder.encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        appUserRepository.save(appUser);

        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(1),
                appUser
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        String link = "http://localhost:8080/api/v1/registration/confirm?token="+token;

        emailService.send(appUser.getEmail(), appUser.getFirstName(), link);

        return token;
    }

    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }
}
