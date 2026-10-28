package calorietracker.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import javax.sql.DataSource;

/**
 * Singleton: у програмі існує рівно один DBConnection, який тримає DataSource
 * (а не одне спільне Connection — аналіз у README).
 *
 * Реалізація — «initialization-on-demand holder»:
 *  - конструктор private — ззовні new DBConnection() неможливий;
 *  - екземпляр створюється ліниво, при першому зверненні до Holder;
 *  - потокобезпечність дає сама JVM: ініціалізація класу виконується один раз і під блокуванням,
 *    тому synchronized/volatile не потрібні.
 *
 * Параметри (url, user, password) беруться з application.properties, а змінні
 * середовища DB_URL / DB_USER / DB_PASSWORD мають пріоритет — так пароль не потрапляє в git.
 */
public final class DBConnection {

    private static final String PROPERTIES_FILE = "application.properties";

    private final DataSource dataSource;

    private DBConnection() {
        Properties props = loadProperties();
        String url = pick("DB_URL", props, "db.url");
        String user = pick("DB_USER", props, "db.user");
        String password = pick("DB_PASSWORD", props, "db.password");
        this.dataSource = new SimpleDataSource(url, user, password);
        DatabaseInitializer.run(dataSource); // автоматичний запуск init.sql при старті
    }

    private static final class Holder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance() {
        return Holder.INSTANCE;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /** Зручний шорткат: нове з'єднання, яке ЗАКРИВАЄ викликач. */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        // Шукаємо файл у classpath (src/main/resources потрапляє туди після mvn compile)
        try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Не знайдено " + PROPERTIES_FILE + " у classpath");
            }
            props.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося прочитати " + PROPERTIES_FILE, e);
        }
        return props;
    }

    private static String pick(String envName, Properties props, String key) {
        String fromEnv = System.getenv(envName);
        return (fromEnv != null && !fromEnv.isEmpty()) ? fromEnv : props.getProperty(key, "");
    }
}
