package com.hww.house.util.jdbc;

import com.alibaba.fastjson.JSONObject;
import com.hww.house.exception.HouseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/5
 * @Time: 19:41
 * Description: 数据库操作工具类
 */
@Component
@Slf4j
public class DataProcessUtil {

    @Value("${spring.datasource.driver-class-name}")
    private String sDbDriver;

    @Value("${spring.datasource.url}")
    private String sDbUrl;

    @Value("${spring.datasource.username}")
    private String sDbUsername;

    @Value("${spring.datasource.password}")
    private String sDbPasssword;

    @Autowired
    private DataSource sDataSource;


    private static String dbDriver;
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPasssword;

    private static DataSource dataSource;

    @PostConstruct
    public void transValues() {
        dbDriver = this.sDbDriver;
        dbUrl = this.sDbUrl;
        dbUsername = this.sDbUsername;
        dbPasssword = this.sDbPasssword;
        dataSource = this.sDataSource;
        log.info("------------datasource's config info as follows:----------------------");
        log.info("database from ： "+dataSource.getClass().getName());
        System.out.println("datasource pool is: "+dataSource);
        log.info("driver ： "+dbDriver);
        log.info("jdbcUrl ： "+dbUrl);
        log.info("username ： "+dbUsername);
        log.info("password ： "+dbPasssword);
    }

    /**
     * 读取数据库字段列表信息
     *
     * @param tableName 需要读取的表名
     */
    public static List<DataMetaType> findDatabaseColumns(String tableName) {
        if (null == tableName || "".equals(tableName.trim())) {
            throw new HouseException("请输入要读取的表名");
        }
        //执行sql 获取metadata信息
        String sql = "select * from " + tableName;
        Connection connection = null;
        PreparedStatement pStatement = null;
        ResultSet rSet = null;

        List<DataMetaType> metaTypes = new ArrayList<>();
        try {
            connection = getConnection();
            pStatement = connection.prepareStatement(sql);
            rSet = pStatement.executeQuery();
            ResultSetMetaData rs = rSet.getMetaData();
            for (int i = 0; i < rs.getColumnCount(); i++) {
                DataMetaType metaType = new DataMetaType();
                metaType.setColumName(rs.getColumnName(i + 1));
                metaType.setColumnType(rs.getColumnTypeName(i + 1));
                metaType.setDataType(rs.getColumnType(i + 1));
                metaType.setColumnLength(rs.getColumnDisplaySize(i + 1));
                metaTypes.add(metaType);
            }
        } catch (Exception e) {
            log.info("获取数据库字段信息出错{}", e.getMessage());
        } finally {
            close(connection, pStatement, null, rSet);
        }
        return metaTypes;
    }

    /**
     * 读取数据库字段列表信息，简化列名去掉下划线
     *
     * @param tableName 需要读取的表名
     */
    public static List<DataMetaType> findDatabaseSimpleColumns(String tableName) {
        if (null == tableName || "".equals(tableName.trim())) {
            throw new HouseException("请输入要读取的表名");
        }
        List<DataMetaType> metaTypes = new ArrayList<>();

        try {
            metaTypes = findDatabaseColumns(tableName);
            if (metaTypes == null || metaTypes.size() < 1) {
                return new ArrayList<>();
            }
            for (DataMetaType dataMetaType : metaTypes) {
                dataMetaType.setColumName(dataMetaType.getColumName().replaceAll("_", ""));
            }
        } catch (Exception e) {
            log.info("获取数据库字段信息出错{}", e.getMessage());
        }
        return metaTypes;
    }

    /**
     * 读取数据库字段列表信息
     *
     * @param tableName 需要读取的表名
     */
    public static List<DataMetaType> findDatabaseOrignColumns(String tableName) {
        if (null == tableName || "".equals(tableName.trim())) {
            throw new HouseException("请输入要读取的表名");
        }
        List<DataMetaType> metaTypes = new ArrayList<>();
        try {
            metaTypes = findDatabaseColumns(tableName);
            if (metaTypes == null || metaTypes.size() < 1) {
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.info("获取数据库字段信息出错{}", e.getMessage());
        }
        return metaTypes;
    }

    /**
     * 获取数据库连接
     *
     * @return
     * @throws Exception
     */
    public static Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (Exception e) {
            log.info("获取是数据连接失败{}", e.getMessage());
            throw new HouseException("获取是数据连接失败");
        }

    }


