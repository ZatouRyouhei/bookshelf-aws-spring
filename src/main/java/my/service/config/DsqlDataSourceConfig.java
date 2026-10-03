package my.service.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dsql.DsqlUtilities;

/**
 * Aurora DSQLはIAM認証トークンをパスワードとして使う(トークンの既定有効期限は15分)。
 * Lambda環境ではプロセスがフリーズ/再開されるため、バックグラウンドスレッドでの
 * 定期リフレッシュは実行が保証されない。そのため接続取得のたびに有効期限をチェックし、
 * 必要な場合だけ新しいトークンを発行する遅延リフレッシュ方式を取る。
 */
@Configuration
public class DsqlDataSourceConfig {

    private static final Duration TOKEN_REFRESH_MARGIN = Duration.ofMinutes(5);

    // 自前でDataSourceを生成しているため、hikari.*の設定を明示的にバインドする
    @Bean(destroyMethod = "close")
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariDataSource dataSource(
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String username,
            @Value("${aws.dsql.region}") String region,
            @Value("${aws.dsql.cluster-endpoint}") String clusterEndpoint) {

        DsqlUtilities dsqlUtilities = DsqlUtilities.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();

        HikariDataSource dataSource = new TokenRefreshingDataSource(dsqlUtilities, clusterEndpoint, region, username);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setDriverClassName("org.postgresql.Driver");
        return dataSource;
    }

    private static final class TokenRefreshingDataSource extends HikariDataSource {

        private final DsqlUtilities dsqlUtilities;
        private final String clusterEndpoint;
        private final Region region;
        private final boolean admin;

        private volatile long tokenIssuedAtMillis = -1;

        private TokenRefreshingDataSource(DsqlUtilities dsqlUtilities, String clusterEndpoint, String region, String username) {
            this.dsqlUtilities = dsqlUtilities;
            this.clusterEndpoint = clusterEndpoint;
            this.region = Region.of(region);
            this.admin = "admin".equals(username);
        }

        @Override
        public Connection getConnection() throws SQLException {
            refreshTokenIfNeeded();
            return super.getConnection();
        }

        private void refreshTokenIfNeeded() {
            long now = System.currentTimeMillis();
            if (tokenIssuedAtMillis < 0 || now - tokenIssuedAtMillis > TOKEN_REFRESH_MARGIN.toMillis()) {
                synchronized (this) {
                    now = System.currentTimeMillis();
                    if (tokenIssuedAtMillis < 0 || now - tokenIssuedAtMillis > TOKEN_REFRESH_MARGIN.toMillis()) {
                        setPassword(generateAuthToken());
                        tokenIssuedAtMillis = now;
                    }
                }
            }
        }

        private String generateAuthToken() {
            return admin
                    ? dsqlUtilities.generateDbConnectAdminAuthToken(builder -> builder
                            .hostname(clusterEndpoint)
                            .region(region))
                    : dsqlUtilities.generateDbConnectAuthToken(builder -> builder
                            .hostname(clusterEndpoint)
                            .region(region));
        }
    }
}
