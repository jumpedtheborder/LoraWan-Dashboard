package com.uniofsurrey.lorawandashboard.config;

import com.uniofsurrey.lorawandashboard.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserRepository userRepository;

    @Autowired
    public WebSecurityConfig(UserRepository userRepository) {
        super();
        this.userRepository = userRepository;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.headers().frameOptions().disable();
        http.httpBasic().disable();
        http.authorizeRequests()
                .antMatchers("/*.js", "/css/**", "/js/**", "/images/**").permitAll()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers(HttpMethod.POST, "/rest/login").permitAll()
                .antMatchers(HttpMethod.POST, "/rest/user").permitAll()
                .antMatchers(HttpMethod.GET, "/rest/user/self").permitAll()
                .antMatchers(HttpMethod.GET, "/rest/book/*").permitAll()
                .antMatchers("/index.html", "/").permitAll()
                .antMatchers("/rest/books").permitAll()
                .antMatchers("/rest/search").permitAll()
                .antMatchers(HttpMethod.GET, "/rest/review/book/*").permitAll()
                .anyRequest().authenticated()
                .and()
                .logout()
                .deleteCookies("jwt")
                .invalidateHttpSession(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/rest/logout"))
                .permitAll()
                .and()
                // We filter the api/login requests
                .addFilterBefore(new JwtLoginFilter("/rest/login", authenticationManager()),
                        UsernamePasswordAuthenticationFilter.class)
                // And filter other requests to check the presence of JWT in header
                .addFilterBefore(new JwtAuthenticationFilter(userRepository),
                        UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(new JwtUserDetailsService(userRepository)).passwordEncoder(new UserPasswordEncoder());
    }
}