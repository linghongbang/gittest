package com.itheima.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;

/**
 * 索引管理类
 */
public class IndexManager {

    //定义索引库位置全局变量
    public static final String INDEX_PATH ="D:\\index";

    /**
     * 根据Term删除索引(模板代码)
     */
    @Test
    public void deleteIndex() throws Exception{
        //1.建立分析器对象（Analyzer），用于分词
        Analyzer analyzer = new IKAnalyzer();

        //2.建立索引库配置对象（IndexWriterConfig），配置索引库
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);

        //3.建立索引库目录对象（Directory），指定索引库位置
        Directory directory = FSDirectory.open(new File(INDEX_PATH));

        //4.建立索引库操作对象（IndexWriter），操作索引库
        IndexWriter writer = new IndexWriter(directory,iwc);

        //5.建立删除条件对象（Term）
        /**
         * mysql：delete from table where bookName=java
         * 需求：删除图书名称域中包含有java的图书
         */
        Term term = new Term("bookName","java");

        //6.使用IndexWriter执行删除
        writer.deleteDocuments(term);

        //7.释放资源
        writer.close();
    }

    /**
     * 删除全部(模板代码)
     */
    @Test
    public void delAllIndex() throws Exception{
        //1.建立分析器对象（Analyzer），用于分词
        Analyzer analyzer = new IKAnalyzer();

        //2.建立索引库配置对象（IndexWriterConfig），配置索引库
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);

        //3.建立索引库目录对象（Directory），指定索引库的位置
        Directory directory = FSDirectory.open(new File(INDEX_PATH));

        //4.建立索引库操作对象（IndexWriter），操作索引库
        IndexWriter writer = new IndexWriter(directory,iwc);

        //5.使用IndexWriter，执行删除
        writer.deleteAll();

        //6.释放资源
        writer.close();
    }

    /**
     * 更新索引
     */
    @Test
    public void UpdateIndexByTerm() throws IOException {

        //1.建立分析器对象（Analyzer），用于分词
        Analyzer analyzer = new IKAnalyzer();

        //2.建立索引库的配置对象（IndexWriterConfig），配置索引库
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);

        //3.建立索引库目录对象（Directory），指定索引库位置
        Directory directory = FSDirectory.open(new File(INDEX_PATH));

        //4.建立索引库操作对象（IndexWriter），操作索引库
        IndexWriter writer = new IndexWriter(directory,iwc);

        //5.建立文档对象（Document）
        Document doc = new Document();

        doc.add(new StringField("id","9523", Field.Store.YES));
       // doc.add(new TextField("name","mybatis and springmvc and spring", Field.Store.YES));

        //更新机制：找得到就更新，找不到就添加
        doc.add(new TextField("name","mybaits and spring and springmvc and lucene and solrj", Field.Store.YES));
        //6.建立更新条件对象（Term）
        Term term = new Term("name","mybatis");

        //7.使用IndexWriter，执行更新
        writer.updateDocument(term,doc);

        //8.释放资源
        writer.close();

    }

    /**
     * 检索流程实现（封装搜索方法）
     *
     */
    private void searcher(Query query) throws Exception {

        //打印搜索语法
        System.out.println("搜索语法："+query);

        //1.建立索引库目录对象（Directory）,指定索引库的位置
        Directory directory = FSDirectory.open(new File(INDEX_PATH));

        //2.建立索引读取对象（IndexReader）,把索引数据读取到内存中
        IndexReader reader = DirectoryReader.open(directory);

        //3.建立索引搜索对象(IndexSearcher),执行搜索，返回搜索的结果集（ToDocs）
        IndexSearcher searcher = new IndexSearcher(reader);

        /**
         * 执行搜索的方法：search
         * 参数：
         *  参数一：查询对象
         *  参数二：获取搜索结果排序后的前n个
         */
        TopDocs topDocs = searcher.search(query, 10);

        //4.处理结果集
            //4.1实际搜索到的结果数量
        System.out.println("实际搜索到的结果数量："+topDocs.totalHits);
            //4.2获取结果数据
        /**
         * ScoreDoc中只包含两个信息：一个是当前的文档id,一个是当前文档的分值
         */
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc sd : scoreDocs) {
            System.out.println("---------------华丽丽分割线----------------");
            int docId = sd.doc;
            float score = sd.score;
            System.out.println("当前文档的Id:"+docId+",当前文档的分值："+score);

            //根据文档id获取文档数据
            Document doc = searcher.doc(docId);
            System.out.println("图书Id:"+doc.get("bookId"));
            System.out.println("图书名称:"+doc.get("bookName"));
            System.out.println("图书价格:"+doc.get("bookPrice"));
            System.out.println("图书图片:"+doc.get("bookPic"));
            System.out.println("图书描述:"+doc.get("bookDesc"));
        }
        //5.释放资源
        reader.close();
    }

    /**
     * 使用TermQuery
     * 需求：查询图书名称域中包含有java的图书
     */
    @Test
    public void testTermQuery() throws Exception {
        //创建查询对象
        TermQuery tq = new TermQuery(new Term("bookName","java"));

        //执行搜索
        searcher(tq);
    }

    /**
     * 使用NumericRangeQuery
     * 需求：查询图书价格在80到100之间的图书
     */
    @Test
    public void testNumericRangeQuery() throws Exception {
        //1.创建查询对象
        /**
         * 参数说明：
         *  field:域的名称
         *  min:搜索范围的最小值
         *  max:搜索范围的最大值
         *  minInclusive:是否包含最小边界值
         *  maxInclusiv:是否包含最大边界值
         */
        //不包含边界值
        //NumericRangeQuery nrq = NumericRangeQuery.newDoubleRange("bookPrice",80d,100d,false,false);

        //测试包含边界值
        NumericRangeQuery nrq = NumericRangeQuery.newDoubleRange("bookPrice",80d,100d,true,true);

        //执行搜索
        searcher(nrq);
    }

    /**
     * 使用BooleanQuery
     * 需求：查询图书名称域中包含有lucene,并且图书价格在80到100之间的图书。（包含边界值）
     */
    @Test
    public void testBooleanQuery() throws Exception {

        //创建查询条件对象一
        TermQuery query1 = new TermQuery(new Term("bookName","lucene"));

        //创建查询对象二
        NumericRangeQuery query2 = NumericRangeQuery.newDoubleRange("bookPrice",80d,100d,true,true);

        //组合查询条件
        BooleanQuery bq = new BooleanQuery();

        //并且
        bq.add(query1, BooleanClause.Occur.MUST);
        bq.add(query2, BooleanClause.Occur.MUST);

      /*  //或
        bq.add(query1, BooleanClause.Occur.SHOULD);
        bq.add(query2, BooleanClause.Occur.SHOULD);*/

        //执行搜索
        searcher(bq);
    }

    /**
     * 使用QueryParser解析表达式
     *        Occur.MUST搜索条件必须满足，相当于AND	             +
     *        Occur.SHOULD搜索条件可选，相当于OR	            空
     *        Occur.MUST_NOT搜索条件不能满足，相当于NOT非	    -
     *
     * 注意事项：表达式中的关键词AND/OR/NOT必须要大写。
     *
     * 需求：查询图书名称域中包含有java,并且图书名称域中包含有lucene的图书
     */
 @Test
    public void testQueryParser() throws Exception {

     //1.创建查询对象
        //1.1创建分析器对象
     Analyzer analyzer = new IKAnalyzer();
        //1.2使用QueryParser解析表达式，实例化Query对象
     QueryParser parser = new QueryParser("bookName",analyzer);

     /**
      * 表达式：bookName:java AND bookName:lucene
      */
     //Query query = parser.parse("bookName:java AND bookName:lucene");

     /**
      * 表达式：bookName:java OR bookName:lucene
      */
    // Query query = parser.parse("bookName:java OR bookName:lucene");

     /**
      * 表达式：bookName:java NOT bookName:lucene
      */
     Query query = parser.parse("bookName:java NOT bookName:lucene");

     //执行搜索
     searcher(query);
    }

    /**
     * 更新索引，学习相关度排序
     */
    @Test
    public void updateIndexSetBoots() throws Exception {

        //1.建立分析器对象（Analyazer），用于分词
        Analyzer analyzer = new IKAnalyzer();

        //2.建立索引库的配置对象（IndexWriterConfig）,配置索引库
        IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_4_10_3,analyzer);

        //3.建立索引库目录对象（Directory）,指定索引库位置
        Directory directory = FSDirectory.open(new File(INDEX_PATH));

        //4.建立索引库操作 对象（IndexWriter）,操作索引库
        IndexWriter writer = new IndexWriter(directory,iwc);

        //5.建立文档对象（Document）
        Document doc = new Document();

        /**
         * 5   Lucene Java精华版 80 5.jpg  本书总结搜索引擎相关理论与实际解决方案，并
         */
        doc.add(new StringField("bookId","5", Field.Store.YES));

        //设置加权值
        TextField nameField = new TextField("bookName","Lucene Java精华版", Field.Store.YES);
        nameField.setBoost(100);
        doc.add(nameField);

        doc.add(new DoubleField("bookPrice",80, Field.Store.YES));
        doc.add(new StoredField("bookPic","5.jpg"));
        doc.add(new TextField("bookDesc","本书总结搜索引擎相关理论与实际解决方案，并.....", Field.Store.NO));

        //6.建立更新条件对象（Term）
        Term term = new Term("bookId","5");

        //7.使用IndexWriter,执行更新
        writer.updateDocument(term,doc);

        //8.释放资源
        writer.close();
    }

    /**
     * 检索流程实现（封装搜索方法--实现高亮;模板代码)
     */
    private void searcherHighlighter(Query query) throws Exception {

        //打印搜索语法
        System.out.println("搜索语法："+query);

        //1.建立索引库目录对象（Directory）,指定索引库的位置
        Directory directory = FSDirectory.open(new File(INDEX_PATH));

        //2.建立索引读取对象（IndexReader）,把索引数据读取到内存中
        IndexReader reader = DirectoryReader.open(directory);

        //3.建立索引搜索对象（IndexSearcher）,执行搜索，返回搜索的结果集（ToDocs）
        IndexSearcher searcher = new IndexSearcher(reader);

        /**
         * 执行搜索的方法：search
         * 参数：
         *  参数一：查询对象
         *  参数二：获取搜索结果排序后的前n个
         */
        TopDocs topDocs = searcher.search(query, 10);

        //4.处理结果集
            //4.1实际搜索到的结果数量
        System.out.println("实际搜索到的结果数量："+topDocs.totalHits);

        //增加高亮处理========================================start
        //1.建立分值对象（QueryScorer），计算分值
        QueryScorer qs = new QueryScorer(query);

        //2.建立输出片段对象（Fragmenter），用于把文档内容切片（分段）
        Fragmenter fragmenter = new SimpleSpanFragmenter(qs);

        //3.建立高亮组件对象（Highlighter）
        Highlighter highlighter = new Highlighter(qs);
            //3.1设置输出片段对象
        highlighter.setTextFragmenter(fragmenter);

        //4.建立分析器对象（Analyzer），用于分词
        Analyzer analyzer = new IKAnalyzer();

        //增加高亮处理==================================end
        //4.2获取结果数据
        /**
         *  ScoreDoc中只包含两个信息：一个是当前的文档id，一个当前文的分值
         */
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc sd : scoreDocs) {
            System.out.println("---------------华丽丽分割线----------------");
            //取出文档id和分值
            int docId = sd.doc;
            float score = sd.score;
            System.out.println("当前文档的Id:"+docId+",当前文档的分值："+score);

            //根据文档id获取文档数据
            Document doc = searcher.doc(docId);
            System.out.println("图书Id:"+doc.get("bookId"));

            //实现图书名称的高亮显示
            String bookName = doc.get("bookName");
            if (bookName != null){
                //5.通过TokenSources类获取文档对象的流对象（TokenStream）
                /**
                 * getTokenStream：获取文档对象的流对象
                 *  参数：
                 *      参数一：文档对象
                 *      参数二：高亮显示的域的名称
                 *      参数三：分析器对象
                 */
                TokenStream tokenStream = TokenSources.getTokenStream(doc, "bookName", analyzer);

                //6.使用高亮组件对象，获取高亮结果内容
                /**
                 * getBestPragment:获取高亮结果内容
                 * 参数：
                 *      参数一：文档的流对象
                 *      参数二：高亮显示域的原始内容
                 */
                bookName = highlighter.getBestFragment(tokenStream,bookName);
            }
            System.out.println("图书名称："+bookName);

            System.out.println("图书价格："+doc.get("bookPrice"));
            System.out.println("图书图片："+doc.get("bookPic"));
            System.out.println("图书描述："+doc.get("bookDesc"));
        }
            //释放资源
        reader.close();
    }

    /**
     * 测试高亮显示
     * 需求：查询图书名称域中包含有java的图书
     */
    @Test
    public void testHighlighter() throws Exception {

        //1.建立查询对象
        Term term = new Term("bookName","java");
        TermQuery query = new TermQuery(term);

        //执行流程
        searcherHighlighter(query);
    }

    /**
     * 自定义高亮html格式化标签
     *
     *      步骤： 1.定义一个html格式化标签的对象SimpleHTMLFormatter
     *             2.建立高亮组件对象Highlighter，指定使用SimpleHTMLFormatter
     */
    private void searcherHighlighter2(Query query) throws Exception {

        //打印搜索语法
        System.out.println("搜索语法："+query);

        //1.建立索引库目录对象（Directory）,指定索引库的位置
        Directory directory = FSDirectory.open(new File(INDEX_PATH));

        //2.建立索引读取对象（IndexReader）,把索引数据读取到内存中
        IndexReader reader = DirectoryReader.open(directory);

        //3.建立索引搜索对象（IndexSearcher）,执行搜索，返回搜索的结果集（ToDocs）
        IndexSearcher searcher = new IndexSearcher(reader);

        /**
         * 执行搜索的方法：search
         * 参数：
         *  参数一：查询对象
         *  参数二：获取搜索结果排序后的前n个
         */
        TopDocs topDocs = searcher.search(query, 10);

        //4.处理结果集
        //4.1实际搜索到的结果数量
        System.out.println("实际搜索到的结果数量："+topDocs.totalHits);

        //增加高亮处理========================================start
        //1.建立分值对象（QueryScorer），计算分值
        QueryScorer qs = new QueryScorer(query);

        //2.建立输出片段对象（Fragmenter），用于把文档内容切片（分段）
        Fragmenter fragmenter = new SimpleSpanFragmenter(qs);

        //3.定义一个html格式化标签的对象SimpleHTMLPormatter
        /**
         * 参数：
         *       preTag:html标签的开始部分
         *       postTag:html标签的结束部分
         */
        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<font color ='red'>","</font>");
         //4.建立高亮组件对象Highlighter,指定使用SimpleHTMLPormatter
        Highlighter highlighter = new Highlighter(formatter,qs);
            //4.1设置输出片段对象
        highlighter.setTextFragmenter(fragmenter);

        //4.建立分析器对象（Analyzer），用于分词
        Analyzer analyzer = new IKAnalyzer();

        //增加高亮处理==================================end
        //4.2获取结果数据
        /**
         *  ScoreDoc中只包含两个信息：一个是当前的文档id，一个当前文的分值
         */
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc sd : scoreDocs) {
            System.out.println("---------------华丽丽分割线----------------");
            //取出文档id和分值
            int docId = sd.doc;
            float score = sd.score;
            System.out.println("当前文档的Id:"+docId+",当前文档的分值："+score);

            //根据文档id获取文档数据
            Document doc = searcher.doc(docId);
            System.out.println("图书Id:"+doc.get("bookId"));

            //实现图书名称的高亮显示
            String bookName = doc.get("bookName");
            if (bookName != null){
                //5.通过TokenSources类获取文档对象的流对象（TokenStream）
                /**
                 * getTokenStream：获取文档对象的流对象
                 *  参数：
                 *      参数一：文档对象
                 *      参数二：高亮显示的域的名称
                 *      参数三：分析器对象
                 */
                TokenStream tokenStream = TokenSources.getTokenStream(doc, "bookName", analyzer);

                //6.使用高亮组件对象，获取高亮结果内容
                /**
                 * getBestPragment:获取高亮结果内容
                 * 参数：
                 *      参数一：文档的流对象
                 *      参数二：高亮显示域的原始内容
                 */
                bookName = highlighter.getBestFragment(tokenStream,bookName);
            }
            System.out.println("图书名称："+bookName);

            System.out.println("图书价格："+doc.get("bookPrice"));
            System.out.println("图书图片："+doc.get("bookPic"));
            System.out.println("图书描述："+doc.get("bookDesc"));
        }
        //释放资源
        reader.close();
    }

    /**
     * 测试自定义标签高亮显示
     * 需求：查询图书名称域中包含有java的图书
     */
    @Test
    public void testHighlighter2() throws Exception {

        //1.建立查询对象
        Term term = new Term("bookName","java");
        TermQuery query = new TermQuery(term);

        //执行流程
        searcherHighlighter2(query);
    }
}
