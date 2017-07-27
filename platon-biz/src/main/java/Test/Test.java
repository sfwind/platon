package Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * Created by xfduan on 2017/7/18.
 */
public class Test {

    public static void main(String[] args) {
        JSONObject json = new JSONObject();
        Map dataMap = Maps.newTreeMap();
        dataMap.put(0, "https://xfduan.github.io/imgs/avatar.jpeg");
        dataMap.put(1, "https://xfduan.github.io/imgs/avatar.jpeg");
        dataMap.put(2, "https://xfduan.github.io/imgs/avatar.jpeg");
        dataMap.put(3, "https://xfduan.github.io/imgs/avatar.jpeg");
        dataMap.put(4, "https://xfduan.github.io/imgs/avatar.jpeg");
        dataMap.put(5, "https://xfduan.github.io/imgs/avatar.jpeg");
        dataMap.put(6, "https://xfduan.github.io/imgs/avatar.jpeg");
        json.putAll(dataMap);
        System.out.println(json.toJSONString());
    }

}
