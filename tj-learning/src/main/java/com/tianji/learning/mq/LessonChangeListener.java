package com.tianji.learning.mq;

import com.tianji.api.dto.trade.OrderBasicDTO;
import com.tianji.common.constants.MqConstants;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.service.ILearningLessonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LessonChangeListener {

    private final ILearningLessonService lessonService;

    public LessonChangeListener(ILearningLessonService lessonService) {
        this.lessonService = lessonService;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "learning.lesson.pay.queue", durable = "true"),
            exchange = @Exchange(value = MqConstants.Exchange.ORDER_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.ORDER_PAY_KEY
    ))
    public void listenLessonPay(OrderBasicDTO orderBasicDTO){
        if (orderBasicDTO == null || orderBasicDTO.getUserId() == null || CollUtils.isEmpty(orderBasicDTO.getCourseIds())) {
            log.info("订单信息为空");
            return;
        }
        log.debug("监听订单支付，添加用户课程关系，订单id：{}，用户id：{}，课程id：{}", orderBasicDTO.getOrderId(), orderBasicDTO.getUserId(), orderBasicDTO.getCourseIds());
        lessonService.addUserLessons(orderBasicDTO.getUserId(), orderBasicDTO.getCourseIds());
    }
}
