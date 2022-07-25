package com.github.liuyueyi.forum.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.liueyueyi.forum.api.model.enums.YesOrNoEnum;
import com.github.liueyueyi.forum.api.model.vo.PageParam;
import com.github.liuyueyi.forum.service.comment.dto.UserFollowDTO;
import com.github.liuyueyi.forum.service.comment.repository.entity.CommentDO;
import com.github.liuyueyi.forum.service.user.UserRelationService;
import com.github.liuyueyi.forum.service.user.repository.entity.UserInfoDO;
import com.github.liuyueyi.forum.service.user.repository.entity.UserRelationDO;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserInfoMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserMapper;
import com.github.liuyueyi.forum.service.user.repository.mapper.UserRelationMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户关系Service
 *
 * @author louzai
 * @date 2022-07-20
 */
@Service
public class UserRelationServiceImpl implements UserRelationService {

    @Resource
    private UserRelationMapper userRelationMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserFollowDTO> getUserRelationList(Integer userId, PageParam pageParam) {

        // TODO: 需要改成联合查询的方式

        List<UserRelationDO> userRelationList = getUserRelationListByUserId(userId, pageParam);
        if (userRelationList.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserFollowDTO> userFollowList = new ArrayList<>();
        for (UserRelationDO userRelationDO : userRelationList) {
            UserInfoDO userInfoDO = getUserInfoByUserId(userRelationDO.getFollowUserId());
            if (userInfoDO != null)  {
                UserFollowDTO userFollowDTO = new UserFollowDTO();
                userFollowDTO.setFollowUserId(userInfoDO.getUserId());
                userFollowDTO.setPhoto(userInfoDO.getPhoto());
                userFollowDTO.setUserName(userInfoDO.getUserName());
                userFollowDTO.setProfile(userInfoDO.getProfile());
                userFollowList.add(userFollowDTO);
            }
        }
        return userFollowList;
    }

    public UserInfoDO getUserInfoByUserId(Long userId) {
        LambdaQueryWrapper<UserInfoDO> query = Wrappers.lambdaQuery();
        query.eq(UserInfoDO::getUserId, userId)
                .eq(UserInfoDO::getDeleted, YesOrNoEnum.NO.getCode());
        return userInfoMapper.selectOne(query);
    }

    /**
     * 获取关注用户列表
     *
     * @param userId
     * @return
     */
    private List<UserRelationDO> getUserRelationListByUserId(Integer userId, PageParam pageParam) {
        LambdaQueryWrapper<UserRelationDO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDO::getUserId, userId)
                .eq(UserRelationDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last(PageParam.getLimitSql(pageParam))
                .orderByDesc(UserRelationDO::getId);
        return userRelationMapper.selectList(query);
    }

    /**
     * 获取被关注用户列表
     *
     * @param followUserId
     * @return
     */
    public IPage<UserRelationDO> getUserRelationListByFollowUserId(Integer followUserId, PageParam pageParam) {
        LambdaQueryWrapper<UserRelationDO> query = Wrappers.lambdaQuery();
        query.eq(UserRelationDO::getFollowUserId, followUserId);
        Page page = new Page(pageParam.getPageNum(), pageParam.getPageSize());
        return userRelationMapper.selectPage(page, query);
    }

    @Override
    public void deleteUserRelationById(Long id) {
        UserRelationDO userRelationDTO = userRelationMapper.selectById(id);
        if (userRelationDTO != null) {
            userRelationMapper.deleteById(id);
        }
    }
}
