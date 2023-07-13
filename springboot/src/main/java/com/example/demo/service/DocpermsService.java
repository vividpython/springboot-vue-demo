package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.DocpermsQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.Docperms;

import java.util.List;
import java.util.Map;

public interface DocpermsService extends IService<Docperms> {
    /**
     * 添加部门
     * @param docperms
     * @return
     */
    Result insertDocperms(Docperms docperms);




    /**
     * 分页查询部门列表
     * @param index 当前页
     * @param size 每页显示数量
     * @param docpermsQueryParam 筛选条件对象
     * @return
     */
    Result findDocpermsList(Integer index, Integer size, DocpermsQueryParam docpermsQueryParam);

    Result findAllDocperms();

    Result<?> modifyDocperms(Docperms docperms);

    Result<?> deleteDocperms(Integer id);

    Result<?> findByNameList(String docpermsName);

    Result<?> setDocPermsByRoleId(Integer roleId,List<Integer> permissionList);

    Result<?> getPermissionsByRoleId(Integer roleid);
}
