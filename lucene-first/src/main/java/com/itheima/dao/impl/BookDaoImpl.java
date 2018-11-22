package com.itheima.dao.impl;

import com.itheima.dao.BookDao;
import com.itheima.po.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书借口实现类
 */
public class BookDaoImpl implements BookDao{

    Connection conn =null;
    PreparedStatement psta =null;
    ResultSet rs =null;
    /**
     * 查询所有图书
     */
    public List<Book> findAllBooks() {

        //创建图书集合
        List<Book> bookList = new ArrayList<Book>();

        try {
            //创建mysql驱动
            Class.forName("com.mysql.jdbc.Driver");
            //获取连接对象
             conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/lucene", "root", "root");
            //定义mysql语句
            String sql = "select * from book";
            //创建statement对象
             psta = conn.prepareStatement(sql);
            //设置参数
            //无参数
            //执行
            rs = psta.executeQuery();
            //处理结果集
            while (rs.next()){

                //创建图书对象
                Book book= new Book();

                book.setId(rs.getInt("id"));
                book.setBookname(rs.getString("bookname"));
                book.setPic(rs.getString("pic"));
                book.setBookdesc(rs.getString("bookdesc"));
                book.setPrice(rs.getFloat("price"));

                //添加到集合
                bookList.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
                try {
                    //释放资源
                    if (rs != null) rs.close();
                    if (psta != null) psta.close();
                    if (conn != null) conn.close();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
        return bookList;
    }
}
