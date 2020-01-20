package com.hww.house.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/5
 * @Time: 19:41
 * Description: redis操作业务
 */
@Component(value = "redisService")
@Slf4j
public class RedisService implements CacheService {

    private static final long serialVersionUID = 1L;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Set<String> getKeys(String... pattern) {
        Set<String> keys = new HashSet<String>();
        for (int i = 0; i < pattern.length; i++) {
            Set<String> allKeys = redisTemplate.keys(pattern[i]);
            if (null == allKeys || allKeys.isEmpty()) {
                continue;
            }
            keys.addAll(allKeys);
        }
        return keys;
    }

    @Override
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error("设置缓存失效时间异常=" + key, e);
            return false;
        }
    }

    @Override
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("判断key是否存在异常=" + key, e);
            return false;
        }
    }

    @Override
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    @Override
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("普通缓存放入错误=" + key, e);
            return false;
        }

    }

    @Override
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("普通缓存放入并设置超期时间错误 =" + key, e);
            return false;
        }
    }


    public boolean set(String key, Object value, long time,TimeUnit timeUnit) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, timeUnit);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            log.error("普通缓存放入并设置超期时间错误 =" + key, e);
            return false;
        }
    }

    @Override
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    @Override
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    @Override
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    @Override
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("HashSet对象缓存错误 =" + key, e);
            return false;
        }
    }

    @Override
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("HashSet对象缓存并设置缓存时间错误 =" + key, e);
            return false;
        }
    }

    @Override
    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error("向hash表中放入数据,如果不存在将创建错误 =" + key, e);
            return false;
        }
    }

    @Override
    public boolean hset(String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("向hash表中放入数据并设置失效时间,如果不存在将创建错误 =" + key, e);
            return false;
        }
    }

    @Override
    public void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    @Override
    public boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    @Override
    public double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    @Override
    public double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }

    @Override
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("根据key获取Set中的所有值错误 =" + key, e);
            return null;
        }
    }

    @Override
    public boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            log.error("根据value从set中查询,是否存在错误 =" + key, e);
            return false;
        }
    }

    @Override
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            log.error("set缓存对象错误 =" + key, e);
            return 0;
        }
    }

    @Override
    public long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            log.error("set缓存对象并设置缓存时间错误 =" + key, e);
            return 0;
        }
    }

    @Override
    public long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            log.error("获取set缓存的长度错误 =" + key, e);
            return 0;
        }
    }

    public long setRemove(String key) {
        try {
            long count = 0;
            Set<Object> objs = sGet(key);
            for (Object object : objs) {
                count += redisTemplate.opsForSet().remove(key, object);
            }
            return count;
        } catch (Exception e) {
            log.error("删除key的values值错误 =" + key, e);
            return 0;
        }
    }

    @Override
    public long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            log.error("删除key的values值错误 =" + key, e);
            return 0;
        }
    }
    //===============================list=================================  

    public List<Object> lGet(String key) {
        try {
            return lGet(key, 0);
        } catch (Exception e) {
            log.error("获取list缓存的内容错误 =" + key, e);
            return null;
        }
    }

    public List<Object> lGet(String key, long start) {
        try {
            return lGet(key, start, lGetListSize(key));
        } catch (Exception e) {
            log.error("获取list缓存的内容错误 =" + key, e);
            return null;
        }
    }

    @Override
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("获取list缓存的内容错误 =" + key, e);
            return null;
        }
    }

    @Override
    public long lGetListSize(String key) {
        try {
            Long l = redisTemplate.opsForList().size(key);
            if (null == l) {
                return 0;
            }
            return l;
        } catch (Exception e) {
            log.error("获取list缓存的长度错误 =" + key, e);
            return 0;
        }
    }

    @Override
    public Object lGetIndex(String key, long index) {
        try {
            return redisTemplate.opsForList().index(key, index);
        } catch (Exception e) {
            log.error("通过索引 获取list中的值错误 =" + key, e);
            return null;
        }
    }

    @Override
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("list放入缓存错误 =" + key, e);
            return false;
        }
    }

    @Override
    public boolean lSet(String key, Object value, long time) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("list放入缓存并设置缓存时间错误 =" + key, e);
            return false;
        }
    }

    @Override
    public boolean lSet(List<Object> value, String key) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            log.error("list多个对象放入缓存错误 =" + key, e);
            return false;
        }
    }

    @Override
    public boolean lSet(List<Object> value, String key, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            log.error("list多个对象放入缓存并设置缓存时间错误 =" + key, e);
            return false;
        }
    }

    @Override
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            log.error("根据索引修改list中的某条数据错误 =" + key, e);
            return false;
        }
    }

    public long lRemove(String... key) {
        long remove = 0;
        try {
            for (int i = 0; i < key.length; i++) {
                for (int j = 0; j < lGetListSize(key[i]); j++, remove++) {
                    List<Object> objs = redisTemplate.opsForList().range(key[i], j, j);
                    lRemove(key[i], objs.size(), objs.get(0));
                }
            }
            return remove;
        } catch (Exception e) {
            log.error("删除多个缓存对象错误 =" + key, e);
            return remove;
        }
    }

    @Override
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            log.error("删除多个缓存对象错误 =" + key, e);
            return 0;
        }
    }

}
