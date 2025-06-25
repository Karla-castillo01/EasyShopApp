package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.data.UserDao;
import org.yearup.models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Make sure this import exists

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlUserDao extends MySqlDaoBase implements UserDao
{
    @Autowired
    public MySqlUserDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public User create(User newUser)
    {
        String sql = "INSERT INTO users (username, hashed_password, role) VALUES (?, ?, ?)";
        // It's good practice to @Autowired the PasswordEncoder if you have it as a bean
        // For simplicity, instantiating directly here as per your original code pattern
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(newUser.getPassword());

        try (Connection connection = getConnection())
        {
            // Use Statement.RETURN_GENERATED_KEYS to get the auto-generated ID
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, newUser.getUsername());
            ps.setString(2, hashedPassword);
            ps.setString(3, newUser.getRole());

            int rowsAffected = ps.executeUpdate(); // Execute the insert

            if (rowsAffected > 0) {
                // Retrieve the auto-generated primary key (user_id)
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1); // Get the first (and usually only) generated key
                    // Construct and return the new User object with the generated ID
                    User createdUser = new User(newId, newUser.getUsername(), hashedPassword, newUser.getRole());
                    createdUser.setPassword(""); // Clear password before returning for security
                    return createdUser;
                } else {
                    // This case indicates an issue with retrieving generated keys
                    System.err.println("Warning: User created, but could not retrieve generated ID.");
                    return null; // Or throw a more specific exception
                }
            } else {
                // This case indicates no rows were affected by the insert
                System.err.println("Error: No rows affected when creating user.");
                return null; // Or throw a more specific exception
            }

        }
        catch (SQLException e)
        {
            // Log the actual SQL exception for detailed debugging
            System.err.println("Error creating user in database: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace to console
            // Re-throw as a RuntimeException with the original cause for Spring to handle
            throw new RuntimeException("Database error during user creation.", e);
        }
    }

    @Override
    public List<User> getAll()
    {
        List<User> users = new ArrayList<>();

        String sql = "SELECT * FROM users";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                User user = mapRow(row);
                users.add(user);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        return users;
    }

    @Override
    public User getUserById(int id)
    {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, id);

            ResultSet row = statement.executeQuery();

            if(row.next())
            {
                User user = mapRow(row);
                return user;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public User getByUserName(String username)
    {
        String sql = "SELECT * " +
                " FROM users " +
                " WHERE username = ?";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            ResultSet row = statement.executeQuery();
            if(row.next())
            {
                User user = mapRow(row);
                return user;
            }
        }
        catch (SQLException e)
        {
            // This is good, it prints the error to System.out.
            System.out.println("Error retrieving user by username: " + e.getMessage());
            e.printStackTrace(); // Added for more detailed logging
        }

        return null;
    }

    @Override
    public int getIdByUsername(String username)
    {
        User user = getByUserName(username);

        if(user != null)
        {
            return user.getId();
        }

        return -1;
    }

    @Override
    public boolean exists(String username)
    {
        User user = getByUserName(username);
        return user != null;
    }

    private User mapRow(ResultSet row) throws SQLException
    {
        int userId = row.getInt("user_id");
        String username = row.getString("username");
        String hashedPassword = row.getString("hashed_password");
        String role = row.getString("role");

        return new User(userId, username,hashedPassword, role);
    }
}
