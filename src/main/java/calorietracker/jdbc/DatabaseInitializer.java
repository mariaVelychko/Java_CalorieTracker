package calorietracker.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import calorietracker.exception.DataAccessException;

public final class DatabaseInitializer {

    private static final String SCRIPT = "init.sql";

    private DatabaseInitializer() {
    }

    public static void run(DataSource dataSource) {
        String script = readScript();
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            for (String sql : split(script)) {
                st.execute(sql); 
            }
        } catch (SQLException e) {
            throw new DataAccessException("Не вдалося виконати " + SCRIPT, e);
        }
    }

    private static String readScript() {
        try (InputStream in = DatabaseInitializer.class.getClassLoader().getResourceAsStream(SCRIPT)) {
            if (in == null) {
                throw new IllegalStateException("Не знайдено " + SCRIPT + " у classpath");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Не вдалося прочитати " + SCRIPT, e);
        }
    }

    static java.util.List<String> split(String script) {
        StringBuilder noComments = new StringBuilder();
        for (String line : script.split("\\R")) {
            int idx = line.indexOf("--");
            noComments.append(idx >= 0 ? line.substring(0, idx) : line).append('\n');
        }
        java.util.List<String> result = new java.util.ArrayList<>();
        for (String part : noComments.toString().split(";")) {
            String sql = part.trim();
            if (!sql.isEmpty()) {
                result.add(sql);
            }
        }
        return result;
    }
}
