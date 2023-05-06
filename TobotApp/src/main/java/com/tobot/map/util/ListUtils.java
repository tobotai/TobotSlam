package com.tobot.map.util;

import java.util.List;

/**
 * @author houdeming
 * @date 2022/06/24
 */
public class ListUtils {

    /**
     * 分页
     *
     * @param list
     * @param pageNum  当前页数
     * @param pageSize 每页数量
     * @return
     */
    public static List<String> page(List<String> list, int pageNum, int pageSize) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        int listSize = list.size();
        int pageCount = listSize / pageSize;
        pageCount = listSize % pageSize == 0 ? pageCount : pageCount + 1;
        if (pageNum > pageCount) {
            return null;
        }

        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = fromIndex + pageSize;
        if (toIndex > listSize) {
            toIndex = listSize;
        }
        return list.subList(fromIndex, toIndex);
    }
}
