package com.abc_bank;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONObject;


public final class Database_Handler {

    private Connection db_conn;
    private PreparedStatement preparedQuery;


    public Database_Handler() {
        try {
            JSONObject databaseLoginInfo = getDatabaseLoginInfo();

            String Server = databaseLoginInfo.getString("SERVER");
            String Port = databaseLoginInfo.getString("PORT");
            String jdbcUrl = String.format("jdbc:mysql://%s:%s/ABC_BANK", Server, Port);

            this.db_conn = DriverManager.getConnection(
                    jdbcUrl,
                    databaseLoginInfo.getString("USERNAME"),
                    databaseLoginInfo.getString("PASSWORD")
            );

        } catch (SQLException e) {
            System.out.println( "Database Connection Failed!");
            System.out.println("\nThe database server couldn't be found!\n");
            System.out.println(" - Check the server status");
            System.out.println(" - Reconfigure the login credentials ( Run Admin Panel at > Admin/main.py )\n");
            System.exit(0);
        } catch(IOException e) {
            System.out.println( "Database Connection Failed!");
            System.out.println("\nThe login credentials couldn't be read\n");
            System.out.println(" - Check if the json file exists at this location > Admin/info.json\n");
            System.exit(0);
        }
    }

    public JSONObject getDatabaseLoginInfo() throws IOException {
        String jsonData = FileHandler.read(Main.CONFIG_FILE_PATH);
        JSONObject jsonObject = new JSONObject(jsonData);
        return  jsonObject.getJSONObject("dbLoginInfo");
    }

    public void prepareQuery(String query, String[] placeholderValues) throws SQLException {
        PreparedStatement ps = this.db_conn.prepareStatement(query);
        for (int i = 0; i < placeholderValues.length; i++) {
            ps.setString(i + 1, placeholderValues[i]);
        }
        this.preparedQuery = ps;
    }

    public void prepareQuery(String query, ArrayList<String> placeholderValues) throws SQLException {
        PreparedStatement ps = this.db_conn.prepareStatement(query);
        for (int i = 0; i < placeholderValues.size(); i++) {
            ps.setString(i + 1, placeholderValues.get(i));
        }
        this.preparedQuery = ps;
    }

    public void prepareQuery(String query, String placeholderValue) throws SQLException {
        PreparedStatement ps = this.db_conn.prepareStatement(query);
        if (!placeholderValue.isBlank()) {
            ps.setString(1, placeholderValue);
        }
        this.preparedQuery = ps;
    }

    public void prepareQuery(String query) throws SQLException {
        this.preparedQuery = this.db_conn.prepareStatement(query);
    }

    public ResultSet getData() throws SQLException {
        try {
            return this.preparedQuery.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveData() throws SQLException {
        try {
            this.preparedQuery.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
