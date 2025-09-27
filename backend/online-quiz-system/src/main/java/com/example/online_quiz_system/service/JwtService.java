package com.example.online_quiz_system.service;

import com.example.online_quiz_system.security.UserPrincipal;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.security.MessageDigest;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret}")
    @SuppressWarnings("unused")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    @Value("${app.jwt.private-key}")
    private String jwtPrivateKeyPem;
    @Value("${app.jwt.public-key}")
    private String jwtPublicKeyPem;

    @Value("${app.jwt.issuer:online-quiz-system}")
    private String issuer;
    @Value("${app.jwt.audience:online-quiz-frontend}")
    private String audience;

    private KeyPair getKeyPair() {
        // Cache the parsed key pair to avoid re-parsing PEM every time
        if (cachedKeyPair != null) return cachedKeyPair;
        synchronized (this) {
            if (cachedKeyPair != null) return cachedKeyPair;
            try {
                String privateKeyPEM = jwtPrivateKeyPem
                        .replace("-----BEGIN PRIVATE KEY-----", "")
                        .replace("-----END PRIVATE KEY-----", "")
                        .replaceAll("\\s+", "");
                String publicKeyPEM = jwtPublicKeyPem
                        .replace("-----BEGIN PUBLIC KEY-----", "")
                        .replace("-----END PUBLIC KEY-----", "")
                        .replaceAll("\\s+", "");
                byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPEM);
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyPEM);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
                PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                cachedKeyPair = new KeyPair(publicKey, privateKey);
                return cachedKeyPair;
            } catch (Exception e) {
                throw new RuntimeException("Failed to load RSA key pair", e);
            }
        }
    }

    // cached keypair
    private volatile KeyPair cachedKeyPair;

    // Helper to compute current kid (sha-256 of public key bytes, base64url)
    private String computeKid() {
        try {
            PublicKey pub = getKeyPair().getPublic();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(pub.getEncoded());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            logger.warn("Failed to compute kid for JWKS: {}", e.getMessage());
            return UUID.randomUUID().toString();
        }
    }

    // --- CHỈNH SỬA: thêm roles vào token khi generate bằng UserDetails ---
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Thêm roles vào claims
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);

        if (userDetails instanceof UserPrincipal) {
            claims.put("userId", ((UserPrincipal) userDetails).getId());
        }
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setHeaderParam("kid", computeKid())
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setNotBefore(new Date(System.currentTimeMillis() - 1000))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .setIssuer(issuer)
                .setAudience(audience)
                .setId(jti)
                .signWith(getKeyPair().getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("typ", "refresh");
        return buildToken(claims, userDetails.getUsername(), refreshExpirationMs);
    }

    @SuppressWarnings("unused")
    public String generateAccessToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        if (userDetails instanceof UserPrincipal) {
            claims.put("userId", ((UserPrincipal) userDetails).getId());
        }

        return buildToken(claims, userDetails.getUsername(), jwtExpirationMs);
    }

    @SuppressWarnings("unused")
    public String generateRefreshToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(new HashMap<>(), userDetails.getUsername(), refreshExpirationMs);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        String jti = UUID.randomUUID().toString();
        return Jwts.builder()
                .setHeaderParam("kid", computeKid())
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setNotBefore(new Date(System.currentTimeMillis() - 1000))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setIssuer(issuer)
                .setAudience(audience)
                .setId(jti)
                .signWith(getKeyPair().getPrivate(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // alias để tương thích (nếu controller cũ gọi tên này)
    public String getUsernameFromToken(String token) {
        return extractUsername(token);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKeyPair().getPublic())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @SuppressWarnings("unused")
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @SuppressWarnings("unused")
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && validateToken(token);
    }

    public boolean validateToken(String token) {
        try {
            Jws<Claims> parsed = Jwts.parserBuilder()
                    .setSigningKey(getKeyPair().getPublic())
                    .build()
                    .parseClaimsJws(token);

            Claims claims = parsed.getBody();

            // Validate issuer and audience
            if (claims.getIssuer() != null && !claims.getIssuer().equals(issuer)) {
                logger.warn("Invalid token issuer: {} expected {}", claims.getIssuer(), issuer);
                return false;
            }
            if (claims.getAudience() != null && !claims.getAudience().equals(audience)) {
                logger.warn("Invalid token audience: {} expected {}", claims.getAudience(), audience);
                return false;
            }

            // Check jti blacklist
            try {
                String jti = claims.getId();
                if (jti != null && !jti.isBlank() && redisServiceIsBlacklistedJti(jti)) {
                    return false;
                }
            } catch (Exception ignored) {}

            return true;
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    // Placeholder: will be wired to Redis via a static accessor injected by Spring
    private static com.example.online_quiz_system.service.RedisService staticRedis;
    @org.springframework.beans.factory.annotation.Autowired
    public void setStaticRedis(com.example.online_quiz_system.service.RedisService redis) {
        staticRedis = redis;
    }
    private boolean redisServiceIsBlacklistedJti(String jti) {
        try {
            return staticRedis != null && staticRedis.hasKey("jti:blacklist:" + jti);
        } catch (Exception e) {
            return false;
        }
    }

    // --- THÊM: helper để trích roles từ token ---
    @SuppressWarnings("unused")
    public List<String> extractRoles(String token) {
         try {
             Claims claims = extractAllClaims(token);
             Object rolesObj = claims.get("roles");
             if (rolesObj instanceof List<?>) {
                 return ((List<?>) rolesObj).stream()
                         .map(Object::toString)
                         .collect(Collectors.toList());
             } else {
                 return Collections.emptyList();
             }
         } catch (Exception e) {
             logger.error("Failed to extract roles from token: {}", e.getMessage());
             return Collections.emptyList();
         }
     }

    /**
     * Return JWKS JSON containing the current RSA public key (kid is SHA-256 of public key bytes).
     */
    public String getJwksJson() {
        try {
            PublicKey pub = getKeyPair().getPublic();
            if (!(pub instanceof RSAPublicKey)) {
                throw new IllegalStateException("Public key is not RSA");
            }
            RSAPublicKey rsaPub = (RSAPublicKey) pub;
            // compute kid as sha256(publicKey.getEncoded()) base64url
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rsaPub.getEncoded());
            String kid = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);

            RSAKey jwk = new RSAKey.Builder(rsaPub)
                    .keyID(kid)
                    .build();
            JWKSet set = new JWKSet(jwk);
            return set.toJSONObject().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build JWKS", e);
        }
    }

 }
