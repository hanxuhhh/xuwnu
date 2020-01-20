package com.hww.house.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.hww.house.base.bmap.BaiDuMapLocation;
import com.hww.house.base.HouseSort;
import com.hww.house.base.bmap.MapSearch;
import com.hww.house.base.enums.Level;
import com.hww.house.base.es.*;
import com.hww.house.base.kafkamessage.KafKaMessage;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.dto.HouseBucketDto;
import com.hww.house.entity.House;
import com.hww.house.entity.HouseDetail;
import com.hww.house.entity.HouseTag;
import com.hww.house.entity.SupportAddress;
import com.hww.house.exception.HouseException;
import com.hww.house.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/12
 * @Time: 15:26
 * Description: es业务操作类
 * 一：创建索引先检索关系型数据库再存入数据到es中
 * 二：es查询，先查询es取出文档id，再去关系型数据库查询
 * 三：搜索建议：
 * <p>
 * 1）建立一个搜索建议索引（index），然后定义一个text字段用来存储搜索字符串，每次搜索一次就存储一次搜索字符串；
 * 每次进行搜索的时候，都将输入文字实时传到后台，然后查询匹配的（通常是前缀匹配）搜索字符串，返回到前台进行展示。
 */
@Service
@Slf4j
public class EsSearchServiceImpl implements EsSearchService {

    private static final String INDEX_NAME = "xunwu";

    private static final String KAFKA_TOPIC = "kafka_xunwu";

    @Autowired
    private HouseService houseService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private RestHighLevelClient client;


    @Autowired
    private HouseDetailService houseDetailService;

    @Autowired
    private HouseTagService houseTagService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private SupportAddressService supportAddressService;

    @Value("${qiniu.cdnPath}")
    private String cdnPath;

    /**
     * 创建文档
     *
     * @param houseId
     */
    @Override
    public void index(long houseId) {
        indexAndRetry(houseId, 0);
    }

    /**
     * 删除文档
     *
     * @param houseId
     */
    @Override
    public void remove(long houseId) {
        removeAndRetry(houseId, 0);
    }

    /**
     * 关键字模糊匹配
     *
     * @param prefix
     * @return
     */
    @Override
    public ServiceResponse<List<String>> suggest(String prefix) {
        //查询请求
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //查询请求参数构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        CompletionSuggestionBuilder completionSuggestionBuilder = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(2);
        SuggestBuilder suggestBuilder = new SuggestBuilder();
        suggestBuilder.addSuggestion("hww_search_suggest", completionSuggestionBuilder);
        searchSourceBuilder.suggest(suggestBuilder);
        searchRequest.source(searchSourceBuilder);
        log.info("搜索建议the search condition is {}", searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            Suggest suggest = searchResponse.getSuggest();
            if (suggest == null) {
                return new ServiceResponse<List<String>>(true, null, new ArrayList<String>());
            }
            Suggest.Suggestion result = suggest.getSuggestion("hww_search_suggest");
            int maxSuggest = 0;
            Set<String> suggestSet = new HashSet<>();
            for (Object term : result.getEntries()) {
                if (term instanceof CompletionSuggestion.Entry) {
                    CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;

                    if (item.getOptions().isEmpty()) {
                        continue;
                    }
                    for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
                        String tip = option.getText().string();
                        if (suggestSet.contains(tip)) {
                            continue;
                        }
                        suggestSet.add(tip);
                        maxSuggest++;
                    }
                }
                if (maxSuggest > 5) {
                    break;
                }
            }
            List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
            return new ServiceResponse(true, null, suggests);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServiceResponse<List<String>>(true, null, new ArrayList<String>());
    }

