package com.lixin.fruit.dao.base;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//v2.0新建BaseDAO抽象类
public abstract class BaseDAO<T> {

    protected Connection conn;
    protected PreparedStatement prmt;
    protected ResultSet rs;

    public final String DRIVER = "com.mysql.cj.jdbc.Driver";
    public final String URL = "jdbc:mysql://localhost:3306/fruitdb?useUnicode=true&characterEncoding=utf-8";
    public final String USER = "root";
    public final String PWD = "123456";

    private Class entityClass;

    //v2.0为了抽取查询的方法,新增构造方法,为了知道泛型T的类型,这里是Fruit
    public BaseDAO() {
        /*
         * 1.getClass()获取当前Class对象->当前我们执行的是new FruitDAOImpl(),创建的是FruitDAOImpl的Class;
         * 2.子类构造方法内部会首先调用父类(BaseDAO)的无参构造方法(这里);
         * 3.因此此处的getClass()会被执行,但是getClass获取的是FruitDAOImpl的Class;
         * 4.所以getGenericSuperclass()获取到的是FruitDAOImpl的父类BaseDAO的Class;
         */
        Type genericType = getClass().getGenericSuperclass();
        //ParameterizedType参数化类型->泛型里面的参数<T1,T2,...>
        Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();
        //获取到<T>中的真实类型,这里只有一个T->Fruit
        Type actualType = actualTypeArguments[0];
        try {
            entityClass = Class.forName(actualType.getTypeName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    //v2.0抽取获取连接的方法
    private Connection getConn() {
        try {
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PWD);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    //v2.0抽取释放资源的方法
    private void close(Connection conn, PreparedStatement prmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (prmt != null) {
                prmt.close();
            }
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //v2.0抽取给预处理命令对象(prepareStatement)设置参数的方法->sql语句中留出的?
    private void setParams(PreparedStatement prmt, Object... params) throws SQLException {
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                prmt.setObject(i + 1, params[i]);
            }
        }
    }

    //v2.0抽取执行更新(增删改)的方法,返回影响行数
    protected int executeUpdate(String sql, Object... params) {
        boolean insertFlag = sql.toUpperCase().trim().startsWith("INSERT");
        try {
            conn = getConn();
            //v3.0新增自增主键
            if (insertFlag) {
                prmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                prmt = conn.prepareStatement(sql);
            }
            setParams(prmt, params);
            int count = prmt.executeUpdate();
            //如果是INSERT,返回自增列的值,否则返回影响行数
            if (insertFlag) {
                rs = prmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, prmt, rs);
        }
        return 0;
    }

    //v2.0抽取给obj对象设置property属性值的方法
    private void setValue(Object obj, String property, Object propertyValue) {
        try {
            Class clazz = obj.getClass();
            Field field = clazz.getDeclaredField(property);
            field.setAccessible(true);
            field.set(obj, propertyValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    //v2.0抽取执行查询的方法,返回单个实体对象
    protected T load(String sql, Object... params) {
        try {
            conn = getConn();
            prmt = conn.prepareStatement(sql);
            setParams(prmt, params);
            rs = prmt.executeQuery();
            //通过rs可以获取结果集的元数据:元数据即为描述结果集数据的数据,简单说,就是这个结果集有多少列,每列的类型等等...
            ResultSetMetaData rsmd = rs.getMetaData();
            //获取结果集的列数
            int columnCount = rsmd.getColumnCount();
            if (rs.next()) {
                T entity = (T) entityClass.newInstance();
                for (int i = 0; i < columnCount; i++) {
                    String columnName = rsmd.getColumnName(i + 1);
                    Object columnValue = rs.getObject(i + 1);
                    setValue(entity, columnName, columnValue);
                }
                return entity;
            }
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            close(conn, prmt, rs);
        }
        return null;
    }

    //v2.0抽取执行查询的方法,返回List<T>
    protected List<T> executeQuery(String sql, Object... params) {
        List<T> list = new ArrayList<>();
        try {
            conn = getConn();
            prmt = conn.prepareStatement(sql);
            setParams(prmt, params);
            rs = prmt.executeQuery();
            //通过rs可以获取结果集的元数据:元数据即为描述结果集数据的数据,简单说,就是这个结果集有多少列,每列的类型等等...
            ResultSetMetaData rsmd = rs.getMetaData();
            //获取结果集的列数
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                T entity = (T) entityClass.newInstance();
                for (int i = 0; i < columnCount; i++) {
                    String columnName = rsmd.getColumnName(i + 1);
                    Object columnValue = rs.getObject(i + 1);
                    setValue(entity, columnName, columnValue);
                }
                list.add(entity);
            }
        } catch (SQLException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            close(conn, prmt, rs);
        }
        return list;
    }

    protected Object[] executeComplexQuery(String sql, Object... params) {
        try {
            conn = getConn();
            prmt = conn.prepareStatement(sql);
            setParams(prmt, params);
            rs = prmt.executeQuery();
            //通过rs可以获取结果集的元数据:元数据即为描述结果集数据的数据,简单说,就是这个结果集有多少列,每列的类型等等...
            ResultSetMetaData rsmd = rs.getMetaData();
            //获取结果集的列数
            int columnCount = rsmd.getColumnCount();
            Object[] columnValueArr = new Object[columnCount];
            if (rs.next()) {
                for (int i = 0; i < columnCount; i++) {
                    Object columnValue = rs.getObject(i + 1);
                    columnValueArr[i] = columnValue;
                }
                return columnValueArr;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, prmt, rs);
        }
        return null;
    }

}