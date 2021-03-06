package com.codecool.web.dao;

import com.codecool.web.model.Coupon;

import java.sql.SQLException;
import java.util.List;

public interface CouponDao {

    List<Coupon> findAllByUserAndShopId(int user_id, int shop_id) throws SQLException;

    List<Coupon> findAllByUserId(int user_id) throws SQLException;

    List<Coupon> findAll() throws SQLException;

    Coupon findById(int id) throws SQLException;

    Coupon add(String name, int percentage) throws SQLException;

    void add(int couponId, int... shopIds) throws SQLException;
}
