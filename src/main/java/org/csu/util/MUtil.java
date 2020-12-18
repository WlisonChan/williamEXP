package org.csu.util;

import cn.hutool.core.bean.BeanUtil;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

public class MUtil {
    /**
     * 需引入hutool工具类
     * 深度拷贝对象
     * @param src
     * @param <T>
     * @return
     */
    @SneakyThrows
    public static <T> List<T> deepCopy(List<T> src){
        List<T> newList = new ArrayList<>();
        for (T t : src) {
            Object obj = t.getClass().newInstance();
            BeanUtil.copyProperties(t, obj);
            newList.add((T)obj);
        }
        return newList;
    }
}
