package com.upc.utils;

import com.upc.modular.institution.entity.Institution;

import java.util.*;
import java.util.stream.Collectors;

public class InstitutionUtil {
    /**
     * 获取指定机构及其所有子机构的 ID 列表
     * @param rootId 根机构ID
     * @param allList 所有机构数据
     * @return 包含 rootId 的所有子孙机构 ID 列表
     */
    public static List<Long> getAllSubInstitutionIds(Long rootId, List<Institution> allList) {
        Set<Long> result = new HashSet<>();
        result.add(rootId);
        collectChildren(rootId, allList, result);
        return new ArrayList<>(result);
    }

    /**
     * 递归查找所有子机构ID
     */
    private static void collectChildren(Long parentId, List<Institution> allList, Set<Long> result) {
        for (Institution institution : allList) {
            if (Objects.equals(institution.getFatherInstitutionId(), parentId)) {
                result.add(institution.getId());
                collectChildren(institution.getId(), allList, result);
            }
        }
    }

}
