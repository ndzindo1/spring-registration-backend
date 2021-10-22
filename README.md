# registration-backend-spring

Simple spring application for user registration and email confirmation using UUID generated token.
Also /login and /logout are provided from Spring Security.

User can send registration data on: http://localhost:8080/api/v1/registration

After successful registration user will recieve mail to confirm account.

Route for confirmation: http://localhost:8080/api/v1/registration/confirm?token=TOKEN_VALUE

e.g. http://localhost:8080/api/v1/registration/confirm?token=85961b58-95b7-4cf6-9b89-b637a8ebca19

Server port: 8080

DB used in application: PostgreSQL
