package com.iquanwai.platon.biz.po.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author nethunder
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleMonth {
    private Integer year;
    private Integer month;
    private Integer category;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScheduleMonth) {
            ScheduleMonth temp = (ScheduleMonth) obj;
            return Objects.equals(temp.getYear(), this.year) && Objects.equals(temp.getMonth(), this.month) && Objects.equals(temp.getCategory(), this.category);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Integer.valueOf((Objects.toString(year) + Objects.toString(month) + Objects.toString(category)));
    }
}
