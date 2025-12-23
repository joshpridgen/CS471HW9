package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("joshpridgen - /database called");
            final var statement = connection.createStatement();
//            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
//            statement.executeUpdate("INSERT INTO ticks VALUES (now())");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string (tick timestamp, random_string varchar(50))");
            statement.executeUpdate("INSERT INTO table_timestamp_and_random_string VALUES (now(), '" + getRandomString() + "')");
            final var resultSet = statement.executeQuery("SELECT tick, random_string FROM table_timestamp_and_random_string ORDER BY tick DESC");            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add("Read from DB: "
                        + resultSet.getTimestamp("tick")
                        + " "
                        + resultSet.getString("random_string"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    private String getRandomString() {
        return UUID.randomUUID().toString();
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}