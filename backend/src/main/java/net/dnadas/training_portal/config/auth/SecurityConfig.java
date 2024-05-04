package net.dnadas.training_portal.config.auth;

import lombok.NoArgsConstructor;
import net.dnadas.training_portal.exception.auth.UnauthorizedException;
import net.dnadas.training_portal.model.group.UserGroupDao;
import net.dnadas.training_portal.model.group.project.ProjectDao;
import net.dnadas.training_portal.model.group.project.task.TaskDao;
import net.dnadas.training_portal.model.user.ApplicationUserDao;
import net.dnadas.training_portal.service.auth.CustomPermissionEvaluator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@NoArgsConstructor
public class SecurityConfig {

  /**
   * Unique field of {@link UserDetailsService} that represents the subject is always called
   * "username", in our application it is the username.
   */
  @Bean
  public UserDetailsService userDetailsService(ApplicationUserDao applicationUserDao) {
    return username -> (UserDetails) applicationUserDao
      .findByUsernameAndEnabled(username)
      .orElseThrow(UnauthorizedException::new);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
  }

  @Bean
  public AuthenticationProvider authenticationProvider(ApplicationUserDao applicationUserDao) {
    DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
    daoAuthenticationProvider.setUserDetailsService(userDetailsService(applicationUserDao));
    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
    return daoAuthenticationProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
    throws Exception {
    return config.getAuthenticationManager();
  }

  // Method-level security

  /**
   * {@link EnableMethodSecurity} would not pick up {@link CustomPermissionEvaluator} by default<br>
   * <a href="https://docs.spring.io/spring-security/reference/5.8/migration/servlet/authorization.html#servlet-replace-permissionevaluator-bean-with-methodsecurityexpression-handler">
   * Spring Security Docs</a> <br>
   *
   * @param userGroupDao {@link UserGroupDao}
   * @param projectDao   {@link ProjectDao}
   */
  @Bean
  public MethodSecurityExpressionHandler expressionHandler(
    ApplicationUserDao applicationUserDao, UserGroupDao userGroupDao, ProjectDao projectDao,
    TaskDao taskDao) {
    var expressionHandler = new DefaultMethodSecurityExpressionHandler();
    expressionHandler.setPermissionEvaluator(
      new CustomPermissionEvaluator(applicationUserDao, userGroupDao, projectDao, taskDao));
    return expressionHandler;
  }
}