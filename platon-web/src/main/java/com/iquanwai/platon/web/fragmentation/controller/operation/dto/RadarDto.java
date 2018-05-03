package com.iquanwai.platon.web.fragmentation.controller.operation.dto;

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.List;

@Data
public class RadarDto {
    private String title;
    private List<RadarItem> series;

    public static RadarDto init() {
        RadarDto dto = new RadarDto();
        dto.setSeries(Lists.newArrayList(new RadarItem()));
        return dto;
    }

    public void addDetail(String category, Double value, Double max) {
        RadarItemDetail detail = new RadarItemDetail();
        detail.setCategory(category);
        detail.setValue(value);
        detail.setMax(max);
        this.series.get(0).getDetail().add(detail);
    }
}


@Data
class RadarItem {
    RadarItem() {
        this.detail = Lists.newArrayList();
    }

    private List<RadarItemDetail> detail;
}

@Data
class RadarItemDetail {
    private String category;
    private Double value;
    private Double max;
}