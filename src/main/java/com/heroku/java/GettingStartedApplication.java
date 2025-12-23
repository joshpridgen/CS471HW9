package com.heroku.java;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    // REQUIRED: /database inserts (timestamp, random_string) and displays all rows
    @GetMapping("/database")
    String database(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("joshpridgen - /database called");

            final var statement = connection.createStatement();

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string " +
                            "(tick timestamp, random_string varchar(50))"
            );

            statement.executeUpdate(
                    "INSERT INTO table_timestamp_and_random_string VALUES (now(), '" + getRandomString() + "')"
            );

            final var resultSet = statement.executeQuery(
                    "SELECT tick, random_string " +
                            "FROM table_timestamp_and_random_string " +
                            "ORDER BY tick DESC"
            );

            final var output = new ArrayList<String>();
            while (resultSet.next()) {
                output.add("Read from DB: " +
                        resultSet.getTimestamp("tick") + " " +
                        resultSet.getString("random_string"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    // EXTRA CREDIT: show the input form
    @GetMapping("/dbinput")
    String dbinput(Map<String, Object> model) {
        return "dbinput";
    }

    // EXTRA CREDIT: accept form submission and insert into random_string column
    @PostMapping("/dbinput")
    String dbinputSubmit(@RequestParam("userString") String userString,
                         Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string " +
                            "(tick timestamp, random_string varchar(50))"
            );

            // escape single quotes so SQL doesn't break
            String safe = userString.replace("'", "''");

            statement.executeUpdate(
                    "INSERT INTO table_timestamp_and_random_string VALUES (now(), '" + safe + "')"
            );

            // show it on /database
            return "redirect:/database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }

    // Generates a random string. UUID is fine for the assignment.
    private String getRandomString() {
        // Optionally shorten to <= 50 chars if you want:
        // return UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        return UUID.randomUUID().toString();
    }

    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
