package com.springboot.jwtaunthetication.configuration;

import com.springboot.jwtaunthetication.configuration.jwt.JwtTokenFilter;
import com.springboot.jwtaunthetication.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    JwtTokenFilter jwtTokenFilter;
//    @Bean
//    public PasswordEncoder passwordEncoder(){
//
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }

    @SuppressWarnings("deprecation")
    @Bean
    public static NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(username -> userRepository.findByEmail(username)
        .orElseThrow(()-> new UsernameNotFoundException("User" +username+"not found"))

        );
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.exceptionHandling().authenticationEntryPoint(
                ((request, response, ex) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED,ex.getMessage()))
        );

        http.authorizeRequests()
        .antMatchers("/auth/login").permitAll()
        .anyRequest().authenticated();

        http.addFilterBefore(jwtTokenFilter,UsernamePasswordAuthenticationFilter.class);
    }
}


