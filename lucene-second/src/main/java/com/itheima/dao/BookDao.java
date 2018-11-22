package com.itheima.dao;

import com.itheima.po.Book;

import java.util.List;

/**
 * 图书dao接口
 */
public interface BookDao {

    /**
     * 查询所有图书
     */
    List<Book> findAllBooks();
}
