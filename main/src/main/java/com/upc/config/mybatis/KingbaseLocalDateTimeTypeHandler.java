package com.upc.config.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp; // 引入 java.sql.Timestamp
import java.time.LocalDateTime;

// TypeHandler for LocalDateTime
public class KingbaseLocalDateTimeTypeHandler extends BaseTypeHandler<LocalDateTime> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        // 将 LocalDateTime 转换为 Timestamp 后再设置到 PreparedStatement
        ps.setTimestamp(i, Timestamp.valueOf(parameter));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        // 从 ResultSet 中获取 Timestamp，然后转换为 LocalDateTime
        Timestamp timestamp = rs.getTimestamp(columnName);
        return (timestamp != null) ? timestamp.toLocalDateTime() : null;
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        // 从 ResultSet 中获取 Timestamp，然后转换为 LocalDateTime
        Timestamp timestamp = rs.getTimestamp(columnIndex);
        return (timestamp != null) ? timestamp.toLocalDateTime() : null;
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        // 从 CallableStatement 中获取 Timestamp，然后转换为 LocalDateTime
        Timestamp timestamp = cs.getTimestamp(columnIndex);
        return (timestamp != null) ? timestamp.toLocalDateTime() : null;
    }
}
