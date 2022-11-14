package io.redlink.more.studymanager.utils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RepositoryUtils {
    public static Integer getValidNullableIntegerValue(ResultSet rs, String strColName) throws SQLException {
        int nValue = rs.getInt(strColName);
        return rs.wasNull() ? null : nValue;
    }
}
