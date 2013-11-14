/**
 * Copyright (c) 2013 Sohu. All Rights Reserved
 */
package com.sohu.video.simplepush.message;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;


/**
 * @author erichuang
 *
 */
public class WebPushConfigurerService {
    
    private ShardedJedisPool jedisPool;
    
    private Log log = LogFactory.getLog(WebPushConfigurerService.class);
    
    
    protected Object execute(CallBack callback) {
        ShardedJedis shardedJedis = null;
        
        try {
            shardedJedis = this.getJedisPool().getResource();
            return callback.doInTemp(shardedJedis);

        } catch (Exception e) {
        	log.error("get redis from redis pool error", e);
        } finally {
            if (null != shardedJedis) {
                this.getJedisPool().returnResource(shardedJedis);
            }
        }
        return null;
    }

    private interface CallBack {
        public Object doInTemp(ShardedJedis jedis);
    }
    
    public ShardedJedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(ShardedJedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }


    public Long del(final String key) {
        return (Long) this.execute(new CallBack(){
            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.del(key);
                } catch (Exception e) {
                    log.error("del error,key="+key,e);
                }
               return null;
            }
            
        });
        
    }

    //--------------------list 类型数据操作方法（start）--------------------//
    /**
     * 向list的头部插入元素
     */
    public Long lpush(final String key, final String value) {
        return (Long) this.execute(new CallBack(){
            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.lpush(key, value);
                } catch (Exception e) {
                    log.error("lpush error,key="+key+",value="+value,e);
                }
               return null;
            }
            
        });
    }
    
    /**
     * 向list的尾部插入元素
     */
    public Long rpush(final String key,final String value) throws Exception {
        return (Long) this.execute(new CallBack(){
            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.rpush(key, value);
                } catch (Exception e) {
                    log.error("redis rpush error,list key="+key,e);
                }
                return null;
            }
            
        });
            
    }

    /**
     * 删除list的前size个元素
     */
    public Long ltrim(final String key, final int size) throws Exception {
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                    try {
                        jedis.ltrim(key, size, -1); //删除前size个，即保留size到最后的元素
                        return Long.valueOf(size+"");                        
                    } catch (Exception e) {
                        log.error("ltrim error,list key="+key+",delete size="+size,e);
                    }
                return null;
            }
            
        });
    }

    /**
     * 取指定下标之间内的元素列表
     */
    @SuppressWarnings("unchecked")
    public List<String> lrange(final String key,final int start, final int end){
        return (List<String>) this.execute(new CallBack(){
            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.lrange(key, start, end);
                } catch (Exception e) {
                    log.error("lrange error,key="+key+",start="+start+",end="+end,e);
                }
               return null; 
            }
            
        });
            
    }


    /**
     * 删除list中值为value的元素
     */
    public Long lrem(final String key, final int count, final String value){
        return (Long) this.execute(new CallBack(){
            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.lrem(key, count, value);  
                } catch (Exception e) {
                    log.error("lrem error,key="+key+",value="+value,e);
                }
                return null;
            }
            
        });
            
    }
    //--------------------List 类型数据操作方法（end）--------------------//
    

    //--------------------String 类型数据操作方法（start）--------------------//
    
    public Long incr(final String key) throws Exception {
        
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.incr(key);
                } catch (Exception e) {
                    log.error("incr error,key="+key,e);
                }
               return null;
            }
            
        });
            
    }

    public String get(final String key)throws Exception {
        return (String) this.execute(new CallBack(){
            @Override
            public Object doInTemp(ShardedJedis jedis) {
               try {
                   return jedis.get(key);
                } catch (Exception e) {
                   log.error("get error,key="+key,e);
                }
              return null; 
            }
            
        });
            
    }
    
    public Boolean set(final String key,final String value,final int expirationInSeconds)throws Exception{
        return (Boolean) this.execute(new CallBack(){
            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    jedis.set(key, value);   
                    if(expirationInSeconds > 0){
                        jedis.expire(key, expirationInSeconds);
                    }
                    return true;
                } catch (Exception e) {
                	log.error("set key: "+key+"value: "+value+" error ",e);
                    return false;
                }
            }
            
        });
    }
   
  //--------------------String 类型数据操作方法(end)--------------------//
    

  //--------------------hash 类型数据操作方法(start)--------------------//
    /**
     * 如果指定的fieldKey已存在，则更新该字段的值，并返回0，否则新增一个字段，返回1
     */
    public Long hset(final String key,final String fieldKey,final String fieldValue)throws Exception{
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
               try {
                       return jedis.hset(key, fieldKey, fieldValue);
                   } catch (Exception e) {
                       log.error("hset error,key="+key+",fieldKey="+fieldKey+",fieldValue="+fieldValue,e);
                   }
                   return null;
            }
            
        });
    }
    
    public String hget(final String key,final String fieldKey)throws Exception{
        return (String)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.hget(key, fieldKey);
                } catch (Exception e) {
                    log.error("hget error,key="+key+",fieldKey="+fieldKey,e);
                }
               return null;
            }
            
        });
    }
    
    public Long hincrBy(final String key,final String fieldKey,final Long step) {
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.hincrBy(key, fieldKey, step);
                } catch (Exception e) {
                    log.error("hincrBy error,key="+key+",fieldKey="+fieldKey,e);
                }
                return null;               
            }
            
        });
            
    }
    
    
    public Long hdel(final String key,final String fieldKey)throws Exception{
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.hdel(key, fieldKey);
                } catch (Exception e) {
                    log.error("hel error,key="+key+",fieldKey="+fieldKey,e);
                }
                return null;               
            }
            
        });
    }
    
    @SuppressWarnings("unchecked")
    public List<String> hmget(final String hashKey, final String[] fields) {
        return (List<String>) this.execute(new CallBack(){
            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.hmget(hashKey, fields);
                } catch (Exception e) {
                    log.error("redis hmget error,hash key="+hashKey,e);
                }
                return null;
            }
            
        });
    }
    
    //--------------------hash 类型数据操作方法(end)--------------------//

  
    //--------------------zset 类型数据操作方法(start)--------------------//
    /**
     * 获取有序集合中成员个数
     */
    public Long zcard(final String zsetKey) throws Exception {
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
               try {
                   return jedis.zcard(zsetKey);
                } catch (Exception e) {
                    log.error("zcard error,zsetKey="+zsetKey,e);
                }
                return null; 
            }
            
        });
    }
    
    @SuppressWarnings("unchecked")
    public Set<String> zrange(final String zsetKey,final int start,final int end)throws Exception {
        
        return (Set<String>)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.zrange(zsetKey, start, end); 
                } catch (Exception e) {
                    log.error("zrange error,zsetKey="+zsetKey+",start="+start+",end="+end,e);
                }
               return null;
            }
            
        });
    }
    
    @SuppressWarnings("unchecked")
    public Set<String> zrangeByScore(final String zsetKey,final double minScore,final double maxScore)throws Exception{
        return (Set<String>)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                try {
                    return jedis.zrangeByScore(zsetKey, minScore, maxScore); 
                } catch (Exception e) {
                    log.error("zrangeByScore error,zsetKey="+zsetKey+",minScore="+minScore+",maxScore="+maxScore,e);
                }
                return null;               
            }
            
        });
    }

    public Long zremByRangeScore(final String zsetKey, final double minScore,final double maxScore)throws Exception {
        
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                Long delKeyNum = 0L;
                    try {
                        Long r = jedis.zremrangeByScore(zsetKey, minScore, maxScore);
                        delKeyNum+=r;
                        return delKeyNum;
                    } catch (Exception e) {
                        log.error("zremrangeByScore error,zsetKey="+zsetKey,e);
                    }
               return null;
            }
            
        });
            
    }
    
    public Long zadd(final String zsetKey,final double score,final String value,final Long maxMemberNum)throws Exception{
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                Long addMember = 0L; //增加的成员数
                
                try {
                    if(maxMemberNum > 0){
                        Long num = jedis.zcard(zsetKey);
                        log.info("key="+zsetKey+",maxMemberNum="+maxMemberNum+",members's num in zset="+num);
                        if(num >= 0 && num >= maxMemberNum){ //超过最大的数量，删除最旧的评论
                            log.info("touch maxMemberNum!!");
                            jedis.zremrangeByRank(zsetKey, 0, (int)(num-maxMemberNum)); //删除replyId最小的，即评论最旧的
                        } 
                    }
                    addMember += jedis.zadd(zsetKey, score, value);
                    return addMember;
                } catch (Exception e) {
                    log.error("zadd key="+zsetKey+" error!",e); 
                }
              return null;
            }
            
        });
    }

  //--------------------zset 类型数据操作方法(end)--------------------//

    public Long expire(final String key, final int seconds) {
        
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                    try {
                        return jedis.expire(key, seconds);
                    } catch (Exception e) {
                        log.error("expire error,key="+key,e);
                    }
               return null;
            }
            
        });
            
    }

    public Long hlen(final String key) {
        return (Long)this.execute(new CallBack(){

            @Override
            public Object doInTemp(ShardedJedis jedis) {
                    try {
                        return jedis.hlen(key);
                    } catch (Exception e) {
                        log.error("hlen error,key="+key,e);
                    }
               return null;
            }
            
        });
            
    }

	@SuppressWarnings("unchecked")
	public Map<String, String> hgetAll(final String key) {
		
		return (Map<String, String>) this.execute(new CallBack(){

			@Override
			public Object doInTemp(ShardedJedis jedis) {
				 try {
                     return jedis.hgetAll(key);
                 } catch (Exception e) {
                     log.error("hlen error,key="+key,e);
                 }
				 return null;
			}
			
		});
	
	}

}

    