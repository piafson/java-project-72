package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlCheckRepository extends BaseRepository {

    public static void save(UrlCheck urlCheck) throws SQLException {
        String sql = "INSERT INTO url_checks (status_code, title, h1, description, created_at, url_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (var connection = dataSource.getConnection();
                 var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, urlCheck.getStatusCode());
            preparedStatement.setString(2, urlCheck.getTitle());
            preparedStatement.setString(3, urlCheck.getH1());
            preparedStatement.setString(4, urlCheck.getDescription());
            var createdAt = new Timestamp(new Date().getTime());
            preparedStatement.setTimestamp(5, createdAt);
            preparedStatement.setLong(6, urlCheck.getUrlId());
            preparedStatement.executeUpdate();
        }
    }

    public static List<UrlCheck> findByUrlId(long urlId) throws SQLException {
        String sql = "SELECT * FROM url_checks WHERE url_id = ?";
        var urlChecks = new ArrayList<UrlCheck>();
        try (var connection = dataSource.getConnection();
                 var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, urlId);
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var statusCode = resultSet.getInt("status_code");
                var title = resultSet.getString("title");
                var h1 = resultSet.getString("h1");
                var description = resultSet.getString("description");
                var createdAt = resultSet.getTimestamp("created_at");
                var urlCheck = new UrlCheck(statusCode, title, h1, description);
                urlCheck.setId(id);
                urlCheck.setUrlId(urlId);
                urlCheck.setCreatedAt(createdAt);
                urlChecks.add(urlCheck);
            }
            return urlChecks;
        }
    }

    public static Map<Long, UrlCheck> getLastCheck() throws SQLException {
        String sql = "SELECT DISTINCT ON (url_id) * FROM url_checks ORDER BY url_id, created_at DESC";
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            var resultSet = statement.executeQuery(sql);
            var result = new HashMap<Long, UrlCheck>();
            while (resultSet.next()) {
                var urlCheck = new UrlCheck();
                var urlId = resultSet.getLong("url_id");
                var statusCode = resultSet.getInt("status_code");
                var createdAt = resultSet.getTimestamp("created_at");
                urlCheck.setUrlId(urlId);
                urlCheck.setStatusCode(statusCode);
                urlCheck.setCreatedAt(createdAt);
                result.put(urlId, urlCheck);
            }
            return result;
        }

    }
}
