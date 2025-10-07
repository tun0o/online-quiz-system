# Test Configuration

## OAuth2 Test Configuration

File `application-test.properties` chứa cấu hình OAuth2 cho môi trường test:

### OAuth2 Client Credentials (Test)
- **Google**: `test-google-client-id` / `test-google-client-secret`
- **Facebook**: `test-facebook-client-id` / `test-facebook-client-secret`

### Database
- Sử dụng H2 in-memory database cho test
- Tự động tạo và xóa database sau mỗi test

### JWT
- Secret key: `test-jwt-secret-key-for-testing-purposes-only-very-long-key`
- Expiration: 1 hour (3600000ms)

### Caching & Redis
- Cache disabled cho test
- Redis settings minimal

### Logging
- Reduced logging level để tránh spam trong test output

## Sử dụng

```java
@SpringBootTest
@ActiveProfiles("test")
public class YourTestClass {
    // Test methods
}
```

## Lưu ý

- Test profile tự động disable Flyway migration
- Security auto-configuration có thể bị disable cho một số test
- Async processing được giới hạn để đảm bảo test stability

