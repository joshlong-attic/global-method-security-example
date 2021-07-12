package com.example.securemethods;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;

@SpringBootApplication
public class SecureMethodsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureMethodsApplication.class, args);
    }

    //
    static final UserDetails JOSH = User.builder().roles("USER").username("jlong").password("pw").build();

    //
    static final UserDetails MARIO = User.builder().roles("USER", "ADMIN").username("mgray").password("pw").build();

    @Bean
    UserDetailsService authentication() {
        return new InMemoryUserDetailsManager(List.of(JOSH, MARIO));
    }

}


@Configuration
@EnableGlobalMethodSecurity(proxyTargetClass = true, prePostEnabled = true, securedEnabled = true)
class DemoRunner extends GlobalMethodSecurityConfiguration {

}

@Configuration
class Demo {


    @Bean
    ApplicationRunner runner(CustomerService customerService) {
        return args -> {

            System.out.println("trying to doSomethingBasic with JOSH");
            installAuthentication(SecureMethodsApplication.JOSH);
            customerService.doSomethingBasic();

            SecurityContextHolder.clearContext();

            System.out.println("trying to delete with MARIO");
            installAuthentication(SecureMethodsApplication.MARIO);
            customerService.delete();


            try {

                System.out.println("trying to delete with USER");
                installAuthentication(SecureMethodsApplication.JOSH);
                customerService.delete();

            } catch (Exception ex) {
                System.out.println("EXCEPTION: " + ex.getMessage());
            }

        };
    }

    private void installAuthentication(UserDetails userDetails) {
        var auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),
                userDetails.getPassword(),
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}

@Service
class CustomerService {

    private Principal getPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    @Secured(("ROLE_USER"))
    public void doSomethingBasic() {
        System.out.println("doing something basic for " + getPrincipal().getName() + '.');
    }

    @Secured({"ROLE_ADMIN"})
    public void delete() {
        System.out.println("running delete for user " + getPrincipal().getName() + '.');
    }
}