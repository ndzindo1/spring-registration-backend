package com.ndzindo.registrationproject.registration;

import com.ndzindo.registrationproject.appuser.AppUser;
import com.ndzindo.registrationproject.appuser.AppUserRole;
import com.ndzindo.registrationproject.appuser.AppUserService;
import com.ndzindo.registrationproject.email.EmailSender;
import com.ndzindo.registrationproject.registration.token.ConfirmationToken;
import com.ndzindo.registrationproject.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class RegistrationService {

    private final AppUserService appUserService;
    private final EmailValidator emailValidator;
    private final ConfirmationTokenService confirmationTokenService;
    private final EmailSender emailSender;

    public String register(RegistrationRequest request) {
        boolean isValidEmail = emailValidator.test(request.getEmail());

        if(!isValidEmail){
            throw new IllegalStateException("Email is not valid!");
        }

        String token = appUserService.signUpUser(
                new AppUser(
                        request.getFirstName(),
                        request.getLastName(),
                        request.getEmail(),
                        request.getPassword(),
                        AppUserRole.USER
                )
        );

        return token;
    }

    @Transactional
    public String confirmToken(String token){
        ConfirmationToken confirmationToken = confirmationTokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found."));

        if(confirmationToken.getConfirmedAt() != null){
            throw new IllegalStateException("Email already confirmed.");
        }

        if(confirmationToken.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new IllegalStateException("Confirmation token expired.");
        }

        confirmationTokenService.setConfirmedAt(token);
        appUserService.enableAppUser(confirmationToken.getAppUser().getEmail());

        return "Email successfully confirmed";
    }


}
