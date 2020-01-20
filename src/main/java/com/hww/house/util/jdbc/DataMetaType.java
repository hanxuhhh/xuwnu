package com.hww.house.util.jdbc;


/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/5
 * @Time: 19:41
 * Description: 字段类型信息
 */
public class DataMetaType {

    /**
     * 字段名称
     */
    private String columName;
    /**
     * 字段类型 (java.sql.types)
     */
    private Integer dataType;
    /**
     * 对应的数据库类型
     */
    private String columnType;
    
    /*
     * 对应别名信息（自定义SQL）
     */
    private String columnAlias;
    /*
     * 对应表别名信息（自定义SQL）
     */
    private String tableAlias;

    /*
     * 对应数据库表字段设定的长度
     */
    private Integer columnLength;

    /*
     * 对应数据库column_comment
     */
    private String columnComment;

    public DataMetaType() {

    }

    public DataMetaType(String columName, String columnType) {
        this.columName = columName;
        this.columnType = columnType;
    }

    public String getColumName() {
        return columName;
    }

    public void setColumName(String columName) {
        this.columName = columName;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnAlias() {
        return columnAlias;
    }

    public void setColumnAlias(String columnAlias) {
        this.columnAlias = columnAlias;
    }
    
    public String getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(String tableAlias) {
        this.tableAlias = tableAlias;
    }

    public Integer getColumnLength() {
        return columnLength;
    }

    public void setColumnLength(Integer columnLength) {
        this.columnLength = columnLength;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }
}
