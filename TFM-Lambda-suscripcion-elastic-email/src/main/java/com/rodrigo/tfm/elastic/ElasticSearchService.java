package com.rodrigo.tfm.elastic;

import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.*;


public class ElasticSearchService {


    private static RestHighLevelClient esClient;
    private String host;
    private int port;
    private String protocol;

    private static String aesEndpoint; // e.g. https://search-mydomain.us-west-1.es.amazonaws.com
    //    private static String aesEndpoint = "https://search-midominiotest-gurn7gkn5qijqldxkntpysbrje.us-east-1.es.amazonaws.com"; // e.g. https://search-mydomain.us-west-1.es.amazonaws.com
    private static String regionName;
//    private static String region = "us-east-1";


    //    SimpleDateFormat dateFormat;
    String indexName;
    String typeIndexName;

    private static ElasticSearchService uniqueInstance;

    public static ElasticSearchService getIntance() {
        if (uniqueInstance == null) {
            try {
                uniqueInstance = new ElasticSearchService();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return uniqueInstance;
    }


    private ElasticSearchService() throws IOException {
//        this.config = loadEngineResources();
//        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        loadConfig();
        esClient = AWSElasticSearchFactory.createClient(aesEndpoint, regionName);

        boolean responsePing = esClient.ping(RequestOptions.DEFAULT);

        if (!responsePing) {
            System.out.println("ElasticSearch is not available");
            System.exit(1);
        }

        if (!existsIndex(indexName)) {
            System.out.println("Index [" + indexName + "] not exist");
            System.exit(1);
        }
    }


    public static void main(String[] args) throws IOException {


        ElasticSearchService elasticConn = new ElasticSearchService();
        HashMap<String, Object> filtro = new HashMap<String, Object>() {{
            put("especie", "Perro");
            put("raza", "Dálmata");
            put("sexo", "M");
        }};
        elasticConn.getEmails(filtro);


    }


    private void loadConfig() {
        System.out.println("Load config");
        // https://search-midominiotest-gurn7gkn5qijqldxkntpysbrje.us-east-1.es.amazonaws.com/
//        host = "search-midominiotest-gurn7gkn5qijqldxkntpysbrje.us-east-1.es.amazonaws.com";
//        port = 9200;
//        protocol = "http";
//        indexName = "suscripcion_filtro";
//        typeIndexName = "suscripcion";

        System.out.println("ElasticSearch_regionName = [" + System.getenv("ElasticSearch_regionName") + "]");
        System.out.println("ElasticSearch_host = [" + System.getenv("ElasticSearch_host") + "]");
        System.out.println("ElasticSearch_port = [" + Integer.parseInt(System.getenv("ElasticSearch_port")) + "]");
        System.out.println("ElasticSearch_protocol = [" + System.getenv("ElasticSearch_protocol") + "]");
        System.out.println("ElasticSearch_indexName = [" + System.getenv("ElasticSearch_indexName") + "]");
        System.out.println("ElasticSearch_typeIndexName = [" + System.getenv("ElasticSearch_typeIndexName") + "]");

        regionName = System.getenv("ElasticSearch_regionName");
        host = System.getenv("ElasticSearch_host");
        port = Integer.parseInt(System.getenv("ElasticSearch_port"));
        protocol = System.getenv("ElasticSearch_protocol");
        indexName = System.getenv("ElasticSearch_indexName");
        typeIndexName = System.getenv("ElasticSearch_typeIndexName");

//        "https://search-midominiotest-gurn7gkn5qijqldxkntpysbrje.us-east-1.es.amazonaws.com"
        aesEndpoint = protocol + "://" + host;

        System.out.println("aesEndpoint = [" + aesEndpoint + "]");
    }


    public Set<String> getEmails(Map<String, Object> filtro) {


        Map<String, Boolean> map = new HashMap();

        System.out.println("----------------------------------------------");
        System.out.println("filtro = [" + filtro + "]");
        System.out.println("----------------------------------------------");


        SearchRequest searchRequest = new SearchRequest("suscripcion_filtro");
        searchRequest.source(createQuery(filtro));


        SearchResponse searchResponse = null;
        try {

            System.out.println("Making request to Elastic");
            SearchHits searchHits = esClient
                    .search(searchRequest, RequestOptions.DEFAULT)
                    .getHits();

            System.out.println("TotalHits = [" + searchHits.totalHits + "]");

            Iterator<SearchHit> iterator = searchHits.iterator();

            while (iterator.hasNext()) {
                SearchHit hit = iterator.next();
//                System.out.println("  -> " + hit.getSourceAsMap() + "");
                map.put((String) hit.getSourceAsMap().get("email"), true);

            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);

        }

        return map.keySet();
    }

    private SearchSourceBuilder createQuery(Map<String, Object> filtro) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder bqb = QueryBuilders.boolQuery();

        for (Map.Entry<String, Object> entry : filtro.entrySet()) {

            System.out.println("entry = [" + entry + "]");

            if (entry.getValue() instanceof String) {
                bqb.must(QueryBuilders.matchQuery(entry.getKey(), entry.getValue()).operator(Operator.AND));

            } else if (entry.getValue() instanceof List) {

                List<String> list = (List<String>) entry.getValue();

                for (String str : list) {
//                  bqb.must(QueryBuilders.termQuery(entry.getKey(), list));

//                  bqb.must(QueryBuilders.matchQuery(entry.getKey(), list.get(0)).operator(Operator.OR));
                    bqb.should(QueryBuilders.matchQuery(entry.getKey(), str));

//                  bqb.filter(QueryBuilders.termsQuery(entry.getKey(), list));
                }
            }

        }

        searchSourceBuilder.query(bqb);
        return searchSourceBuilder;
    }

    private static boolean existsIndex(String indexName) throws IOException {
        //sync
        boolean exists = esClient
                .indices()
                .exists(new GetIndexRequest().indices(indexName), RequestOptions.DEFAULT);
        //async
        //createClient.indices().existsAsync(request, RequestOptions.DEFAULT, listener);
        return exists;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        esClient.close();
        uniqueInstance = null;
    }
}