    /**
     * 关闭数据库连接
     *
     * @param conn
     * @param pstmt
     * @param stmt
     * @param rs
     */
    public static void close(Connection conn, PreparedStatement pstmt, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            log.error("关闭连接错误{}", e.getMessage());
            throw new HouseException("关闭连接错误");
        }
    }

    /**
     * 获取统计数据的信息
     *
     * @param sql
     * @return
     */
    public static Integer getOneCount(String sql) {
        if (StringUtils.isEmpty(sql)) {
            throw new HouseException("SQL不能为空");
        }
        Integer count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (Exception e) {
            log.error("获取统计数据的信息错误{}", e.getMessage());
            throw new HouseException("获取统计数据的信息错误");
        } finally {
            close(conn, pstmt, null, rs);
        }
        return count;
    }


    /**
     * 获取动态获取数据信息
     *
     * @param sql
     * @return
     */
    public static List<Map<String, String>> getDataInfo(String sql, boolean isHump) {
        if (StringUtils.isEmpty(sql)) {
            throw new HouseException("SQL不能为空");
        }
        List<Map<String, String>> resultList = new ArrayList<>();
        Map<String, String> resultMap = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            ResultSetMetaData rsMetaData = rs.getMetaData();
            while (rs.next()) {
                resultMap = new HashMap<>();
                for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
                    resultMap.put(rsMetaData.getColumnName(i + 1), rs.getString(i + 1));
                }
                resultList.add(resultMap);
            }
        } catch (Exception e) {
            log.error("获取数据错误{}", e.getMessage());
            throw new HouseException("获取数据错误");
        } finally {
            close(conn, pstmt, null, rs);
        }
        return resultList;
    }

    /**
     * 从数据库查询一条记录
     *
     * @param sql
     * @return
     */
    public static Map<String, String> getDataInfoSingle(String sql) {
        if (StringUtils.isEmpty(sql)) {
            throw new HouseException("SQL不能为空");
        }
        Map<String, String> resultMap = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            ResultSetMetaData rsMetaData = rs.getMetaData();
            while (rs.next()) {
                resultMap = new HashMap<>();
                for (int i = 0; i < rsMetaData.getColumnCount(); i++) {
                    resultMap.put(rsMetaData.getColumnName(i + 1), rs.getString(i + 1));
                }
            }
        } catch (Exception e) {
            log.error("获取数据出错{}", e.getMessage());
            throw new HouseException("获取数据出错");
        } finally {
            close(conn, pstmt, null, rs);
        }
        return resultMap;
    }

    /**
     * 根据字段动态更新单条数据
     *
     * @param jsonObject
     * @param tableName
     * @param whereCondition
     * @return
     */
    public static Boolean executeDynaicUpdate(JSONObject jsonObject, String tableName, String whereCondition) {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            StringBuilder params = new StringBuilder(" ");
            for (Map.Entry entry : jsonObject.entrySet()) {
               /* if ("idCard".equals(entry.getKey())) {
                    continue;
                }*/
                if (entry.getKey() != null && entry.getValue() != null && StringUtils.isNotEmpty(entry.getValue().toString())) {
                    params.append(entry.getKey().toString()).append("= '").append(entry.getValue()).append("',");
                }
            }
            if (params.length() > 0) {
                params.deleteCharAt(params.length() - 1);
            }
            StringBuilder sql = new StringBuilder("UPDATE ");
            sql.append(tableName).append(" SET").append(params).append(whereCondition);
            pstmt = conn.prepareStatement(sql.toString());
            count = pstmt.executeUpdate();
        } catch (Exception e) {
            log.error("更新数据出错{}", e.getMessage());
            throw new HouseException("更新数据出错");
        } finally {
            DataProcessUtil.close(conn, pstmt, null, null);
        }
        return count > 0 ? true : false;
    }



    /**
     * 执行一条sql语句没有返回值
     *
     * @param sql
     */
    public static void executeSingleSql(String sql) {
        Connection connection = getConnection();
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.execute();
        } catch (SQLException e) {
            log.error("执行sql语句出错{}", e.getMessage());
            throw new HouseException("执行sql语句出错");
        } finally {
            DataProcessUtil.close(connection, preparedStatement, null, null);
        }
    }

    public static void main(String[] args) {

    }

}
