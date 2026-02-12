package com.example.skilltrack.security;

import com.example.skilltrack.entity.Role;
import com.example.skilltrack.entity.User;
import com.example.skilltrack.repository.RoleRepository;
import com.example.skilltrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);
        
        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException(ex.getMessage());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String oauthId = String.valueOf(attributes.get("id"));
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("login");

        if (email == null) {
            // Some GitHub profiles don't have public emails
            email = username + "@github.com";
        }

        Optional<User> userOptional = userRepository.findActiveByEmail(email)
                .or(() -> userRepository.findActiveByUsername(username));

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getOauthId() == null) {
                user.setOauthId(oauthId);
                user.setOauthProvider(registrationId);
                userRepository.save(user);
            }
        } else {
            user = registerNewOAuth2User(registrationId, oauthId, username, email);
        }

        return oAuth2User;
    }

    private User registerNewOAuth2User(String registrationId, String oauthId, String username, String email) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(UUID.randomUUID().toString()) // Random password for OAuth users
                .oauthId(oauthId)
                .oauthProvider(registrationId)
                .enabled(true)
                .deleted(false)
                .build();

        // Assign default STUDENT role
        Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("Default role NOT_FOUND: ROLE_STUDENT"));
        
        user.addRole(studentRole);
        
        User savedUser = userRepository.save(user);
        log.info("Registered new GitHub user: {}", username);
        return savedUser;
    }
}
