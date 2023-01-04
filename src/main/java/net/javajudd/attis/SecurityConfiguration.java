package net.javajudd.attis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${administrator.password:#{null}}")
    String administratorPassword;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        administratorPassword = Optional.ofNullable(administratorPassword).orElse(UUID.randomUUID().toString());
        log.info("\n\nUsing generted administrator password: " + administratorPassword + "\n");

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        auth
                .inMemoryAuthentication()
                .withUser("administrator")
                .password(encoder.encode(administratorPassword))
                .roles("USER", "ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers("/participant/**").permitAll()
                .antMatchers("/images/*").permitAll()
                .antMatchers("/css/*").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                  .formLogin()
        ;
    }
}
