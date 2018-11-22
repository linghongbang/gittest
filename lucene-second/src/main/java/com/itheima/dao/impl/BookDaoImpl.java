package com.itheima.dao.impl;

import com.itheima.dao.BookDao;
import com.itheima.po.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书dao实现类
 */
public class BookDaoImpl implements BookDao{

    Connection conn =null;
    PreparedStatement pstat =null;
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
           conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/lucene","root","root");
            //定义mysql语句
            String sql = "select * from lucene";
            //获取statement对象
            pstat = conn.prepareStatement(sql);
            //设置参数
            //执行
            rs = pstat.executeQuery();
            //处理结果集
            while (rs.next()){
                //创建Book对象
                Book book = new Book();

                //获取id
                book.setId(rs.getInt("id"));
                //获取图书名称
                book.setBookname(rs.getString("bookname"));
                //获取价格
                book.setPrice(rs.getFloat("price"));
                //获取图片
                book.setPic(rs.getString("pic"));
                //获取描述
                book.setBookdesc(rs.getString("bookdesc"));

                bookList.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
                try {
                    //释放资源
                    if (rs !=null) rs.close();
                    if (pstat != null) pstat.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
        return bookList;
    }
}
