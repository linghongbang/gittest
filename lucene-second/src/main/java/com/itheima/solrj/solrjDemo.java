package com.itheima.solrj;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.IOException;

/**
 * solrj入门程序案例
 */
public class solrjDemo {

    /**
     * 添加/更新索引
     */
    @Test
    public void addOrupdateIndex() throws IOException, SolrServerException {
        //1.建立httpSolrServer服务对象，连接solr服务
        HttpSolrServer httpSolrServer = new HttpSolrServer("http://127.0.0.1:8081/solr/");

        //2.建立文档对象（SolrInputDocument）
        SolrInputDocument doc = new SolrInputDocument();

        //3.添加索引
        doc.addField("id","9523");
        //doc.addField("name","solrj is a good thins");

        //测试更新索引
        doc.addField("name","solr and lucene are good thins");

        //4.执行添加
        httpSolrServer.add(doc);

        //5.提交
        httpSolrServer.commit();
    }

    /**
     * 根据id删除索引
     */
    @Test
    public void delIndexById() throws IOException, SolrServerException {
        //1.建立HttpSolrServer对象，连接solr对象
        HttpSolrServer httpSolrServer = new HttpSolrServer("http://127.0.0.1:8081/solr/");

        //2.执行删除
        httpSolrServer.deleteById("9523");

        //3.提交
        httpSolrServer.commit();
    }

    /**
     * 根据条件删除索引
     */
    @Test
    public void delIndexByQuery() throws IOException, SolrServerException {
        //1.建立HttpSolrServer对象，连接solr服务
        HttpSolrServer httpSolrServer = new HttpSolrServer("http://127.0.0.1:8081/solr/");

        //2.执行删除
        httpSolrServer.deleteByQuery("name:solr");

        //3.提交
        httpSolrServer.commit();
    }

    /**
     * 查询索引
     */
    @Test
    public void queryIndex() throws SolrServerException {

        //1.建立HttSolrServer对象，连接solr服务
        HttpSolrServer httpSolrServer = new HttpSolrServer("http://127.0.0.1:8081/solr/");

        //2.建立查询对象（SolrQuery）
        SolrQuery sq = new SolrQuery("*:*");

        //3.使用HttpSolrServer对象，执行查询，返回查询结果集（QueryResponse）
        QueryResponse queryResponse = httpSolrServer.query(sq);

        //4.从QueryResponse对象中，获取查询的结果集（SolrDocumentList）
        SolrDocumentList results = queryResponse.getResults();

        //5.处理结果集
            //5.1实际查询的结果数量
        System.out.println("实际搜索到的结果数量："+results.getNumFound());

            //5.2打印结果集
        for (SolrDocument doc : results) {

            System.out.println("-----------------华丽丽分割线---------------");
            System.out.println("id域："+doc.get("id"));
            System.out.println("name域："+doc.get("name"));
        }
    }
}
