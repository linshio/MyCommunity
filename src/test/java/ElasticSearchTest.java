import cn.linshio.community.MainApplication;
import cn.linshio.community.dao.DiscussPostMapper;
import cn.linshio.community.dao.elasticsearch.DiscussPostRepository;
import cn.linshio.community.entity.DiscussPost;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.facet.FacetResult;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.security.PublicKey;
import java.util.*;
import java.util.function.Function;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = MainApplication.class)
@Slf4j
public class ElasticSearchTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert() {
        //插入单条数据
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(276));
    }

    @Test
    public void testInsertList(){
        //插入多条数据
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(11,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100,0));
    }


    @Test
    public void testSearchByRepository(){
        //构建查询对象
        SearchQuery searchQueryBuilder = new NativeSearchQueryBuilder()
                //要查询的内容和字段
                .withQuery(QueryBuilders.multiMatchQuery("测试","title","content"))
                //按照指定的字段进行排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //进行分页操作
                .withPageable(PageRequest.of(0,10))
                //对查询到的字段进行分页操作
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("/em"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("/em")
                )
                .build();
        //查询数据
        Page<DiscussPost> page = discussPostRepository.search(searchQueryBuilder);
        //一共多少条数据
        System.out.println(page.getTotalElements());
        //一共有多少页
        System.out.println(page.getTotalPages());
        //当前我们处在页
        System.out.println(page.getNumber());
        //每页显示多少条数据
        System.out.println(page.getSize());

        for (DiscussPost discussPost : page) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void testSearchByTemplate(){
        //构建查询对象
        SearchQuery searchQueryBuilder = new NativeSearchQueryBuilder()
                //要查询的内容和字段
                .withQuery(QueryBuilders.multiMatchQuery("测试","title","content"))
                //按照指定的字段进行排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //进行分页操作
                .withPageable(PageRequest.of(0,10))
                //对查询到的字段进行分页操作
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();

        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQueryBuilder, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                //获取到查询到的所有对象
                SearchHits hits = response.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();

                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    //将类型进行封装转换
                    post.setId(Integer.parseInt(sourceAsMap.get("id").toString()));
                    post.setUserId(Integer.parseInt(sourceAsMap.get("userId").toString()));
                    post.setTitle(sourceAsMap.get("title").toString());
                    post.setContent(sourceAsMap.get("content").toString());
                    post.setCreateTime(new Date(Long.parseLong(sourceAsMap.get("createTime").toString())));
                    post.setCommentCount(Integer.parseInt(sourceAsMap.get("commentCount").toString()));
                    post.setStatus(Integer.parseInt(sourceAsMap.get("status").toString()));

                    //处理高亮显示的结果
                    HighlightField title = hit.getHighlightFields().get("title");
                    if (title!=null){
                        post.setTitle(title.getFragments()[0].toString());
                    }
                    HighlightField content = hit.getHighlightFields().get("content");
                    if (content!=null){
                        post.setContent(content.getFragments()[0].toString());
                    }

                    list.add(post);
                }
                return new AggregatedPageImpl(list,pageable,hits.getTotalHits(),
                        response.getAggregations(),response.getScrollId(),hits.getMaxScore());
            }
        });
        //一共多少条数据
        System.out.println(page.getTotalElements());
        //一共有多少页
        System.out.println(page.getTotalPages());
        //当前我们处在页
        System.out.println(page.getNumber());
        //每页显示多少条数据
        System.out.println(page.getSize());

        for (DiscussPost discussPost : page) {
            System.out.println(discussPost);
        }
    }
}
