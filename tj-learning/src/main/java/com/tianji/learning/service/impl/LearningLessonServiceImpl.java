package com.tianji.learning.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.api.client.course.CourseClient;
import com.tianji.api.dto.course.CourseSimpleInfoDTO;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.mapper.LearningLessonMapper;
import com.tianji.learning.service.ILearningLessonService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 学生课程表 服务实现类
 * </p>
 *
 * @author author
 * @since 2025-09-03
 */
@Service
public class LearningLessonServiceImpl extends ServiceImpl<LearningLessonMapper, LearningLesson> implements ILearningLessonService {

    private final CourseClient courseClient;

    public LearningLessonServiceImpl(CourseClient courseClient) {
        this.courseClient = courseClient;
    }

    @Override
    @Transactional
    public void addUserLessons(Long userId, List<Long> courseIds) {
        List<CourseSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(courseIds);
        if (CollUtils.isEmpty(courseInfos)){
            return;
        }
        List<LearningLesson> lessons = courseInfos.stream().map(courseInfo -> {
            LearningLesson lesson = new LearningLesson();
            Integer validDuration = courseInfo.getValidDuration();
            if (validDuration != null && validDuration > 0){
                LocalDateTime now = LocalDateTime.now();
                lesson.setCreateTime(now);
                lesson.setExpireTime(now.plusDays(validDuration));
            }
            lesson.setUserId(userId);
            lesson.setCourseId(courseInfo.getId());
            return lesson;
        }).collect(Collectors.toList());

        saveBatch(lessons);
    }

    @Override
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query) {
        Long userId = UserContext.getUser();

        Page<LearningLesson> page = lambdaQuery()
                .eq(LearningLesson::getUserId, userId)
                .page(query.toMpPage("latest_learn_time", false));
        List<LearningLesson> records = page.getRecords();
        if (CollUtils.isEmpty(records)){
            return PageDTO.empty(page);
        }
        Set<Long> cIds = records.stream().map(LearningLesson::getCourseId).collect(Collectors.toSet());

        List<CourseSimpleInfoDTO> courseInfos = courseClient.getSimpleInfoList(cIds);
        if (CollUtils.isEmpty(courseInfos)){
            throw new IllegalArgumentException("课程信息不存在");
        }

        Map<Long, CourseSimpleInfoDTO> courseInfoMap = courseInfos.stream().collect(Collectors.toMap(CourseSimpleInfoDTO::getId, info -> info));

        List<LearningLessonVO> list = new ArrayList<>(records.size());
        records.forEach(lesson -> {
            LearningLessonVO vo = BeanUtil.copyProperties(lesson, LearningLessonVO.class);
            CourseSimpleInfoDTO courseSimpleInfoDTO = courseInfoMap.get(lesson.getCourseId());
            vo.setCourseName(courseSimpleInfoDTO.getName());
            vo.setCourseCoverUrl(courseSimpleInfoDTO.getCoverUrl());
            vo.setSections(courseSimpleInfoDTO.getSectionNum());
            list.add(vo);
        });
        return PageDTO.of(page, list);
    }
}
