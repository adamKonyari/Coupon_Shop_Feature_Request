package com.codecool.web.dao.database;

import com.codecool.web.dao.CouponDao;
import com.codecool.web.model.Coupon;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseCouponDao extends AbstractDao implements CouponDao {

    public DatabaseCouponDao(Connection connection) {
        super(connection);
    }

    public List<Coupon> findAllByUserAndShopId(int user_id, int shop_id) throws SQLException {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c. percentage " +
                "FROM coupons AS c " +
                "JOIN users_coupons AS uc ON c.id = uc.coupon_id " +
                "JOIN users AS u ON u.id = uc.user_id " +
                "JOIN coupons_shops AS cs ON cs.coupon_id = c.id " +
                "WHERE cs.shop_id = " + shop_id + "AND u.id = " + user_id;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                coupons.add(fetchCoupon(resultSet));
            }
        }
        return coupons;
    }

    public List<Coupon> findAllByUserId(int user_id) throws SQLException {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c. percentage " +
                "FROM coupons AS c " +
                "JOIN users_coupons AS uc ON c.id = uc.coupon_id " +
                "JOIN users AS u ON u.id = uc.user_id " +
                "WHERE u.id = " + user_id;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                coupons.add(fetchCoupon(resultSet));
            }
        }
        return coupons;
    }

    @Override
    public List<Coupon> findAll() throws SQLException {
        List<Coupon> coupons = new ArrayList<>();
        String sql = "SELECT id, name, percentage FROM coupons";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                coupons.add(fetchCoupon(resultSet));
            }
        }
        return coupons;
    }

    @Override
    public Coupon findById(int id) throws SQLException {
        String sql = "SELECT id, name, percentage FROM coupons WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return fetchCoupon(resultSet);
                }
            }
        }
        return null;
    }

    @Override
    public Coupon add(String name, int percentage) throws SQLException {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        String sql = "INSERT INTO coupons (name, percentage) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setInt(2, percentage);
            executeInsert(statement);
            int id = fetchGeneratedId(statement);
            connection.commit();
            return new Coupon(id, name, percentage);
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    @Override
    public void add(int couponId, int... shopIds) throws SQLException {
        boolean autoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        String sql = "INSERT INTO coupons_shops (coupon_id, shop_id) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, couponId);
            for (int shopId : shopIds) {
                statement.setInt(2, shopId);
                executeInsert(statement);
            }
            connection.commit();
        } catch (SQLException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(autoCommit);
        }
    }

    private Coupon fetchCoupon(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        int percentage = resultSet.getInt("percentage");
        return new Coupon(id, name, percentage);
    }
}
