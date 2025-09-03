package com.tianji.learning.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.domain.query.PageQuery;
import com.tianji.learning.domain.po.LearningLesson;
import com.tianji.learning.domain.vo.LearningLessonVO;
import com.tianji.learning.service.ILearningLessonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 学生课程表 前端控制器
 * </p>
 *
 * @author author
 * @since 2025-09-03
 */
@RestController
@RequestMapping("/lessons")
public class LearningLessonController {

    private final ILearningLessonService lessonService;

    public LearningLessonController(ILearningLessonService lessonService) {
        this.lessonService = lessonService;
    }


    @GetMapping("/page")
    public PageDTO<LearningLessonVO> queryMyLessons(PageQuery query) {
        return lessonService.queryMyLessons(query);
    }

}
