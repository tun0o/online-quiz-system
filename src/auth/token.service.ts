import crypto from 'crypto';
import jwt, { JwtPayload, SignOptions } from 'jsonwebtoken';
// ...existing code...
import jwksClient from 'jwks-rsa';

const ACCESS_TOKEN_EXPIRATION = '10m'; // 5–15m
const REFRESH_TOKEN_EXPIRATION = '7d'; // Example

const ISSUER = 'your-issuer';
const AUDIENCE = 'your-audience';

// JWK client for key rotation
const client = jwksClient({
  jwksUri: 'https://your-domain.com/.well-known/jwks.json'
});

function getKey(header, callback) {
  client.getSigningKey(header.kid, function(err, key) {
    const signingKey = key.getPublicKey();
    callback(null, signingKey);
  });
}

export async function generateTokens(userId: string) {
  // ...existing code...
  const accessToken = jwt.sign(
    {
      sub: userId,
      iss: ISSUER,
      aud: AUDIENCE,
      nbf: Math.floor(Date.now() / 1000),
      // ...other claims...
    },
    process.env.JWT_PRIVATE_KEY, // RS256 private key
    {
      algorithm: 'RS256',
      expiresIn: ACCESS_TOKEN_EXPIRATION,
      keyid: 'current-key-id', // for key rotation
    } as SignOptions
  );

  const refreshTokenPlain = crypto.randomBytes(64).toString('hex');
  const refreshTokenHash = crypto.createHash('sha256').update(refreshTokenPlain).digest('hex');
  // Save refreshTokenHash to DB, not refreshTokenPlain
  await saveRefreshTokenHashToDB(userId, refreshTokenHash);

  return { accessToken, refreshToken: refreshTokenPlain };
}

export async function validateAccessToken(token: string) {
  return new Promise<JwtPayload>((resolve, reject) => {
    jwt.verify(token, getKey, {
      algorithms: ['RS256'],
      issuer: ISSUER,
      audience: AUDIENCE,
      // exp, nbf are checked automatically
    }, (err, decoded) => {
      if (err) return reject(err);
      resolve(decoded as JwtPayload);
    });
  });
}

// When receiving a refresh token, hash it and compare with DB
export async function validateRefreshToken(userId: string, refreshTokenPlain: string) {
  const hash = crypto.createHash('sha256').update(refreshTokenPlain).digest('hex');
  const storedHash = await getRefreshTokenHashFromDB(userId);
  return hash === storedHash;
}

// ...existing code...

