package my.service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import my.service.domain.model.MUser;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateToken(MUser mUser) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.create()
            .withClaim("id", mUser.getId())
            .withClaim("name", mUser.getName())
            .withClaim("roleName", mUser.getRoleName())
            .sign(algorithm);
    }

    /**
     * トークンを検証し、デコード結果を返す。
     * 署名不正・期限切れ等の場合は JWTVerificationException がスローされる。
     */
    public DecodedJWT verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.require(algorithm)
            .build()
            .verify(token);
    }
}
