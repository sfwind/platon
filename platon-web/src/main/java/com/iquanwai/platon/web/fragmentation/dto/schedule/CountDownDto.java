package com.iquanwai.platon.web.fragmentation.dto.schedule;

import lombok.Data;

/**
 * 倒计时dto
 *
 * @author nethunder
 */
@Data
public class CountDownDto {
   private Integer days;
   private Boolean hasSchedule;
}
