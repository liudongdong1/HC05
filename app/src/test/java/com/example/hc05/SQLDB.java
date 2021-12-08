package com.example.hc05;

import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLDB {

    private static Connection connect()
    {
        Connection conn = null;//定义数据库连接对象

        try {
            String url = "jdbc:sqlite:C:\\Users\\liudongdong\\OneDrive - tju.edu.cn\\文档\\AndroidStudio\\DeviceExplorer\\oppo-pclm10-199d456c\\data\\data\\com.example.hc05\\databases\\flexDb.sqlite3";   //定义连接数据库的url(url:访问数据库的URL路径),test为数据库名称
            Class.forName("org.sqlite.JDBC");//加载数据库驱动
            conn = DriverManager.getConnection(url);    //获取数据库连接

            System.out.println("数据库连接成功！\n");//数据库连接成功输出提示
        }
        //捕获异常信息
        catch (ClassNotFoundException | SQLException e) {
            System.out.println("数据库连接失败！"+e.getMessage());
        }
        return conn;//返回一个连接
    }
    public void selectAll() {   //选择 文本区 中的所有文本。在 null 或空文档上不执行任何操作。
        String sql="Select *from table_flex";//将从表中查询到的的所有信息存入sql
        try {
            Connection conn = this.connect();
            Statement stmt = conn.createStatement();//得到Statement实例
            ResultSet rs = stmt.executeQuery(sql);//执行SQL语句返回结果集
            //输出查询到的记录的内容（表头）
            System.out.println("label"+ "\t"+"flexdata"+ "\t"+"timestamp"+ "\t");
            // 当返回的结果集不为空时，并且还有记录时，循环输出记录
            while (rs.next()) {
                //输出获得记录中的"name","sex","age"字段的值
                System.out.println(rs.getString("label") + "\t" + rs.getString("flexdata")+ "\t" +rs.getInt("timestamp"));
            }
        }
        catch (SQLException e) {
            System.out.println("查询数据时出错！"+e.getMessage());
        }
    }
    //定义一个main方法
    @Test
    public void test_connection(){
        selectAll();
    }
}
