`
PUT /xunwu
{
   "settings": {
     "number_of_replicas": 0,
     "number_of_shards": 1, 
     "index.store.type": "niofs",     //使用文件系统       --7.1取消该配置
     "index.query.default_field": "title",    //默认检索字段   --7.1取消该配置
     "index.unassigned.node_left.delayed_timeout": "5m"     //分片延迟恢复       --7.1取消该配置
   },
   "mappings": {
     "dynamic": false,
     "_all": {                     // 7.1删除该字段
       "enabled": false
     },
     "properties": {
       "houseId": {
         "type": "long"
       },
       "title": {
         "type": "text",
         "index": "true",
         "analyzer": "ik_smart",
         "search_analyzer": "ik_smart"
       },
       "price": {
         "type": "integer"
       },
       "area": {
         "type": "integer"
       },
       "createTime": {
         "type": "date",
         "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
       },
       "lastUpdateTime": {
         "type": "date",
         "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
       },
       "cityEnName": {
         "type": "keyword"
       },
       "regionEnName": {
         "type": "keyword"
       },
       "direction": {
         "type": "integer"
       },
       "distanceToSubway": {
         "type": "integer"
       },
       "subwayLineName": {
         "type": "keyword"
       },
       "subwayStationName": {
         "type": "keyword"
       },
       "tags": {
         "type": "text"
       },
       "street": {
         "type": "keyword"
       },
       "district": {
         "type": "keyword"
       },
       "description": {
         "type": "text",
         "index": "true",
         "analyzer": "ik_smart",
         "search_analyzer": "ik_smart"
       },
       "layoutDesc": {
         "type": "text",
         "index": "true",
         "analyzer": "ik_smart",
         "search_analyzer": "ik_smart"
       },
       "traffic": {
         "type": "text",
         "index": "true",
         "analyzer": "ik_smart",
         "search_analyzer": "ik_smart"
       },
       "roundService": {
         "type": "text",
         "index": "true",
         "analyzer": "ik_smart",
         "search_analyzer": "ik_smart"
       },
       "rentWay": {
         "type": "integer"
       },
       "suggest": {
         "type": "completion",
         "analyzer": "ik_smart",
         "search_analyzer": "ik_smart"
       },
       "location": {
         "type": "geo_point"
       }
     }
   }
 }`