package kr.or.kftc.reading.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "KFTC_TEST_SECRET_KEY_FOR_JWT_TOKEN_MUST_BE_AT_LEAST_256_BITS_LONG_ENOUGH",
                3600000L
        );
    }

    @Test
    @DisplayName("토큰 생성 및 파싱 성공")
    void generateAndParseToken() {
        String token = jwtUtil.generateToken(1L, "admin01", "관리자1");

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.getLoginId(token)).isEqualTo("admin01");
        assertThat(jwtUtil.getAdminId(token)).isEqualTo(1L);

        Claims claims = jwtUtil.parseToken(token);
        assertThat(claims.get("name", String.class)).isEqualTo("관리자1");
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증 실패")
    void invalidToken() {
        assertThat(jwtUtil.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰 검증 실패")
    void expiredToken() {
        JwtUtil shortLivedJwt = new JwtUtil(
                "KFTC_TEST_SECRET_KEY_FOR_JWT_TOKEN_MUST_BE_AT_LEAST_256_BITS_LONG_ENOUGH",
                -1000L // 이미 만료
        );
        String token = shortLivedJwt.generateToken(1L, "admin01", "관리자1");
        assertThat(shortLivedJwt.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("만료 시간(초) 반환")
    void getExpirationSeconds() {
        assertThat(jwtUtil.getExpirationSeconds()).isEqualTo(3600);
    }
}
