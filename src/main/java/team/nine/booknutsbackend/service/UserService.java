package team.nine.booknutsbackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.nine.booknutsbackend.config.JwtTokenProvider;
import team.nine.booknutsbackend.domain.User;
import team.nine.booknutsbackend.exception.user.InvalidTokenException;
import team.nine.booknutsbackend.exception.user.PasswordErrorException;
import team.nine.booknutsbackend.exception.user.UserNotFoundException;
import team.nine.booknutsbackend.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final CustomUserDetailService customUserDetailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    //회원가입
    @Transactional
    public User join(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    //로그인
    @Transactional
    public User login(User user, String inputPassword) {
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new PasswordErrorException();
        }

        user.setRefreshToken(jwtTokenProvider.createRefreshToken(user.getEmail()));
        return userRepository.save(user);
    }

    //ID로 유저 정보 조회
    @Transactional(readOnly = true)
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    //메일로 유저 정보 조회 (UserDetailService - loadUserByUsername)
    @Transactional(readOnly = true)
    public User findUserByEmail(String email) {
        return customUserDetailService.loadUserByUsername(email);
    }

    //유저 닉네임 중복 체크
    @Transactional(readOnly = true)
    public boolean checkNicknameDuplication(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    //유저 로그인 아이디 중복 체크
    @Transactional(readOnly = true)
    public boolean checkLoginIdDuplication(String loginId) {
        return userRepository.existsByLoginId(loginId);
    }

    //토큰 재발급
    @Transactional
    public Object tokenReIssue(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken) //db에 해당 refresh token이 존재하지 않는 경우
                .orElseThrow(InvalidTokenException::new);
        String accessToken = jwtTokenProvider.createAccessToken(user.getUsername(), user.getRoles());

        //refresh token 만료 기간 체크 -> 2일 이하로 남은 경우 재발급
        long validTime = jwtTokenProvider.getValidTime(refreshToken);
        if (validTime <= 172800000) {


            refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }

        Map<String, String> map = new HashMap<>();
        map.put("accessToken", accessToken);
        map.put("refreshToken", refreshToken);

        return map;
    }

}