package com.example.demo.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.DocpermsQueryParam;
import com.example.demo.entity.Docperms;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@DS("mysql")
public interface DocpermsMapper extends BaseMapper<Docperms> {
    /**
     * 查询部门列表
     * @param page 分页对象
     * @param docpermsQueryParam 筛选条件
     * @return
     */
    IPage<Docperms> findDocpermsList(Page<Docperms> page, DocpermsQueryParam docpermsQueryParam);
    List<Docperms> findAllDocperms();
    List<Docperms> findByNameList(String docpermsName);

    List<Docperms> getPermissionsByRoleId(Integer roleid);

    int  setDocPermsByRoleId(@Param("roleId")  Integer roleId,@Param("permissionList")List<Integer> permissionList);
}
