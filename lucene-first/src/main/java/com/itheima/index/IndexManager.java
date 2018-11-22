package com.itheima.index;

import com.itheima.dao.BookDao;
import com.itheima.dao.impl.BookDaoImpl;
import com.itheima.po.Book;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 索引管理类
 *      扩展词库：就是把哪几个字连在一起变成索引
 *      停用词：就是把没意义的词(英文)放在配置文件
 */
public class IndexManager {

    //定义索引库目录位置常量
    public static final String INDEX_PATH="D:\\index";
    /**
     * 索引流程实现（添加索引模板代码）
     */
    @Test
    public void createIndex() throws Exception{
        //1.采集数据
        BookDao bookDao = new BookDaoImpl();
        List<Book> bookList = bookDao.findAllBooks();

        //2.建立文档对象（Document）
        List<Document> docList = new ArrayList<Document>();
        for (Book book : bookList) {
            //创建文档对象
            Document doc = new Document();
            /**
             * 给文档对象添加域
             * add方法：添加域
             * 文本域;TextField
             * 参数：
             *      参数一：域的名称
             *      参数二：域值
             *      参数三：指定是否把域的值保存到文档对象中
             */
            /**
             * 图书Id
             是否分词：不需要分词
             是否索引：需要索引
             是否存储：需要存储

             --StringField
             */
            doc.add(new StringField("bookId",book.getId()+"", Field.Store.YES));
            /**
             * 图书名称
             是否分词：需要分词
             是否索引：需要索引
             是否存储：需要存储

             --TextField
             */
            doc.add(new TextField("bookName",book.getBookname(), Field.Store.YES));

            /**
             * 图书价格
             是否分词：（Lucene对于数值型的Fiel的，使用内部分词）
             是否索引：需要索引
             是否存储：需要存储

             --DoubleField
             */
            doc.add(new DoubleField("bookPrice",book.getPrice(), Field.Store.YES));
            /**
             * 图书图片
             是否分词：不需要分词
             是否索引：不需要索引
             是否存储：需要存储

             --StoredField
             */
            doc.add(new StoredField("bookPic",book.getPic()));
            /**
             * 图书描述
             是否分词：需要分词
             是否索引：需要索引
             是否存储：不需要存储

             --TextField
             */
            doc.add(new TextField("bookDesc",book.getBookdesc(), Field.Store.NO));

            docList.add(doc);
        }
        //3.建立分析器（分词器）对象(Analyzer)，用于分词
        Analyzer analyzer = new IKAnalyzer();
        //4.建立索引库配置对象（IndexWriterConfig），配置索引库
        /**
         * 参数：
         *      参数一：当前使用的Lucene的版本
         *      参数二：分析器对象
         */
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);
        //5.建立索引库目录对象（Directory），指定索引库的位置
        /**
         * 参数：保存文件路径
         */
       Directory directory =FSDirectory.open(new File(INDEX_PATH));

        //6.建立索引库操作对象（IndexWriter），操作索引库
        /**
         * 参数：
         *      参数一：索引库对象
         *      参数二：索引库配置对象
         */
        IndexWriter writer = new IndexWriter(directory,iwc);

        //7.使用IndexWriter，把文档对象写入索引库
        for (Document doc : docList) {
            /**
             * addDocument方法：把文档对象写入索引库
             */
            writer.addDocument(doc);
        }
        //8.释放资源
        writer.close();
    }


    /**
     * 检索流程实现（模板代码）
     */
    @Test
    public void readIndex() throws Exception{
        //1.建立分析器对象（Analyzer），用于分词
        Analyzer analyzer = new IKAnalyzer();
        //2.建立查询对象（Query）
            //2.1建立查询解析器对象
                //参数一：索引名     参数二：分词器
        QueryParser parser = new QueryParser("bookName",analyzer);
            //2.2使用查询解析器对象，解析表达式，实例化Query对象
        Query query = parser.parse("bookName:java");

        //3.建立索引库目录对象（Directory），指定索引库的位置
        Directory directory = FSDirectory.open(new File(INDEX_PATH));

        //4.建立索引读取对象（IndexReader），把索引数据读取内存中
        IndexReader reader = DirectoryReader.open(directory);

        //5.建立索引搜索对象（IndexSearcher），执行搜索，返回搜索的结果集（TopDocs）
        IndexSearcher searcher = new IndexSearcher(reader);

        /**
         * 执行搜索的方法：search
         *  参数：
         *      参数一：查询对象
         *      参数二：获取搜索结果排序后的前n个（10）
         */
        TopDocs topDocs = searcher.search(query,10);

        //6.处理结果集
            //6.1实际搜索到的结果数量
        System.out.println("实际搜索到的结果数量："+topDocs.totalHits);
            //6.2获取结果数据
        /**
         * scoreDoc中只包含两个信息:一个是当前的文档id,一个是当前文档的分值
         */
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc sd : scoreDocs) {
            System.out.println("----------------华丽丽分割线--------------");
            //取出文档id和分值
            int docId = sd.doc;
            float score = sd.score;
            System.out.println("当前文档的id:"+docId+",当前文档的分值："+score);

            //根据文档的id获取文档数据
            Document doc = searcher.doc(docId);
            System.out.println("图书Id:"+doc.get("bookId"));
            System.out.println("图书名称："+doc.get("bookName"));
            System.out.println("图书图片:"+doc.get("bookPic"));
            System.out.println("图书价格："+doc.get("bookPrice"));
            System.out.println("图书描述："+doc.get("bookDesc"));
        }
        //7.释放资源
        reader.close();

    }
}
