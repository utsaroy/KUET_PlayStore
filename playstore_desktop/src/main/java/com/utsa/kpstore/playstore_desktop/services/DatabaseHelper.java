package com.utsa.kpstore.playstore_desktop.services;

import com.utsa.kpstore.playstore_desktop.models.User;
import com.utsa.kpstore.playstore_desktop.models.Category;
import com.utsa.kpstore.playstore_desktop.models.App;
import com.utsa.kpstore.playstore_desktop.models.Rating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DATABASE_URL = "jdbc:sqlite:playstore.db";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initializeDatabase() {
        String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        full_name TEXT NOT NULL,
                        email TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL,
                        is_admin INTEGER DEFAULT 0,
                        is_active INTEGER DEFAULT 1,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        String createCategoriesTable = """
                    CREATE TABLE IF NOT EXISTS categories (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT UNIQUE NOT NULL,
                        description TEXT,
                        icon_url TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """;

        String createAppsTable = """
                    CREATE TABLE IF NOT EXISTS apps (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL,
                        description TEXT,
                        developer_id INTEGER NOT NULL,
                        category_id INTEGER NOT NULL,
                        version TEXT NOT NULL,
                        size INTEGER NOT NULL,
                        downloads INTEGER DEFAULT 0,
                        price REAL DEFAULT 0.0,
                        icon_url TEXT,
                        package_name TEXT UNIQUE NOT NULL,
                        apk_file_path TEXT,
                        is_approved INTEGER DEFAULT 0,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (developer_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
                    )
                """;

        String createRatingsTable = """
                    CREATE TABLE IF NOT EXISTS ratings (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        app_id INTEGER NOT NULL,
                        user_id INTEGER NOT NULL,
                        rating INTEGER NOT NULL CHECK(rating >= 1 AND rating <= 5),
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE CASCADE,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        UNIQUE(app_id, user_id)
                    )
                """;

        String createUserDownloadsTable = """
                    CREATE TABLE IF NOT EXISTS user_downloads (
                        user_id INTEGER NOT NULL,
                        app_id INTEGER NOT NULL,
                        downloaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (user_id, app_id),
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE CASCADE
                    )
                """;

        try (Connection conn = connect();
                Statement stmt = conn.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createCategoriesTable);
            stmt.execute(createAppsTable);
            stmt.execute(createRatingsTable);
            stmt.execute(createUserDownloadsTable);

            createDefaultAdmin();
            createDefaultCategories();

            // Schema Migration: Add is_active column if it doesn't exist
            addColumnIfNotExists(conn, "users", "is_active", "INTEGER DEFAULT 1");
            // Add admin_feedback to apps
            addColumnIfNotExists(conn, "apps", "admin_feedback", "TEXT");
            // Add review_text to ratings
            addColumnIfNotExists(conn, "ratings", "review_text", "TEXT");

            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static void addColumnIfNotExists(Connection conn, String tableName,
            String columnName,
            String columnDefinition) {
        try (Statement stmt = conn.createStatement()) {
            // Check if column exists
            ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
            boolean columnExists = false;
            while (rs.next()) {
                if (rs.getString("name").equals(columnName)) {
                    columnExists = true;
                    break;
                }
            }

            if (!columnExists) {
                String sql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " +
                        columnDefinition;
                stmt.execute(sql);
                System.out.println("Added column " + columnName + " to table " + tableName);
            }
        } catch (SQLException e) {
            System.err.println("Error adding column " + columnName + ": " +
                    e.getMessage());
        }
    }

    private static void createDefaultAdmin() {
        String checkAdminSql = "SELECT COUNT(*) FROM users WHERE is_admin = 1";
        String createAdminSql = "INSERT INTO users (full_name, email, password, is_admin) VALUES (?, ?, ?, 1)";

        try (Connection conn = connect();
                Statement checkStmt = conn.createStatement();
                ResultSet rs = checkStmt.executeQuery(checkAdminSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement pstmt = conn.prepareStatement(createAdminSql)) {
                    pstmt.setString(1, "Administrator");
                    pstmt.setString(2, "admin@kuet.ac.bd");
                    pstmt.setString(3, "admin123");
                    pstmt.executeUpdate();
                    System.out.println("Default admin account created: admin@kuet.ac.bd / admin123");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating default admin: " + e.getMessage());
        }
    }

    private static void createDefaultCategories() {
        String checkCategoriesSql = "SELECT COUNT(*) FROM categories";
        String insertCategorySql = "INSERT INTO categories (name, description, icon_url) VALUES (?, ?, ?)";

        try (Connection conn = connect();
                Statement checkStmt = conn.createStatement();
                ResultSet rs = checkStmt.executeQuery(checkCategoriesSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                String[][] defaultCategories = {
                        { "Games", "Action, puzzle, strategy games and more", "games_icon.png" },
                        { "Education", "Learning apps and educational tools", "education_icon.png" },
                        { "Productivity", "Tools to boost your productivity", "productivity_icon.png" },
                        { "Entertainment", "Movies, music, and fun apps", "entertainment_icon.png" },
                        { "Social", "Connect with friends and family", "social_icon.png" },
                        { "Utilities", "Helpful tools and utilities", "utilities_icon.png" },
                        { "Health & Fitness", "Track your health and fitness", "health_icon.png" },
                        { "Business", "Apps for business professionals", "business_icon.png" }
                };

                try (PreparedStatement pstmt = conn.prepareStatement(insertCategorySql)) {
                    for (String[] category : defaultCategories) {
                        pstmt.setString(1, category[0]);
                        pstmt.setString(2, category[1]);
                        pstmt.setString(3, category[2]);
                        pstmt.executeUpdate();
                    }
                    System.out.println("Default categories created");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating default categories: " + e.getMessage());
        }
    }

    public static User registerUser(String fullName, String email, String password) {
        if (emailExists(email)) {
            return null;
        }
        String sql = "INSERT INTO users (full_name, email, password) VALUES (?, ?, ?)";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, email.toLowerCase().trim());
            pstmt.setString(3, password);
            pstmt.executeUpdate();

            System.out.println("User registered successfully: " + email);
            return getUserByEmail(email);

        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return null;
        }
    }

    public static User validateLogin(String email, String password) {
        String sql = "SELECT id, full_name, email, password, is_active, created_at FROM users WHERE email = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.toLowerCase().trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(password)) {
                    if (rs.getInt("is_active") == 0) {
                        return null; // User is banned
                    }
                    return new User(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getTimestamp("created_at"));
                }
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Error validating login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.toLowerCase().trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            return false;
        }
    }

    public static User getUserByEmail(String email) {
        String sql = "SELECT id, full_name, email, password, is_active, created_at FROM users WHERE email = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.toLowerCase().trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("is_active") == 1);
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Error retrieving user: " + e.getMessage());
            return null;
        }
    }

    public static User validateAdminLogin(String email, String password) {
        String sql = "SELECT id, full_name, email, password, is_admin, is_active, created_at FROM users WHERE email = ? AND is_admin = 1";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.toLowerCase().trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (storedPassword.equals(password)) {
                    if (rs.getInt("is_active") == 0) {
                        return null; // Admin is banned/inactive
                    }
                    return new User(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getTimestamp("created_at"));
                }
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Error validating admin login: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_admin = 0";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user count: " + e.getMessage());
        }
        return 0;
    }

    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, full_name, email, password, is_active, created_at FROM users WHERE is_admin = 0 ORDER BY created_at DESC";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("is_active") == 1));
            }
        } catch (SQLException e) {
            System.err.println("Error getting users: " + e.getMessage());
        }
        return users;
    }

    public static boolean updateUserStatus(int userId, boolean isActive) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, isActive ? 1 : 0);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user status: " + e.getMessage());
            return false;
        }
    }

    public static boolean makeUserAdmin(String email) {
        String sql = "UPDATE users SET is_admin = 1 WHERE email = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.toLowerCase().trim());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error promoting user to admin: " + e.getMessage());
            return false;
        }
    }

    public static boolean registerAdmin(String fullName, String email, String password) {
        if (emailExists(email)) {
            return false;
        }
        String sql = "INSERT INTO users (full_name, email, password, is_admin) VALUES (?, ?, ?, 1)";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
            pstmt.setString(2, email.toLowerCase().trim());
            pstmt.setString(3, password);
            pstmt.executeUpdate();

            System.out.println("Admin registered successfully: " + email);
            return true;

        } catch (SQLException e) {
            System.err.println("Error registering admin: " + e.getMessage());
            return false;
        }
    }

    public static List<User> getAllAdmins() {
        List<User> admins = new ArrayList<>();
        String sql = "SELECT id, full_name, email, password, is_active, created_at FROM users WHERE is_admin = 1 ORDER BY created_at DESC";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                admins.add(new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("is_active") == 1));
            }
        } catch (SQLException e) {
            System.err.println("Error getting admins: " + e.getMessage());
        }
        return admins;
    }

    public static boolean demoteAdmin(String email) {
        String sql = "UPDATE users SET is_admin = 0 WHERE email = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email.toLowerCase().trim());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error demoting admin: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateUserName(int userId, String newName) {
        String sql = "UPDATE users SET full_name = ? WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating user name: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateUserPassword(int userId, String currentPassword, String newPassword) {
        // First verify current password
        String verifySql = "SELECT password FROM users WHERE id = ?";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement verifyStmt = conn.prepareStatement(verifySql)) {

            verifyStmt.setInt(1, userId);
            ResultSet rs = verifyStmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                if (!storedPassword.equals(currentPassword)) {
                    return false; // Current password incorrect
                }
            } else {
                return false; // User not found
            }

            // Now update password
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setInt(2, userId);

                int affectedRows = updateStmt.executeUpdate();
                return affectedRows > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
            return false;
        }
    }

    // ============= Category CRUD Methods =============

    public static List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, description, icon_url, created_at FROM categories ORDER BY name";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("icon_url"),
                        rs.getTimestamp("created_at")));
            }
        } catch (SQLException e) {
            System.err.println("Error getting categories: " + e.getMessage());
        }
        return categories;
    }

    public static Category getCategoryById(int categoryId) {
        String sql = "SELECT id, name, description, icon_url, created_at FROM categories WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("icon_url"),
                        rs.getTimestamp("created_at"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting category: " + e.getMessage());
        }
        return null;
    }

    // ============= App CRUD Methods =============

    public static App createApp(String name, String description, int developerId, int categoryId,
            String version, long size, double price, String iconUrl,
            String packageName, String apkFilePath) {
        String sql = """
                    INSERT INTO apps (name, description, developer_id, category_id, version, size,
                                     price, icon_url, package_name, apk_file_path, is_approved)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)
                """;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, developerId);
            pstmt.setInt(4, categoryId);
            pstmt.setString(5, version);
            pstmt.setLong(6, size);
            pstmt.setDouble(7, price);
            pstmt.setString(8, iconUrl);
            pstmt.setString(9, packageName);
            pstmt.setString(10, apkFilePath);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int appId = rs.getInt(1);
                    System.out.println("App created successfully with ID: " + appId);
                    return getAppById(appId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating app: " + e.getMessage());
        }
        return null;
    }

    public static App getAppById(int appId) {
        String sql = """
                    SELECT a.id, a.name, a.description, a.developer_id, u.full_name as developer_name,
                           a.category_id, c.name as category_name, a.version, a.size, a.downloads,
                           a.price, a.icon_url, a.package_name, a.apk_file_path, a.is_approved, a.admin_feedback,
                           a.created_at, a.updated_at
                    FROM apps a
                    JOIN users u ON a.developer_id = u.id
                    JOIN categories c ON a.category_id = c.id
                    WHERE a.id = ?
                """;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createAppFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting app: " + e.getMessage());
        }
        return null;
    }

    public static List<App> getAllApps() {
        return getApps("SELECT a.id, a.name, a.description, a.developer_id, u.full_name as developer_name, " +
                "a.category_id, c.name as category_name, a.version, a.size, a.downloads, " +
                "a.price, a.icon_url, a.package_name, a.apk_file_path, a.is_approved, a.admin_feedback, " +
                "a.created_at, a.updated_at " +
                "FROM apps a " +
                "JOIN users u ON a.developer_id = u.id " +
                "JOIN categories c ON a.category_id = c.id " +
                "ORDER BY a.created_at DESC");
    }

    public static List<App> getApprovedApps() {
        return getApps("SELECT a.id, a.name, a.description, a.developer_id, u.full_name as developer_name, " +
                "a.category_id, c.name as category_name, a.version, a.size, a.downloads, " +
                "a.price, a.icon_url, a.package_name, a.apk_file_path, a.is_approved, a.admin_feedback, " +
                "a.created_at, a.updated_at " +
                "FROM apps a " +
                "JOIN users u ON a.developer_id = u.id " +
                "JOIN categories c ON a.category_id = c.id " +
                "WHERE a.is_approved = 1 " +
                "ORDER BY a.created_at DESC");
    }

    public static List<App> getUnapprovedApps() {
        return getApps("SELECT a.id, a.name, a.description, a.developer_id, u.full_name as developer_name, " +
                "a.category_id, c.name as category_name, a.version, a.size, a.downloads, " +
                "a.price, a.icon_url, a.package_name, a.apk_file_path, a.is_approved, a.admin_feedback, " +
                "a.created_at, a.updated_at " +
                "FROM apps a " +
                "JOIN users u ON a.developer_id = u.id " +
                "JOIN categories c ON a.category_id = c.id " +
                "WHERE a.is_approved = 0 " +
                "ORDER BY a.created_at ASC");
    }

    public static List<App> getAppsByCategory(int categoryId) {
        String sql = """
                    SELECT a.id, a.name, a.description, a.developer_id, u.full_name as developer_name,
                           a.category_id, c.name as category_name, a.version, a.size, a.downloads,
                           a.price, a.icon_url, a.package_name, a.apk_file_path, a.is_approved, a.admin_feedback,
                           a.created_at, a.updated_at
                    FROM apps a
                    JOIN users u ON a.developer_id = u.id
                    JOIN categories c ON a.category_id = c.id
                    WHERE a.category_id = ? AND a.is_approved = 1
                    ORDER BY a.created_at DESC
                """;

        List<App> apps = new ArrayList<>();
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                apps.add(createAppFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting apps by category: " + e.getMessage());
        }
        return apps;
    }

    public static List<App> getRecentApps(int limit) {
        String sql = """
                    SELECT a.id, a.name, a.description, a.developer_id, u.full_name as developer_name,
                           a.category_id, c.name as category_name, a.version, a.size, a.downloads,
                           a.price, a.icon_url, a.package_name, a.apk_file_path, a.is_approved, a.admin_feedback,
                           a.created_at, a.updated_at
                    FROM apps a
                    JOIN users u ON a.developer_id = u.id
                    JOIN categories c ON a.category_id = c.id
                    WHERE a.is_approved = 1
                    ORDER BY a.created_at DESC
                    LIMIT ?
                """;

        List<App> apps = new ArrayList<>();
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                apps.add(createAppFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent apps: " + e.getMessage());
        }
        return apps;
    }

    public static List<App> getAppsByDeveloper(int developerId) {
        String sql = """
                    SELECT a.id, a.name, a.description, a.developer_id, u.full_name as developer_name,
                           a.category_id, c.name as category_name, a.version, a.size, a.downloads,
                           a.price, a.icon_url, a.package_name, a.apk_file_path, a.is_approved, a.admin_feedback,
                           a.created_at, a.updated_at
                    FROM apps a
                    JOIN users u ON a.developer_id = u.id
                    JOIN categories c ON a.category_id = c.id
                    WHERE a.developer_id = ?
                    ORDER BY a.created_at DESC
                """;

        List<App> apps = new ArrayList<>();
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, developerId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                apps.add(createAppFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting apps by developer: " + e.getMessage());
        }
        return apps;
    }

    public static List<App> searchApps(String query) {
        String sql = """
                    SELECT a.id, a.name, a.description, a.developer_id, u.full_name as developer_name,
                           a.category_id, c.name as category_name, a.version, a.size, a.downloads,
                           a.price, a.icon_url, a.package_name, a.apk_file_path, a.is_approved, a.admin_feedback,
                           a.created_at, a.updated_at
                    FROM apps a
                    JOIN users u ON a.developer_id = u.id
                    JOIN categories c ON a.category_id = c.id
                    WHERE a.is_approved = 1 AND (a.name LIKE ? OR a.description LIKE ?)
                    ORDER BY a.name ASC
                """;

        List<App> apps = new ArrayList<>();
        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchTerm = "%" + query + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                apps.add(createAppFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching apps: " + e.getMessage());
        }
        return apps;
    }

    private static List<App> getApps(String sql) {
        List<App> apps = new ArrayList<>();
        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                apps.add(createAppFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting apps: " + e.getMessage());
        }
        return apps;
    }

    private static App createAppFromResultSet(ResultSet rs) throws SQLException {
        return new App(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("developer_id"),
                rs.getString("developer_name"),
                rs.getInt("category_id"),
                rs.getString("category_name"),
                rs.getString("version"),
                rs.getLong("size"),
                rs.getInt("downloads"),
                rs.getDouble("price"),
                rs.getString("icon_url"),
                rs.getString("package_name"),
                rs.getString("apk_file_path"),
                rs.getInt("is_approved") == 1,
                rs.getString("admin_feedback"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"));
    }

    public static boolean updateAppDetails(int appId, String name, String description, int categoryId, String version) {
        String sql = "UPDATE apps SET name = ?, description = ?, category_id = ?, version = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, categoryId);
            pstmt.setString(4, version);
            pstmt.setInt(5, appId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating app details: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAppStatus(int appId, int isApproved, String feedback) {
        String sql = "UPDATE apps SET is_approved = ?, admin_feedback = ? WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, isApproved);
            pstmt.setString(2, feedback);
            pstmt.setInt(3, appId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating app status: " + e.getMessage());
        }
        return false;
    }

    public static boolean approveApp(int appId) {
        return updateAppStatus(appId, 1, null);
    }

    public static boolean incrementDownloads(int appId) {
        String sql = "UPDATE apps SET downloads = downloads + 1 WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error incrementing downloads: " + e.getMessage());
        }
        return false;
    }

    public static int getTotalAppsCount() {
        String sql = "SELECT COUNT(*) FROM apps";

        try (Connection conn = connect();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting app count: " + e.getMessage());
        }
        return 0;
    }

    // ============= Rating CRUD Methods =============

    public static Rating createOrUpdateRating(int appId, int userId, int rating, String reviewText) {
        String checkSql = "SELECT id FROM ratings WHERE app_id = ? AND user_id = ?";
        String insertSql = "INSERT INTO ratings (app_id, user_id, rating, review_text) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE ratings SET rating = ?, review_text = ? WHERE app_id = ? AND user_id = ?";

        try (Connection conn = connect()) {
            // Check if rating exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, appId);
                checkStmt.setInt(2, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Update existing rating
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, rating);
                        updateStmt.setString(2, reviewText);
                        updateStmt.setInt(3, appId);
                        updateStmt.setInt(4, userId);
                        updateStmt.executeUpdate();
                        System.out.println("Rating updated successfully");
                    }
                } else {
                    // Insert new rating
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, appId);
                        insertStmt.setInt(2, userId);
                        insertStmt.setInt(3, rating);
                        insertStmt.setString(4, reviewText);
                        insertStmt.executeUpdate();
                        System.out.println("Rating created successfully");
                    }
                }

                return getRatingByAppAndUser(appId, userId);
            }
        } catch (SQLException e) {
            System.err.println("Error creating/updating rating: " + e.getMessage());
        }
        return null;
    }

    public static Rating getRatingByAppAndUser(int appId, int userId) {
        String sql = """
                    SELECT r.id, r.app_id, r.user_id, u.full_name as user_name, r.rating, r.review_text, r.created_at
                    FROM ratings r
                    JOIN users u ON r.user_id = u.id
                    WHERE r.app_id = ? AND r.user_id = ?
                """;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Rating(
                        rs.getInt("id"),
                        rs.getInt("app_id"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getTimestamp("created_at"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting rating: " + e.getMessage());
        }
        return null;
    }

    public static List<Rating> getRatingsByApp(int appId) {
        List<Rating> ratings = new ArrayList<>();
        String sql = """
                    SELECT r.id, r.app_id, r.user_id, u.full_name as user_name, r.rating, r.review_text, r.created_at
                    FROM ratings r
                    JOIN users u ON r.user_id = u.id
                    WHERE r.app_id = ?
                    ORDER BY r.created_at DESC
                """;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ratings.add(new Rating(
                        rs.getInt("id"),
                        rs.getInt("app_id"),
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getInt("rating"),
                        rs.getString("review_text"),
                        rs.getTimestamp("created_at")));
            }
        } catch (SQLException e) {
            System.err.println("Error getting ratings for app: " + e.getMessage());
        }
        return ratings;
    }

    public static double getAverageRating(int appId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM ratings WHERE app_id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error getting average rating: " + e.getMessage());
        }
        return 0.0;
    }

    public static int getRatingCount(int appId) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE app_id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting rating count: " + e.getMessage());
        }
        return 0;
    }

    // ============= App Update Methods =============

    public static boolean updateAppFilePaths(int appId, String iconUrl, String apkFilePath) {
        String sql = "UPDATE apps SET icon_url = ?, apk_file_path = ?, is_approved = 0, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, iconUrl);
            pstmt.setString(2, apkFilePath);
            pstmt.setInt(3, appId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating app file paths: " + e.getMessage());
        }
        return false;
    }

    public static boolean updateApp(int appId, String name, String description, int categoryId,
            String version, long size, double price) {
        String sql = """
                    UPDATE apps SET name = ?, description = ?, category_id = ?, version = ?,
                                   size = ?, price = ?, is_approved = 0, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                """;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, categoryId);
            pstmt.setString(4, version);
            pstmt.setLong(5, size);
            pstmt.setDouble(6, price);
            pstmt.setInt(7, appId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating app: " + e.getMessage());
        }
        return false;
    }

    public static boolean deleteApp(int appId) {
        String sql = "DELETE FROM apps WHERE id = ?";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting app: " + e.getMessage());
        }
        return false;
    }

    // ============= User Downloads Methods =============

    public static boolean recordUserDownload(int userId, int appId) {
        String sql = "INSERT OR IGNORE INTO user_downloads (user_id, app_id) VALUES (?, ?)";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, appId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error recording download: " + e.getMessage());
            return false;
        }
    }

    public static List<App> getUserDownloadedApps(int userId) {
        List<App> apps = new ArrayList<>();
        String sql = """
                    SELECT a.*, c.name as category_name, u.full_name as developer_name
                    FROM apps a
                    JOIN user_downloads ud ON a.id = ud.app_id
                    JOIN categories c ON a.category_id = c.id
                    JOIN users u ON a.developer_id = u.id
                    WHERE ud.user_id = ?
                    ORDER BY ud.downloaded_at DESC
                """;

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                App app = new App();
                app.setId(rs.getInt("id"));
                app.setName(rs.getString("name"));
                app.setDescription(rs.getString("description"));
                app.setDeveloperId(rs.getInt("developer_id"));
                app.setCategoryId(rs.getInt("category_id"));
                app.setVersion(rs.getString("version"));
                app.setSize(rs.getLong("size"));
                app.setDownloads(rs.getInt("downloads"));
                app.setPrice(rs.getDouble("price"));
                app.setIconUrl(rs.getString("icon_url"));
                app.setPackageName(rs.getString("package_name"));
                app.setApkFilePath(rs.getString("apk_file_path"));
                app.setApproved(rs.getInt("is_approved") == 1);
                app.setCreatedAt(rs.getTimestamp("created_at"));
                app.setUpdatedAt(rs.getTimestamp("updated_at"));

                app.setCategoryName(rs.getString("category_name"));
                app.setDeveloperName(rs.getString("developer_name"));

                apps.add(app);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user downloaded apps: " + e.getMessage());
        }
        return apps;
    }
}