    /**
     * 聚合统计小区房子的数量
     *
     * @param cityEnName
     * @param regionName
     * @param district
     * @return
     */
    @Override
    public ServiceResponse<Long> aggregateDistrictHouse(String cityEnName, String regionName, String district) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.CITY_EN_NAME, cityEnName));
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.REGION_EN_NAME, regionName));
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.DISTRICT, district));
        TermsAggregationBuilder aggregation = AggregationBuilders.terms(HouseIndexConstant.AGG_DISTRICT)
                .field(HouseIndexConstant.DISTRICT);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder).aggregation(aggregation);
        log.info("执行dsl{}", searchSourceBuilder.toString());
        //searchSourceBuilder.size(0);
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.status() == RestStatus.OK) {
                Terms terms = searchResponse.getAggregations().get(HouseIndexConstant.AGG_DISTRICT);
                long count = terms.getBucketByKey(district).getDocCount();
                return new ServiceResponse<>(true, null, count);
            } else {
                log.info("Failed to Aggregate for " + HouseIndexConstant.AGG_DISTRICT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServiceResponse<>(true, null, 0l);
    }


    /**
     * 聚合统计城市房源信息数量
     */
    @Override
    public BaseServiceResponse<HouseBucketDto> aggregateHouseCountByCityEnName(String cityEnName) {
        List<HouseBucketDto> buckets = Lists.newArrayList();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.CITY_EN_NAME, cityEnName));
        AggregationBuilder aggregation = AggregationBuilders.terms(HouseIndexConstant.AGG_REGION)
                .field(HouseIndexConstant.REGION_EN_NAME);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder).aggregation(aggregation);
        log.info("执行dsl{}", searchSourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.status() != RestStatus.OK) {
                log.error("Failed to Aggregate for " + HouseIndexConstant.AGG_DISTRICT);
                return new BaseServiceResponse<>(0, buckets);
            } else {
                Terms terms = searchResponse.getAggregations().get(HouseIndexConstant.AGG_REGION);
                for (Terms.Bucket bucket : terms.getBuckets()) {
                    buckets.add(new HouseBucketDto(bucket.getKeyAsString(), bucket.getDocCount()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BaseServiceResponse<HouseBucketDto>(searchResponse.getHits().getTotalHits().value, buckets);
    }


    /**
     * es地图查询==>房屋id
     *
     * @param cityEnName     城市名称
     * @param orderBy        排序名称
     * @param orderDirection 排序方向
     * @param start          开始（不是页数，是页数*每页数量）
     * @param size           一页多少
     * @return
     */
    @Override
    public BaseServiceResponse<Long> mapQuery(String cityEnName, String orderBy, String orderDirection, int start, int size) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.CITY_EN_NAME, cityEnName));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        //从多少条开始
        searchSourceBuilder.from(start);
        //每页数量
        searchSourceBuilder.size(size);
        //排序
        searchSourceBuilder.sort(orderBy, SortOrder.fromString(orderDirection));
        log.info("执行的dsl{}", searchSourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        List<Long> ids = new ArrayList<>();
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.status() != RestStatus.OK) {
                log.error("Failed to search");
                return new BaseServiceResponse<>(0, ids);
            } else {
                //结果集
                SearchHit[] hits = searchResponse.getHits().getHits();
                for (SearchHit item : hits) {
                    ids.add(Longs.tryParse(item.getSourceAsMap().get(HouseIndexConstant.HOUSE_ID).toString()));
                }
                return new BaseServiceResponse<Long>(searchResponse.getHits().getTotalHits().value, ids);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BaseServiceResponse<Long>(0, ids);
    }


    /**
     * 小地图查询
     *
     * @param mapSearch
     * @return
     */
    @Override
    public BaseServiceResponse<Long> mapBoundQuery(MapSearch mapSearch) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termQuery(HouseIndexConstant.CITY_EN_NAME, mapSearch.getCityEnName()));

        boolQuery.filter(
                QueryBuilders.geoBoundingBoxQuery("location")
                        .setCorners(
                                new GeoPoint(mapSearch.getLeftLatitude(), mapSearch.getLeftLongitude()),
                                new GeoPoint(mapSearch.getRightLatitude(), mapSearch.getRightLongitude())
                        ));
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQuery);
        //从多少条开始
        searchSourceBuilder.from(mapSearch.getStart());
        //每页数量
        searchSourceBuilder.size(mapSearch.getSize());
        //排序
        searchSourceBuilder.sort(mapSearch.getOrderBy(), SortOrder.fromString(mapSearch.getOrderDirection()));
        log.info("执行的dsl{}语句", searchSourceBuilder.toString());
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = null;
        List<Long> ids = new ArrayList<>();
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.status() != RestStatus.OK) {
                log.error("Failed to search");
                return new BaseServiceResponse<>(0, ids);
            } else {
                //结果集
                SearchHit[] hits = searchResponse.getHits().getHits();
                for (SearchHit item : hits) {
                    ids.add(Longs.tryParse(item.getSourceAsMap().get(HouseIndexConstant.HOUSE_ID).toString()));
                }
                return new BaseServiceResponse<Long>(searchResponse.getHits().getTotalHits().value, ids);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new BaseServiceResponse<Long>(0, ids);
    }

    /**
     * es查询
     * 用户查询返回文档的houseId
     *
     * @param rentSearch
     * @return
     */
    @Override
    public BaseServiceResponse<Long> userEsQuery(RentSearch rentSearch) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.CITY_EN_NAME, rentSearch.getCityEnName()));

        if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.REGION_EN_NAME, rentSearch.getRegionEnName()));
        }

        //关键字
        if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
            boolQueryBuilder.must(
                    QueryBuilders.multiMatchQuery(rentSearch.getKeywords(),
                            HouseIndexConstant.TITLE,
                            HouseIndexConstant.TRAFFIC,
                            HouseIndexConstant.DISTRICT,
                            HouseIndexConstant.ROUND_SERVICE,
                            HouseIndexConstant.SUBWAY_LINE_NAME,
                            HouseIndexConstant.SUBWAY_STATION_NAME,
                            HouseIndexConstant.STREET
                    ));
        }
        //价格区间
        if (rentSearch.getPriceBlock() != null && !"*".equals(rentSearch.getPriceBlock())) {
            RentValueBlock priseValueBlock = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
            RangeQueryBuilder priseRangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexConstant.PRICE);
            if (priseValueBlock.getMin() > 0) {
                priseRangeQueryBuilder.gte(priseValueBlock.getMin());
            } else {
                priseRangeQueryBuilder.gte(0);
            }
            if (priseValueBlock.getMax() > 0) {
                priseRangeQueryBuilder.lte(priseValueBlock.getMax());
            }
            boolQueryBuilder.filter(priseRangeQueryBuilder);
        }

        //地区区间
        if (rentSearch.getAreaBlock() != null && !"*".equals(rentSearch.getAreaBlock())) {
            RentValueBlock areaValueBlock = RentValueBlock.matchArea(rentSearch.getAreaBlock());
            RangeQueryBuilder areaRangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexConstant.AREA);
            if (areaValueBlock.getMin() > 0) {
                areaRangeQueryBuilder.gte(areaValueBlock.getMin());
            } else {
                areaRangeQueryBuilder.gte(0);
            }
            if (areaValueBlock.getMax() > 0) {
                areaRangeQueryBuilder.lte(areaValueBlock.getMax());
            }
            boolQueryBuilder.filter(areaRangeQueryBuilder);
        }
        //朝向
        if (rentSearch.getDirection() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.DIRECTION, rentSearch.getDirection()));
        }
        //租赁方式
        if (rentSearch.getRentWay() > -1) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.RENT_WAY, rentSearch.getRentWay()));
        }
        //卧室数量
        if (rentSearch.getRoom() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexConstant.ROOM, rentSearch.getRoom()));
        }
        //查询请求
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
        //查询请求参数构造器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //将boolQueryBuilder设置到SearchSourceBuilder中
        searchSourceBuilder.query(boolQueryBuilder);

        //从多少条开始
        searchSourceBuilder.from(rentSearch.getStart());
        //每页数量
        searchSourceBuilder.size(rentSearch.getSize());
        //排序
        searchSourceBuilder.sort(HouseSort.getSortKey(rentSearch.getOrderBy()), SortOrder.fromString(rentSearch.getOrderDirection()));
        //将构造器设置到查询请求中
        searchRequest.source(searchSourceBuilder);
        log.info("the search condition is {}", searchSourceBuilder);
        try {
            List<Long> houseIds = Lists.newArrayList();
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse.status() != RestStatus.OK) {
                log.info("Search status is no ok  ");
                return new BaseServiceResponse<Long>(0, houseIds);
            }
            //结果集
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit item : hits) {
                houseIds.add(Longs.tryParse(item.getSourceAsMap().get(HouseIndexConstant.HOUSE_ID).toString()));
            }
            return new BaseServiceResponse<Long>(searchResponse.getHits().getTotalHits().value, houseIds);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new HouseException("未查询到任何消息");
    }


    /**
     * 使用kafka异步发送消息--创建文档
     *
     * @param houseId
     * @param retry
     */
    //@Transactional
    private void indexAndRetry(long houseId, int retry) {
        if (retry > KafKaMessage.MAX_RETRY) {
            log.error("retry index times over " + KafKaMessage.MAX_RETRY);
            return;
        }
        KafKaMessage kafKaMessage = new KafKaMessage(houseId, KafKaMessage.INDEX, retry);
       /*
        生产者事物的写法 或者利用spring的@Transactional注解
        kafkaTemplate.executeInTransaction(t -> {
            kafkaTemplate.send(KAFKA_TOPIC, JSONObject.toJSONString(kafKaMessage));
            return true;
        });
        */
        kafkaTemplate.send(KAFKA_TOPIC, JSONObject.toJSONString(kafKaMessage));
    }

    /**
     * 使用kafka异步发送消息--删除文档
     *
     * @param houseId
     * @param retry
     */
    private void removeAndRetry(long houseId, int retry) {
        if (retry > KafKaMessage.MAX_RETRY) {
            log.error("retry index times over " + KafKaMessage.MAX_RETRY);
            return;
        }
        KafKaMessage kafKaMessage = new KafKaMessage(houseId, KafKaMessage.REMOVE, retry);
        kafkaTemplate.send(KAFKA_TOPIC, JSONObject.toJSONString(kafKaMessage));
    }

    /**
     * kafka消息监听
     *
     * @param content
     */
    @KafkaListener(topics = KAFKA_TOPIC)
    private void handleMessage(String content) {
        KafKaMessage kafKaMessage = JSONObject.parseObject(content, KafKaMessage.class);
        switch (kafKaMessage.getOperation()) {
            case KafKaMessage.INDEX:
                kafKaCreateOrUpdate(kafKaMessage);
                break;
            case KafKaMessage.REMOVE:
                kafKaRemoveIndex(kafKaMessage);
                break;
            default:
                log.info("not support  the message " + kafKaMessage);
                break;
        }
    }

    /**
     * kafka异步处理消息-删除文档
     * 同时移除poi数据
     *
     * @param kafKaMessage
     */
    private void kafKaRemoveIndex(KafKaMessage kafKaMessage) {
        long houseId = kafKaMessage.getHouseId();
        DeleteRequest request = new DeleteRequest(INDEX_NAME, String.valueOf(houseId));
        try {
            DeleteResponse response = this.client.delete(request, RequestOptions.DEFAULT);
            ReplicationResponse.ShardInfo shardInfo = response.getShardInfo();
            if (shardInfo.getTotal() <= 0) {
                this.removeAndRetry(houseId, kafKaMessage.getRetry() + 1);
            }
            if (shardInfo.getTotal() == shardInfo.getSuccessful()) {
                log.info("delete doc " + shardInfo.getTotal());
            }
//            ServiceResponse serviceResponse = supportAddressService.removeLbs(houseId);
//
//            if (!serviceResponse.isSuccess()) {
//                log.warn("Did not remove data from es for response: " + response);
//                // 重新加入消息队列
//                this.removeAndRetry(houseId, kafKaMessage.getRetry() + 1);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * kafka异步处理消息-新增或者修改
     * 同时上传poi数据
     *
     * @param kafKaMessage
     */
    public void kafKaCreateOrUpdate(KafKaMessage kafKaMessage) {
        long houseId = kafKaMessage.getHouseId();
        House house = houseService.getHouseById(houseId);
        if (house == null) {
            log.error(" house {} is not exist !", houseId);
            //再尝试一次
            indexAndRetry(houseId, kafKaMessage.getRetry() + 1);
            return;
        }
        HouseIndexTemplate houseIndexTemplate = modelMapper.map(house, HouseIndexTemplate.class);
        HouseDetail houseDetail = houseDetailService.getHouseById(houseId);
        modelMapper.map(houseDetail, houseIndexTemplate);

        //城市信息
        SupportAddress city = supportAddressService.getSupportAddress(house.getCityEnName(), Level.CITY.getValue());
        //地区信息

        SupportAddress region = supportAddressService.getSupportAddress(house.getRegionEnName(), Level.REGION.getValue());

        String address = city.getCnName() + region.getCnName() + house.getStreet() /* + house.getDistrict()+ houseDetail.getAddress()*/;

        //泥地里获取经纬度
        ServiceResponse<BaiDuMapLocation> locationResponse = supportAddressService.getBaiDuMapLocationByCity(city.getCnName(), address);
        if (locationResponse.isSuccess()) {
            houseIndexTemplate.setLocation(locationResponse.getResult());
        }

        List<HouseTag> houseTags = houseTagService.getHouseTagByHouseId(houseId);
        ArrayList<String> tagsString = Lists.newArrayList();
        if (houseTags.size() > 0 && houseTags != null) {
            houseTags.forEach(item -> {
                tagsString.add(item.getName());
            });
            houseIndexTemplate.setTags(tagsString);
        }
        //反查es判断数据是否已经存入es中，再采取相应的措施
        GetRequest getRequest = new GetRequest(INDEX_NAME, String.valueOf(houseId));
        GetResponse getResponse = null;
        try {
            getResponse = this.client.get(getRequest, RequestOptions.DEFAULT);
            if (getResponse.isExists()) {
                update(houseId, houseIndexTemplate);
            } else {
                create(houseId, houseIndexTemplate);
            }
            //上传poi数据
            ServiceResponse serviceResponse = supportAddressService.lbsUpload(
                    houseIndexTemplate.getLocation(),
                    house.getArea(),
                    house.getTitle(),
                    house.getPrice(),
                    city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict(),
                    houseId,
                    StringUtils.join(tagsString.toArray(), ","),
                    this.cdnPath + house.getCover());
            if (!serviceResponse.isSuccess()) {
                this.indexAndRetry(houseId, kafKaMessage.getRetry() + 1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /******************************操作es的方法*************************************/
    /**
     * 添加文档
     *
     * @param houseId            房屋id
     * @param houseIndexTemplate 模版
     * @return
     */
    private void create(long houseId, HouseIndexTemplate houseIndexTemplate) {
        //更新自动补全字段
        if (!updateSuggest(houseIndexTemplate)) {
            return;
        }
        String houseJson = JSON.toJSONStringWithDateFormat(houseIndexTemplate, "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat);
        IndexRequest request = new IndexRequest(INDEX_NAME).id(String.valueOf(houseId))
                .source(houseJson, XContentType.JSON);
        try {
            IndexResponse response = this.client.index(request, RequestOptions.DEFAULT);
            log.info("create doc in :" + INDEX_NAME);
        } catch (IOException e) {
            log.error("error to create doc ", e);
        }
    }

    /**
     * 修改文档
     *
     * @param houseId
     * @param houseIndexTemplate
     * @return
     */
    private void update(long houseId, HouseIndexTemplate houseIndexTemplate) {
        ////更新自动补全字段
        if (!updateSuggest(houseIndexTemplate)) {
            return;
        }
        UpdateRequest request = new UpdateRequest(INDEX_NAME, String.valueOf(houseId));
        String houseJson = JSON.toJSONStringWithDateFormat(houseIndexTemplate, "yyyy-MM-dd HH:mm:ss", SerializerFeature.WriteDateUseDateFormat);
        request.doc(houseJson, XContentType.JSON);
        try {
            UpdateResponse response = this.client.update(request, RequestOptions.DEFAULT);
            log.info("update doc in :" + INDEX_NAME);
        } catch (IOException e) {
            log.error("error to update doc ", e);
        }
    }

    /**
     * 删除再添加
     *
     * @param totalHit
     * @param houseIndexTemplate
     * @return
     */
    private boolean deleteAndCreate(long houseId, long totalHit, HouseIndexTemplate houseIndexTemplate) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX_NAME);
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder(HouseIndexConstant.HOUSE_ID, String.valueOf(houseIndexTemplate.getHouseId()));
        request.setQuery(termQueryBuilder);
        try {
            BulkByScrollResponse bulkByScrollResponse = client.deleteByQuery(request, RequestOptions.DEFAULT);
            //删除数量
            long deleted = bulkByScrollResponse.getDeleted();
            if (totalHit != totalHit) {
                log.error("Need delete {}, but {} was deleted", totalHit, deleted);
                return false;
            } else {
                create(houseId, houseIndexTemplate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 经典啊
     * 对houseIndexTemplate街道，标题，描述等数据进行分词后存入houseIndexTemplate的suggests里面。
     * 定制化--小区不分词，
     *
     * @param houseIndexTemplate
     * @return
     */
    private boolean updateSuggest(HouseIndexTemplate houseIndexTemplate) {
        AnalyzeRequest request = new AnalyzeRequest();
        request.text(
                houseIndexTemplate.getTitle(),
                houseIndexTemplate.getLayoutDesc(),
                houseIndexTemplate.getRoundService(),
                houseIndexTemplate.getDescription(),
                houseIndexTemplate.getSubwayLineName(),
                houseIndexTemplate.getSubwayStationName()
        );
        request.analyzer("ik_smart");
        try {
            AnalyzeResponse response = client.indices().analyze(request, RequestOptions.DEFAULT);
            List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
            if (tokens == null) {
                log.info("Can not analyze token for house: " + houseIndexTemplate.getHouseId());
                return false;
            }
            List<HouseSuggest> suggests = new ArrayList<>();
            for (AnalyzeResponse.AnalyzeToken token : tokens) {
                // 排序数字类型 || 小于2个字符的分词结果
                if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
                    continue;
                }

                HouseSuggest suggest = new HouseSuggest();
                suggest.setInput(token.getTerm());
                suggests.add(suggest);
            }
            // 定制化小区自动补全--很骚啊
            HouseSuggest districrtSuggest = new HouseSuggest();
            districrtSuggest.setInput(houseIndexTemplate.getDistrict());
            suggests.add(districrtSuggest);
            // 定制化街道自动补全--很骚啊
            HouseSuggest streetSuggest = new HouseSuggest();
            streetSuggest.setInput(houseIndexTemplate.getStreet());
            suggests.add(streetSuggest);
            houseIndexTemplate.setSuggest(suggests);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new HouseException("分词出错");
    }

}