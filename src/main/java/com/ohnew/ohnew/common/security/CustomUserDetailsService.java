package com.ohnew.ohnew.common.security;

import com.ohnew.ohnew.apiPayload.code.exception.GeneralException;
import com.ohnew.ohnew.apiPayload.code.status.ErrorStatus;
import com.ohnew.ohnew.entity.User;
import com.ohnew.ohnew.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userPk)  {
        User user = userRepository.findById(Long.parseLong(userPk))
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        return new CustomUserDetail(user);
    }	// 위에서 생성한 CustomUserDetails Class
}

