package com.mylab.ailearn.base.user.service;

import com.mylab.ailearn.base.entity.AppUser;
import com.mylab.ailearn.base.entity.Profile;
import com.mylab.ailearn.base.repository.ProfileRepository;
import com.mylab.ailearn.base.repository.UserRepository;
import com.mylab.ailearn.base.user.dtos.sendback.ProfileDto;
import com.mylab.ailearn.base.user.dtos.sendback.UserDto;
import com.mylab.ailearn.base.user.dtos.sendto.ProfileUpdateRequest;
import com.mylab.ailearn.base.user.dtos.sendto.UserRegisterRequest;
import com.mylab.ailearn.base.user.dtos.sendto.UserUpdateRequest;
import com.mylab.ailearn.base.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserDto register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        if (profileRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already registered");
        }

        AppUser user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        AppUser savedUser = userRepository.save(user);

        // 账号和档案必须共同创建，任意一次保存失败都会回滚整个注册事务。
        Profile profile = userMapper.toProfile(request, savedUser);
        profileRepository.save(profile);
        return userMapper.toDto(savedUser);
    }

    public UserDto getUserById(long id) {
        return userMapper.toDto(findUserById(id));
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfileByUserId(long id) {
        return userMapper.toProfileDto(findProfileByUserId(id));
    }

    @Transactional
    public UserDto updateUser(long id, UserUpdateRequest request) {
        AppUser user = findUserById(id);

        userRepository.findByEmail(request.getEmail())
                .filter(existingUser -> existingUser.getId() != id)
                .ifPresent(existingUser -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
                });

        userMapper.updateEntity(request, user);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        AppUser savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Transactional
    public void deleteUser(long id) {
        AppUser user = findUserById(id);
        userRepository.delete(user);
    }

    @Transactional
    public ProfileDto updateProfile(long id, ProfileUpdateRequest request) {
        Profile profile = findProfileByUserId(id);

        if (!profile.getUsername().equals(request.getUsername())
                && profileRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already registered");
        }

        userMapper.updateProfile(request, profile);
        return userMapper.toProfileDto(profileRepository.save(profile));
    }

    private AppUser findUserById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Profile findProfileByUserId(long id) {
        return profileRepository.findByUserId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public @NonNull UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Profile not found for " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(profile.getRole().name())
                .build();
    }
}
