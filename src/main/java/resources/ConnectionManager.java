
package resources;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ConnectionManager
        extends FormatManager {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private int timeout = 0;

    private void verifyState()
            throws SQLException {
        if (this.resultSet != null) {
            this.resultSet.close();
        }
        if (this.statement != null) {
            this.statement.close();
        }
    }

    public ConnectionManager(DataSource datasource) {
        try {
            this.connection = datasource.getConnection();
            this.connection.setAutoCommit(true);
        } catch (SQLException e) {
            registerError("ConnectionManager(DataSource datasource)", e);
        }
    }

    public ConnectionManager(String poolName) {
        try {
            if (System.getProperties().containsKey("jetty.base")) {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                this.connection = DriverManager.getConnection("jdbc:mysql://google/" + getEnviromentVariable(new StringBuilder().append(poolName).append("Database").toString()) + "?cloudSqlInstance=" + getEnviromentVariable(new StringBuilder().append(poolName).append("Server").toString()) + "&socketFactory=com.google.cloud.sql.mysql.SocketFactory&user=" + getEnviromentVariable(new StringBuilder().append(poolName).append("User").toString()) + "&password=" + getEnviromentVariable(new StringBuilder().append(poolName).append("Password").toString()) + "&useSSL=false");
                this.connection.setAutoCommit(true);
            } else {
                Context context = (Context) new InitialContext().lookup("java:/jdbc/");
                DataSource datasource = (DataSource) context.lookup(poolName);
                this.connection = datasource.getConnection();
                this.connection.setAutoCommit(true);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException | NamingException e) {
            registerError("ConnectionManager(String poolName)", e);
        }
    }

    public ConnectionManager(String driver, String URL, String user, String password) {
        try {
            Class.forName(driver).newInstance();
            this.connection = DriverManager.getConnection(URL, user, password);
            this.connection.setAutoCommit(true);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException e) {
            registerError("ConnectionManager(String URL, String user, String password)", e);
        }
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public boolean executeQuery(String sql) {
        boolean b = false;
        try {
            verifyState();
            this.statement = this.connection.createStatement(1003, 1007);
            if (this.timeout > 0) {
                this.statement.setQueryTimeout(this.timeout);
            }
            this.resultSet = this.statement.executeQuery(sql);
            b = true;
        } catch (SQLException e) {
            registerError("executeQuery", e);
            registerIntoLog("sentence", sql);
        }
        return b;
    }

    public boolean execute(String sql) {
        return execute(sql, true);
    }

    public boolean execute(String sql, boolean upperCase) {
        boolean b = false;
        try {
            verifyState();
            this.statement = this.connection.createStatement();
            if (this.timeout > 0) {
                this.statement.setQueryTimeout(this.timeout);
            }
            this.statement.execute(upperCase ? sql.toUpperCase() : sql);
            b = true;
        } catch (SQLException e) {
            registerError("execute", e);
            registerIntoLog("sentence", sql);
        }
        return b;
    }

    public boolean executeUpdate(String sql) {
        return executeUpdate(sql, false);
    }

    public boolean executeUpdate(String sql, boolean upperCase) {
        boolean b = false;
        try {
            verifyState();
            this.statement = this.connection.createStatement();
            if (this.timeout > 0) {
                this.statement.setQueryTimeout(this.timeout);
            }
            this.statement.executeUpdate(upperCase ? sql.toUpperCase() : sql);
            b = true;
        } catch (SQLException e) {
            registerError("execute", e);
            registerIntoLog("sentence", sql);
        }
        return b;
    }

    public boolean executeBatch(String sql) {
        return executeBatch(sql, ";", true);
    }

    public boolean executeBatch(String sql, boolean upperCase) {
        return executeBatch(sql, ";", upperCase);
    }

    public boolean executeBatch(String sql, String batchSeparetor, boolean upperCase) {
        boolean b = false;
        try {
            verifyState();
            String[] queries = sql.split(";");
            this.statement = this.connection.createStatement();
            if (this.timeout > 0) {
                this.statement.setQueryTimeout(this.timeout);
            }
            for (String querie : queries) {
                this.statement.addBatch(upperCase ? querie.toUpperCase() : querie);
            }
            this.statement.executeBatch();
            b = true;
        } catch (SQLException e) {
            registerError("execute", e);
            registerIntoLog("sentence", sql);
        }
        return b;
    }

    public ResultSetMetaData getResultSetMetaData()
            throws SQLException {
        return this.resultSet.getMetaData();
    }

    public HashMap<String, String> getResultSetData()
            throws SQLException {
        HashMap<String, String> data = new HashMap();
        ResultSetMetaData metaData = this.resultSet.getMetaData();
        for (int c = 1; c <= metaData.getColumnCount(); c++) {
            data.put(metaData.getColumnLabel(c).toLowerCase(), this.resultSet.getString(c));
        }
        return data;
    }

    public boolean close() {
        boolean b = false;
        try {
            if (this.resultSet != null) {
                this.resultSet.close();
            }
            if (this.statement != null) {
                this.statement.close();
            }
            if (this.connection != null) {
                this.connection.close();
            }
            b = true;
        } catch (SQLException e) {
            registerError("close", e);
        }
        return b;
    }

    public boolean next() {
        boolean b = false;
        try {
            b = this.resultSet.next();
        } catch (SQLException e) {
            registerError("next", e);
        }
        return b;
    }

    public boolean wasNull() {
        try {
            return this.resultSet.wasNull();
        } catch (SQLException localException) {
        }
        return true;
    }

    public boolean getBoolean(int index) {
        boolean b = false;
        try {
            return this.resultSet.getBoolean(index);
        } catch (SQLException e) {
            registerError("getBoolean(" + index + ")", e);
        }
        return b;
    }

    public boolean getBoolean(String name) {
        boolean b = false;
        try {
            return this.resultSet.getBoolean(name);
        } catch (SQLException e) {
            registerError("getBoolean(" + name + ")", e);
        }
        return b;
    }

    public Date getDate(int index) {
        try {
            return this.resultSet.getDate(index);
        } catch (SQLException e) {
            registerError("getDate(" + index + ")", e);
        }
        return Date.valueOf("1900-01-01");
    }

    public Date getDate(String name) {
        try {
            return this.resultSet.getDate(name);
        } catch (SQLException e) {
            registerError("getDate(" + name + ")", e);
        }
        return Date.valueOf("1900-01-01");
    }

    public String getDateLabel(String name) {
        String s = getString(name);
        try {
            if (s.substring(0, 4).equalsIgnoreCase("1900")) {
                return "----";
            }
            return s.substring(8, 10) + "-" + s.substring(5, 7) + "-" + s.substring(0, 4);
        } catch (Exception e) {
            registerError("getDate(" + name + ")", e);
        }
        return "";
    }

    public String getDateTimeLabel(String name) {
        String s = getString(name);
        try {
            if (s.substring(0, 4).equalsIgnoreCase("1900")) {
                return "----";
            }
            return s.substring(8, 10) + "-" + s.substring(5, 7) + "-" + s.substring(0, 4) + " " + s.substring(11, 13) + ":" + s.substring(14, 16);
        } catch (Exception e) {
            registerError("getDate(" + name + ")", e);
        }
        return "";
    }

    public String getTimeLabel(String name) {
        String s = getString(name);
        try {
            return s.substring(11, 13) + ":" + s.substring(14, 16);
        } catch (Exception e) {
            registerError("getDate(" + name + ")", e);
        }
        return "";
    }

    public double getDouble(int index) {
        try {
            return this.resultSet.getDouble(index);
        } catch (SQLException e) {
            registerError("getDouble(" + index + ")", e);
        }
        return 0.0D;
    }

    public double getDouble(String name) {
        try {
            return this.resultSet.getDouble(name);
        } catch (SQLException e) {
            registerError("getDouble(" + name + ")", e);
        }
        return 0.0D;
    }

    public int getInt(int index) {
        try {
            return this.resultSet.getInt(index);
        } catch (SQLException e) {
            registerError("getInt(" + index + ")", e);
        }
        return 0;
    }

    public int getInt(String name) {
        try {
            return this.resultSet.getInt(name);
        } catch (SQLException e) {
            registerError("getInt(" + name + ")", e);
        }
        return 0;
    }

    public long getLong(int index) {
        try {
            return this.resultSet.getLong(index);
        } catch (SQLException e) {
            registerError("getLong(" + index + ")", e);
        }
        return 0L;
    }

    public long getLong(String name) {
        try {
            return this.resultSet.getLong(name);
        } catch (SQLException e) {
            registerError("getLong(" + name + ")", e);
        }
        return 0L;
    }

    public String getString(int index) {
        try {
            return this.resultSet.getString(index);
        } catch (SQLException e) {
            registerError("getString(" + index + ")", e);
        }
        return "";
    }

    public String getString(String name) {
        try {
            return this.resultSet.getString(name);
        } catch (SQLException e) {
            registerError("getString(" + name + ")", e);
        }
        return "";
    }

    public Time getTime(int index) {
        try {
            return this.resultSet.getTime(index);
        } catch (SQLException e) {
            registerError("getTime(" + index + ")", e);
        }
        return Time.valueOf("00:00:00");
    }

    public Time getTime(String name) {
        try {
            return this.resultSet.getTime(name);
        } catch (SQLException e) {
            registerError("getTime(" + name + ")", e);
        }
        return Time.valueOf("00:00:00");
    }

    public Timestamp getTimestamp(int index) {
        try {
            return this.resultSet.getTimestamp(index);
        } catch (SQLException e) {
            registerError("getTimestamp(" + index + ")", e);
        }
        return Timestamp.valueOf("1900-00-00 00:00:00");
    }

    public Timestamp getTimestamp(String name) {
        try {
            return this.resultSet.getTimestamp(name);
        } catch (SQLException e) {
            registerError("getTimestamp(" + name + ")", e);
        }
        return Timestamp.valueOf("1900-00-00 00:00:00");
    }

    public boolean beginTransaction() {
        boolean b = false;
        try {
            this.connection.setAutoCommit(false);
            b = true;
        } catch (SQLException e) {
            registerError("beginTransaction", e);
        }
        return b;
    }

    public int getYear(int index) {
        try {
            return Integer.parseInt(this.resultSet.getString(index).substring(0, 4));
        } catch (SQLException e) {
            registerError("getYear(" + index + ")", e);
        }
        return 0;
    }

    public int getYear(String name) {
        try {
            return Integer.parseInt(this.resultSet.getString(name).substring(0, 4));
        } catch (SQLException e) {
            registerError("getYear(" + name + ")", e);
        }
        return 0;
    }

    public int getMonth(int index) {
        try {
            return Integer.parseInt(this.resultSet.getString(index).substring(5, 5));
        } catch (SQLException e) {
            registerError("getMonth(" + index + ")", e);
        }
        return 0;
    }

    public int getMonth(String name) {
        try {
            return Integer.parseInt(this.resultSet.getString(name).substring(5, 7));
        } catch (SQLException e) {
            registerError("getMonth(" + name + ")", e);
        }
        return 0;
    }

    public int getDay(int index) {
        try {
            return Integer.parseInt(this.resultSet.getString(index).substring(8, 10));
        } catch (SQLException e) {
            registerError("getDay(" + index + ")", e);
        }
        return 0;
    }

    public int getDay(String name) {
        try {
            return Integer.parseInt(this.resultSet.getString(name).substring(8, 10));
        } catch (SQLException e) {
            registerError("getDay(" + name + ")", e);
        }
        return 0;
    }

    public boolean commit() {
        boolean b = false;
        try {
            this.connection.commit();
            b = true;
        } catch (SQLException e) {
            registerError("commit", e);
        }
        return b;
    }

    public boolean rollback() {
        boolean b = false;
        try {
            this.connection.rollback();
            b = true;
        } catch (SQLException e) {
            registerError("rollback", e);
        }
        return b;
    }
    
     public static String getBDCPoolName() {
        return "sicopbdc";
    }

    public static String getBDCDatabaseName() {
        return "sicopbdc";
    }

    public static DataSource getDatasource(String poolName) {
        DataSource datasource = null;
        try {
            Context jdbc = (Context) new InitialContext().lookup("java:/jdbc/");
            datasource = (DataSource) jdbc.lookup(poolName);
        } catch (NamingException e) {

        }
        return datasource;
    }

}
