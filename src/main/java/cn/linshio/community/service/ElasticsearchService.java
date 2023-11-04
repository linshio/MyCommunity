package cn.linshio.community.service;

import cn.linshio.community.dao.elasticsearch.DiscussPostRepository;
import cn.linshio.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

//elasticsearch 的服务方法
@Service
public class ElasticsearchService {

    @Resource
    private DiscussPostRepository repository;

    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    //添加或者修改帖子
    public void saveDiscussPost(DiscussPost discussPost){
        repository.save(discussPost);
    }

    //删除帖子
    public void deleteDiscussPost(int id) {
        repository.deleteById(id);
    }

    /**
     * 搜索帖子
     * @param keyWord 关键词
     * @param current 当前页
     * @param limit 每页显示条数
     * @return 帖子分页
     */
    public Page<DiscussPost> searchDiscussPost(String keyWord,int current,int limit){
        //构建查询对象
        SearchQuery searchQueryBuilder = new NativeSearchQueryBuilder()
                //要查询的内容和字段
                .withQuery(QueryBuilders.multiMatchQuery(keyWord,"title","content"))
                //按照指定的字段进行排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //进行分页操作
                .withPageable(PageRequest.of(current,limit))
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

        return page;
    }

}
