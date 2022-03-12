package com.newcoder.community.dao;


import com.newcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Dictionary;
import java.util.List;

@Mapper
public interface DiscussPostMapper {

    //List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);

    // @Param注解用于给参数取别名,
    // 如果只有一个参数,并且在<if>里使用,则必须加别名.
    int selectDiscussPostRows(@Param("userId") int userId); // 查询共有多少条数据

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateType(int id, int type);

    int updateCommentCount(int id,int commentCount);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);

}
