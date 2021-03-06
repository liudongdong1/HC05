package com.example.hc05;

import com.example.hc05.datamodel.FlexData;
import com.example.hc05.tools.Constant;
import com.example.hc05.tools.RandomFlex;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLDB {
    private static  String driver = null;
    private static  String url = null;
    private static  String username = null;
    private static  String password = null;

    private CallableStatement callableStatement = null;//创建CallableStatement对象
    private Connection conn = null;
    private PreparedStatement pst = null;
    private ResultSet rst = null;
   /* public SQLDB(String driver,String url) {
        this.driver = driver;
        this.url = url;
    }
    public SQLDB(String driver,String url ,String username,String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }*/
    /**
     * @function 获取数据库连接
     * */
    private static Connection getConnection()
    {
        Connection conn = null;//定义数据库连接对象

        try {
            String url = "jdbc:sqlite:C:\\Users\\liudongdong\\OneDrive - tju.edu.cn\\文档\\AndroidStudio\\DeviceExplorer\\oppo-pclm10-199d456c\\data\\data\\com.example.hc05\\databases\\flexDb.sqlite3";   //定义连接数据库的url(url:访问数据库的URL路径),test为数据库名称
            Class.forName("org.sqlite.JDBC");//加载数据库驱动
            conn = DriverManager.getConnection(url);    //获取数据库连接
            //// 获取连接
            //conn = DriverManager.getConnection(url, username,
            //       password);

            System.out.println("数据库连接成功！\n");//数据库连接成功输出提示
        }
        //捕获异常信息
        catch (ClassNotFoundException | SQLException e) {
            System.out.println("数据库连接失败！"+e.getMessage());
        }
        return conn;//返回一个连接
    }
    /**
     * @function: 添加一条flexdata的数据记录
     * @param flexData: 一条传感器数据
     * */
    public Boolean addFlexData(FlexData flexData){
        String sql="insert into table_flex(label,flexdata,timestamp)"+"values(?,?,?)";
        boolean result=false;
        try{
            conn=getConnection();
            pst=conn.prepareStatement(sql);
            pst.setString(1,flexData.getLabel());
            pst.setString(2,flexData.getStringFlexData());
            pst.setString(3,flexData.getTimestamp());
            result=pst.execute();
            return result;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            closeAll();
            return result;
        }
    }
    /**
     * @function: 批量添加flexdata的数据记录
     * @param flexDataArrayList: 一系列传感器数据
     * */
    public void addBatchFlexData(ArrayList<FlexData>flexDataArrayList){
        String sql="insert into table_flex(label,flexdata,timestamp)"+"values(?,?,?)";
        boolean result=false;
        try{
            conn=getConnection();
            pst=conn.prepareStatement(sql);
            int i=0;
            for(FlexData flexData :flexDataArrayList){
                i=i+1;
                pst.setString(1,flexData.getLabel());
                pst.setString(2,flexData.getStringFlexData());
                pst.setString(3,flexData.getTimestamp());
                pst.addBatch();
                if(i%100==0){
                    pst.executeBatch();
                    pst.clearBatch();
                }
            }
            pst.executeBatch();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            closeAll();
        }
    }
    /**
     * SQL 查询将查询结果：一行一列
     * @param wholeLabel : 完整的label
     * @return 结果集
     */
    public ArrayList<FlexData> executeQuery(String wholeLabel) {
        String sql="select * from table_flex where label=?";
        ArrayList<FlexData>arrayList=new ArrayList<FlexData>();
        try {
            // 获得连接
            conn = this.getConnection();
            // 调用SQL
            pst = conn.prepareStatement(sql);
            pst.setString(1,wholeLabel);
            // 执行
            rst = pst.executeQuery();

           while(rst.next()) {
               arrayList.add(new FlexData(rst.getString("flexdata"),rst.getString("timestamp"),rst.getString("label")));
            }
            return arrayList;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            closeAll();
            return arrayList;
        }
    }

    /**
     * SQL 模糊查询，label 数据包含Label
     * @param Label : 关键词
     * @return 结果集
     */
    public ArrayList<FlexData> executeQueryContain(String Label) {
        String sql="select * from table_flex where label like ?";
        ArrayList<FlexData>arrayList=new ArrayList<FlexData>();
        try {
            // 获得连接
            conn = getConnection();

            // 调用SQL
            pst = conn.prepareStatement(sql);
            //System.out.println("select * from table_flex where label like '%?%' ");
            pst.setString(1,"%"+Label+"%");
            //System.out.println("2select * from table_flex where label like '%?%' ");

            System.out.println(pst.getParameterMetaData());

            // 执行
            rst = pst.executeQuery();

            while(rst.next()) {
                arrayList.add(new FlexData(rst.getString("flexdata"),rst.getString("timestamp"),rst.getString("label")));
            }
            return arrayList;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("异常错误");
        } finally {
            closeAll();
            return arrayList;
        }
    }

    /**
     * 关闭所有资源
     */
    private void closeAll() {
        // 关闭结果集对象
        if (rst != null) {
            try {
                rst.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        // 关闭PreparedStatement对象
        if (pst != null) {
            try {
                pst.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        // 关闭CallableStatement 对象
        if (callableStatement != null) {
            try {
                callableStatement.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }

        // 关闭Connection 对象
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    /**
     * @function: 查询所有的数据库记录
     * */
    public void selectAll() {   //选择 文本区 中的所有文本。在 null 或空文档上不执行任何操作。
        String sql="select * from table_flex";
        try {
            Connection conn = this.getConnection();
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
    public void createTable(String sql){
        //String sql="truncate table table_flex";  //sqlite 不支持该操作
        try {
            // 获得连接
            conn = this.getConnection();
            // 调用SQL
            pst = conn.prepareStatement(sql);
            // 执行
            pst.execute();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            closeAll();
        }
    }
    /**
     * @function: 清空表操作
     * */
    public int deleteFlexTableContain(String label){
        String sql="delete from table_flex where label like ?";   //没有* 号
        //String sql="truncate table table_flex";  //sqlite 不支持该操作
        try {
            // 获得连接
            conn = this.getConnection();
            // 调用SQL
            pst = conn.prepareStatement(sql);

            //System.out.println("select * from table_flex where label like '%?%' ");
            pst.setString(1,"%"+label+"%");
            //System.out.println("2select * from table_flex where label like '%?%' ");

            System.out.println(pst.getParameterMetaData());

            // 执行
            return pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            closeAll();
            return 0;
        }
    }
    /**
     * @function: 清空表操作
     * */
    public int deleteFlexTable(){
        String sql="delete from table_flex";   //没有* 号
        //String sql="truncate table table_flex";  //sqlite 不支持该操作
        try {
            // 获得连接
            conn = this.getConnection();
            // 调用SQL
            pst = conn.prepareStatement(sql);
            // 执行
            return pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            closeAll();
            return 0;
        }
    }
    //定义一个main方法
    @Test
    public void test_connection(){
        //selectAll();
        /*RandomFlex randomFlex=RandomFlex.getSingleton();
        ArrayList<FlexData>flexDataArrayList=new ArrayList<>();
        for(int i=0;i<110;i++){
            FlexData flexData=new FlexData(randomFlex.getFlexFakeData(),new Date(),"LD_D_1");
            System.out.println("flexWindow_test"+i+"\t"+flexData.toString());
            flexDataArrayList.add(flexData);
        }
        addBatchFlexData(flexDataArrayList);*/
        //deleteFlexTable();
        createTable(Constant.SQL_Result);
        createTable(Constant.SQL);
        /*deleteFlexTableContain("ten");
        ArrayList<FlexData>arrayList=executeQueryContain("ten");
        for(FlexData flexData: arrayList){
            System.out.println(flexData.toString());
        }*/
    }
    public ArrayList<FlexData> getFlexFromTXT(String filename,String description) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        ArrayList<FlexData>arrayList=new ArrayList<FlexData>();
        while((line = br.readLine())!=null) {
            if(line.split(",").length==5){
                line=line.replace(",",";");
                //System.out.println(line);
                arrayList.add(new FlexData(line,new Date(),description));
            }

        }
        return arrayList;
    }
    private void ListAll(File dir) {
        //获取指定目录下当前的所有文件或文件夹对像
        File[] files = dir.listFiles();
        for (File file : files){
            System.out.println(file.getAbsolutePath());
            if (file.isDirectory()){
                ListAll(file);
            } else {
                System.out.println(file.getAbsolutePath());
            }
        }
    }
    public void saveToSQL(String filepath,String type) throws IOException {
        File basefolder=new File(filepath);
        File[] files=basefolder.listFiles();
        for(File file1:files){
            String label=file1.getAbsolutePath();
            int i=0;
            for(File file: new File(label).listFiles()){
                String filename=file.getAbsolutePath();
                ArrayList<FlexData>arrayList=new ArrayList<FlexData>();
                String[] temp=filename.split("\\\\");
                String description="NULL";
                if(type=="D"){
                    description=String.format("LDD_D_%s_%s",temp[temp.length-2],temp[temp.length-1].split("\\.")[0]);
                }else{
                    description=String.format("LDD_C_%s_%d",temp[temp.length-2],i);
                    i=i+1;
                }
                System.out.println(description);
                arrayList=getFlexFromTXT(filename,description);
                System.out.println("读取文件"+description+" 大小=："+arrayList.size()+"文件名："+filename);
                addBatchFlexData(arrayList);
                arrayList.clear();
            }
        }
    }
    @Test
    public void test_listdir() throws IOException {
        //String filename="D:\\work_OneNote\\OneDrive - tju.edu.cn\\文档\\work_组会比赛\\数据手套\\DashBoard\\flexdata\\digit\\digitFlex_7days";
        //ListAll(new File(filename));
        String filename="D:\\work_OneNote\\OneDrive - tju.edu.cn\\文档\\work_组会比赛\\数据手套\\DashBoard\\flexdata\\digit\\digitFlex_7days";
        saveToSQL(filename,"D");
        filename="D:\\work_OneNote\\OneDrive - tju.edu.cn\\文档\\work_组会比赛\\数据手套\\DashBoard\\flexdata\\chars\\charFlex\\26char";
        saveToSQL(filename,"C");
    }
    @Test
    public void test_txtDataRead() throws IOException {
        //selectAll();
        /*RandomFlex randomFlex=RandomFlex.getSingleton();
        ArrayList<FlexData>flexDataArrayList=new ArrayList<>();
        for(int i=0;i<110;i++){
            FlexData flexData=new FlexData(randomFlex.getFlexFakeData(),new Date(),"LD_D_1");
            System.out.println("flexWindow_test"+i+"\t"+flexData.toString());
            flexDataArrayList.add(flexData);
        }*/

        ArrayList<FlexData>flexDataArrayList=new ArrayList<>();
        String filename="D:\\work_OneNote\\OneDrive - tju.edu.cn\\文档\\work_组会比赛\\数据手套\\DashBoard\\flexdata\\digit\\digitFlex_7days\\eight\\1.txt";
        flexDataArrayList=getFlexFromTXT(filename,"LDD_D_1_1");

        addBatchFlexData(flexDataArrayList);
        ArrayList<FlexData>arrayList=executeQueryContain("D_1");
        for(FlexData flexData: arrayList){
            System.out.println(flexData.toString());
        }
    }
}
