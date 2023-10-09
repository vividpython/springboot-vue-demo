package com.example.demo.controller;

import com.example.demo.common.DocumentQueryParam;
import com.example.demo.common.Result;
import com.example.demo.entity.OneWeekCreateCount;
import com.example.demo.entity.Depart;
import com.example.demo.entity.Document;
import com.example.demo.entity.User;
import com.example.demo.service.DepartService;
import com.example.demo.service.DocumentService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/document")
public class DocumentController {

    private final static Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Autowired
    DocumentService documentService;

    @Autowired
    DepartService departService;
    @Autowired
    UserService userService;

    //单个新增图纸信息
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping
    public Result<?> save(@RequestBody Document document,HttpServletRequest request ) {
        //获取当前用户的id 作为文件的创建者的id
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        Integer id = userByUsername.getId();
        Integer parentId = userByUsername.getDepart().getParentId();
        //根据文件上传者的身份获取他所在的事业部 并且设置为文件的所属的事业部
        document.setDepartId(parentId);
        //设置创建人的id
        document.setUserId(id);
        return documentService.insertDocument(document);
    }

    //修改新增图纸信息
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/editDocument")
    public Result<?> edit(@RequestBody Document document,HttpServletRequest request ) {
        //获取当前用户的id 作为文件的创建者的id
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        //1 首先找到待更改的文件
        //2 判断待更改的文件是否为压缩包
        //  如果待更改的文件为压缩包进入下一步
        //  如果待更改的文件不是压缩包 则直接拒绝更改
        //3 解压原来的文件 并且根据文件名称核对 解压文件中是否有要更新的文件
        //  如果含有要更细的文件进入下一步
        //  如果不含有待更新的文件 则直接拒绝
        //4 复制原来的压缩包 并且起名加copy后缀 然后把待更新的文件放到copy压缩包中替代
        //5
        return documentService.insertDocument(document);
    }



    //批量新增 设计变更的时候的变更通知单也是走这里
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @RequiresPermissions("document:multipleInsert")
    @PostMapping("/insert")
    public Result<?> save1(@RequestBody Document document,HttpServletRequest request ) {
        //获取当前用户的id 作为文件的创建者的id
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        Integer id = userByUsername.getId();
        Integer parentId = userByUsername.getDepart().getParentId();
        //根据文件上传者的身份获取他所在的事业部 并且设置为文件的所属的事业部
        document.setDepartId(parentId);

        document.setUserId(id);

        return documentService.insertDocument1(document);
    }


    //文件更新用户部门验证
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/departConfirm")
    public Result<?> departConfirm(@RequestBody Document document,HttpServletRequest request ){
        //首先获取当前用户的部门是什么
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        Integer currentUserDepartId = userByUsername.getDepartId();
        if (document != null && document.getItemNo() != null){
            Integer fileCreateDepartId = documentService.departConfirm(document);
            if (fileCreateDepartId == null) {
                // 查询为空，进行相应处理
                return Result.error("201", "项目信息查询失败");
            } else {
                // 查询结果不为空，进行其他操作
                if (currentUserDepartId.equals(fileCreateDepartId)) {
                    // 部门ID相同，返回成功
                    return Result.success();
                } else {
                    // 部门ID不同，返回失败
                    return Result.error("201", "非本部门项目，禁止更新");
                }
            }
        }else{
            return Result.error("201", "项目信息参数错误");
        }
    }


    //更新（批量更新用 需计算当前版本）
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/updateM")
    public Result<?> updateM(@RequestBody Document document,HttpServletRequest request) {
        //获取当前用户的id 作为文件的更新者的id
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        //获取操作用户的用户信息
        Integer id = userByUsername.getId();
        Integer parentId = userByUsername.getDepart().getParentId();
        //设定更新文件的事业部id
        document.setDepartId(parentId);

        document.setSubId(id);
        return documentService.updateDocumentM(document);
    }


    /**
     * @description: 此函数用在验证批量导入的时候文件是否重复的问题
     * @param document:
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/6/7 15:11
     */
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer",})
    @PostMapping("/confirm")
    public Result<?> confirm(@RequestBody Document document) {

        return documentService.confirmDocument(document);
    }
    /**
     * @description: 此函数用于验证修改或者作废压缩包中单文件的时候 压缩包是否已经校验和开放的 如果已经开放则返回true 否则返回false
     * @param document:
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/10/7 10:33
     */

    @PostMapping("publishconfirm")
    public Result<?> publishconfirm(@RequestBody Document document) {
        Integer approvalStatus = document.getApprovalStatus();
        if (approvalStatus != null && approvalStatus == 1){
            Integer publishStatus = document.getPublishStatus();
            if (publishStatus != null && publishStatus == 1){

                return Result.success();
            }else{
                return Result.error("201","文件未发布，禁止对未发布文件修改");
            }
        }else{
            return Result.error("201","文件未校核，禁止对未校核文件修改");
        }
    }

    @PostMapping("/publish")
    public Result<?> publish(@RequestBody Document document,HttpServletRequest request) {
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"), "username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        if (userByUsername == null) {
            // 用户不存在，处理对应的逻辑
            return Result.error("201","审核人员用户信息出错");
        }

        // 查询审核人员的身份，根据审核人员的身份获取他所在的部门
        Depart depart1 = userByUsername.getDepart();
        if (depart1 == null) {
            return Result.error("201","审核人员部门不存在");
        }
        Integer id1 = depart1.getId();

        // 获取文件创建者的身份
        Integer userId = document.getUserId();
        if (userId == null) {
            // 文件创建者的 ID 为空，处理对应的逻辑
            return Result.error("201","文件创建者信息出错");
        }

        // 根据文件创建者的 ID 查询他所在的部门
        User userInfolog = userService.getUserInfolog(userId);
        if (userInfolog == null) {
            // 文件创建者不存在，处理对应的逻辑
            return Result.error("201","文件创建者不存在");
        }
        Integer departId = userInfolog.getDepartId();
        Depart depart =  departService.getDepartById(departId);
        if (depart == null) {
            // 部门信息为空，处理对应的逻辑
            return Result.error("201","文件创建者部门不存在");
        }
        Integer id = depart.getId();

// 比较两个部门的 ID
        if (id1 == null || id == null || !id1.equals(id)) {
            // 部门 ID 不相等，拒绝访问，处理对应的逻辑
            return  Result.error("201","非本部门文件禁止开放");
        } else {

            // 部门 ID 相等，通过验证，处理对应的逻辑
            //需要判断文件记录是不是已经通过了校核和未发布
            Integer approvalStatus = document.getApprovalStatus();
            if (approvalStatus != null && approvalStatus == 1){
                Integer publishStatus = document.getPublishStatus();
                if (publishStatus != null && publishStatus == 0){
                    //设置文件的开放状态为开放
                    //document.setPublishStatus(1);
                    return documentService.publishUpdate(document);
                }else{
                    return Result.error("201","文件已开放，禁止再次开放");
                }
            }else{
                return Result.error("201","文件未校核，请先对文件进行校核");
            }


        }


    }

    @PostMapping("/verifyPass")
    public Result<?> verifyPass(@RequestBody Document document,HttpServletRequest request) {
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"), "username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        if (userByUsername == null) {
            // 用户不存在，处理对应的逻辑
            return Result.error("201","审核人员用户信息出错");
        }

    // 查询审核人员的身份，根据审核人员的身份获取他所在的部门
        Depart depart1 = userByUsername.getDepart();
        if (depart1 == null) {
            return Result.error("201","审核人员部门不存在");
        }
        Integer id1 = depart1.getId();

    // 获取文件创建者的身份
        Integer userId = document.getUserId();
        if (userId == null) {
            // 文件创建者的 ID 为空，处理对应的逻辑
            return Result.error("201","文件创建者信息出错");
        }

    // 根据文件创建者的 ID 查询他所在的部门
        User userInfolog = userService.getUserInfolog(userId);
        if (userInfolog == null) {
            // 文件创建者不存在，处理对应的逻辑
            return Result.error("201","文件创建者不存在");
        }
        Integer departId = userInfolog.getDepartId();
        Depart depart =  departService.getDepartById(departId);
        if (depart == null) {
            // 部门信息为空，处理对应的逻辑
            return Result.error("201","文件创建者部门不存在");
        }
        Integer id = depart.getId();

// 比较两个部门的 ID
        if (id1 == null || id == null || !id1.equals(id)) {
            // 部门 ID 不相等，拒绝访问，处理对应的逻辑
            return  Result.error("201","非本部门文件禁止校核");
        } else {
            // 部门 ID 相等，通过验证，处理对应的逻辑
            Integer approvalStatus = document.getApprovalStatus();
            if (approvalStatus != null && approvalStatus == 0){

                return documentService.verifyPass(document);
            }else{
                return Result.error("201","文件已审核，请勿再次审核");
            }


        }


    }

    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/updatestatus")
    public Result<?> updatestatus(@RequestBody Document document) {
        return documentService.updatestatus(document);

    }
    @PostMapping("/updateDeletedstatus")
    public Result<?> updateDeletedstatus(@RequestBody Document document) {
        return documentService.updateDeletedstatus(document);

    }



    /**
     * @description: 更新时候的用户验证 仅单个文件手动更新时使用
     * @param document:
     * @param request:
     * @return com.example.demo.common.Result<?>
     * @author: Zheng
     * @date: 2023/7/3 15:42
     */

    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/updateconfirm")
    public Result<?> updateconfirm(@RequestBody Document document,HttpServletRequest request) {

        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        Integer userId = document.getUserId();
        //判断一下是不是能从token中获取用户信息
        if (userByUsername == null || userByUsername.getId() == null ){
            //如果获取用户信息失败 说明无法获取操作人的用户信息
            return Result.error("201", "获取用户参数失败");
        }
        if (userByUsername.getId().equals(userId)) {
            return Result.success();
        } else {
            return Result.error("201", "非本人资源,禁止更新操作");
        }
    }

    // 删除单个图纸信息 需要验证删除时间是否在一天之内
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @DeleteMapping("{id}/{createTime}")
    public Result<?> deleteDocument(@PathVariable(value = "id") String  id,
                                    @PathVariable("createTime") String createTime,
                                    HttpServletRequest request) {

        //首先判断操作者得身份 如果事管理员得身份 则他可以删除创建人是本部门任意人的文件

        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        String roleKey = userByUsername.getRole().getRoleKey();
        Integer id1 = userByUsername.getDepart().getId();
        
        //如果操作者的身份是部门管理员则没有时间限制但是要验证是否为本部门文件
        if ("department_admin".equals(roleKey)){
            Result<?> documentById = documentService.getDocumentById(Integer.parseInt(id));
            Object data = documentById.getData();
            if (data instanceof Document) {
                Document document = (Document) data;
                //查出操作者的部门id和部门管理员的部门id作比较
                Integer userId = document.getUserId();
                Result<?> userInfoById = userService.getUserInfoById(userId);
                Object data1 = userInfoById.getData();
                User user1 = (User) data1;
                Integer departId = user1.getDepartId();
                //操作者的部门id和部门管理员的部门id相同则允许删除
                if (departId == id1){
                    //部门管理员用户仅被允许删除 再删除允许时间之内的文件 即未开放的文件
                    Integer publishStatus = document.getPublishStatus();
                    if (publishStatus != null && publishStatus == 0){
                        return documentService.deleteDocument(Integer.parseInt(id));
                    }else {
                        return  Result.error("201","文件已开放，禁止删除");
                    }

                }else{
                    return Result.error("201","非本部门文件，禁止删除");
                }
                
            }else{
                return Result.error("201","系统错误");
            }
        } else if ("superuser".equals(roleKey)) {
            //如果用户的身份是超级用户 则没有任何限制 直接就可以删除
            return documentService.deleteDocument(Integer.parseInt(id));
        } else {


            //// 将字符串转换为 LocalDateTime 对象
            //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            //LocalDateTime targetTime = LocalDateTime.parse(createTime, formatter);
            //
            //// 获取当前时间
            //LocalDateTime currentTime = LocalDateTime.now();
            //
            //// 计算时间差
            //long daysBetween = ChronoUnit.DAYS.between(targetTime, currentTime);
            //
            //// 判断时间差是否大于一天
            //if (daysBetween > 1) {
            //    return Result.error("201", "创建时间超过一天,禁止删除");
            //}else {
                Result<?> documentById = documentService.getDocumentById(Integer.parseInt(id));
                Object data = documentById.getData();
                if (data instanceof Document) {
                    Document document = (Document) data;
                    //根据文件记录id获取该条记录的创建者的id
                    Integer userId = document.getUserId();


                    if (userByUsername != null && userByUsername.getId() != null && userByUsername.getId().equals(userId)) {
                        Integer publishStatus = document.getPublishStatus();
                        Integer approvalStatus = document.getApprovalStatus();
                        //普通的设计人员也是仅被允许删除 删除允许时间之内的文件 即未开放的文件
                        if (publishStatus != null && publishStatus == 0){
                            if (publishStatus != null && publishStatus == 0){
                                return documentService.deleteDocument(Integer.parseInt(id));
                            }else {
                                return  Result.error("201","文件已开放，禁止删除");
                            }
                        }else{
                            return  Result.error("201","文件已被管理员审核，请联系部门管理员删除");
                        }

                    } else {
                        return Result.error("201", "非本人资源,禁止删除");
                    }
                } else {
                    // handle error here...
                    return Result.error("201", "系统出错 删除失败");
                }
            //}
        }


    }
    // 删除多个图纸 管理员用户 批量删除操作
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/verifyBatch")
    public Result<?> verifyBatch(@RequestBody List<Integer> ids,HttpServletRequest request) {
        //从携带的token中获取当前用户的信息
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        if (userByUsername == null) {
            // 用户不存在，处理对应的逻辑
            return Result.error("201","审核人员用户信息出错");
        }

        // 查询审核人员的身份，根据审核人员的身份获取他所在的部门
        Depart depart1 = userByUsername.getDepart();
        if (depart1 == null) {
            return Result.error("201","审核人员部门不存在");
        }
        Integer id1 = depart1.getId();

        //设置两个列表 一个列表用来存放校核失败的记录的id 一个列表用来存放校核成功的记录中文件存储路径
        List<Integer> failedIds = new ArrayList<>();
        List<String> successPaths = new ArrayList<>();


        int successCount = 0;

        //对传入的ids 进行循环
        for (Integer id : ids) {
            Result<?> documentById = documentService.getDocumentById(id);
            Object data = documentById.getData();
            if (data != null && data.getClass().equals(Document.class)) {
                Document document = (Document) data;
                // 获取文件创建者的身份
                Integer userId = document.getUserId();
                if (userId == null) {
                    // 文件创建者的 ID 为空，处理对应的逻辑
                    //return Result.error("201","文件创建者信息出错");
                    failedIds.add(id);
                }

                // 根据文件创建者的 ID 查询他所在的部门
                User userInfolog = userService.getUserInfolog(userId);
                if (userInfolog == null) {
                    // 文件创建者不存在，处理对应的逻辑
                    //return Result.error("201","文件创建者不存在");
                    failedIds.add(id);
                }
                Integer departId = userInfolog.getDepartId();
                Depart depart =  departService.getDepartById(departId);
                if (depart == null) {
                    // 部门信息为空，处理对应的逻辑
                    //return Result.error("201","文件创建者部门不存在");
                    failedIds.add(id);
                }
                Integer id2 = depart.getId();

                //判断一下文件创建者的部门id和审核人员的部门id是否一直
                if (id2.equals(id1)){
                    //如果一致则执行校核操作
                    successPaths.add(document.getDocumentPath());
                    Integer integer = documentService.verifyPassById(id);
                    if (integer == 0){
                        //更新失败
                        failedIds.add(id);
                    }
                    successCount++;

                }else{
                    //如果不一致则加入失败列表
                    failedIds.add(id);
                }
            }else {
                //只有那些可以删除 但是仅仅因为用户和创建人不一致得时候才加入失败得列表
                failedIds.add(id);
            }

        }

        int failedCount = ids.size() - successCount;
        if (failedCount == 0) {
            //如果校验成功了了 则回复全不删除了
            //全部删除成功
            return Result.success();
        } else if (failedCount == ids.size()) {
            //全部校验失败 则返回失败的id列表
            String errorMessage = String.format("校核失败：%s", failedIds.toString());
            return Result.error("201", errorMessage);
        } else {
            String Message = String.format("校核成功：%d 条记录,校核失败：%d条记录", successCount,failedCount);
            return Result.success("101",Message, null);
        }
    }


    // 删除多个图纸 管理员用户 批量删除操作
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer"})
    @PostMapping("/deleteBatch")
    public Result<?> deleteDocumentMore(@RequestBody List<Integer> ids,HttpServletRequest request) {
        //从携带的token中获取当前用户的信息
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        //判断一下是不是能从token中获取用户信息
        if (userByUsername == null || userByUsername.getId() == null ){
            //如果获取用户信息失败 说明无法获取操作人的用户信息
            return Result.error("201", "获取用户参数失败");
        }
        //设置两个列表 一个列表用来存放删除失败的记录的id 一个列表用来存放删除成功的记录中文件存储路径
        List<Integer> failedIds = new ArrayList<>();
        List<String> successPaths = new ArrayList<>();


        int successCount = 0;

        //对传入的ids 进行循环
        for (Integer id : ids) {
            Result<?> documentById = documentService.getDocumentById(id);
            Object data = documentById.getData();
            if (data != null && data.getClass().equals(Document.class)) {
                Document document = (Document) data;
                if (userByUsername != null && document.getUserId().equals(userByUsername.getId())){
                    //如果操作人和文件的创建人一致 则删除文件并且把文件的的文件路径保存起来
                    successPaths.add(document.getDocumentPath());
                    Result<?> result = documentService.deleteDocument(id);
                    successCount++;

                }else{
                    //只有那些可以删除 但是仅仅因为用户和创建人不一致得时候才加入失败得列表
                    failedIds.add(id);
                }
            }else {
                //只有那些可以删除 但是仅仅因为用户和创建人不一致得时候才加入失败得列表
                failedIds.add(id);
            }

        }

        int failedCount = ids.size() - successCount;
        if (failedCount == 0) {
            //如果全部能全部删除成功了 也要把这些能删除成功的id对应的文件路径用列表返回
            //全部删除成功
            return Result.success("0","全部删除成功",successPaths);
        } else if (failedCount == ids.size()) {
            //全部删除失败
            String errorMessage = String.format("删除失败：%s", failedIds.toString());
            return Result.error("201", errorMessage);
        } else {
            String Message = String.format("删除成功：%d 条记录,删除失败：%d条记录", successCount,failedCount);
            return Result.success("101",Message, successPaths);
        }
    }



    // 查询图纸信息列表
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer","regular_user"})
    @PostMapping("{index}/{size}")
    public Result<?> findDocumentList(@PathVariable(value = "index") Integer index,
                          @PathVariable(value = "size") Integer size,
                          @RequestBody(required = true) DocumentQueryParam documentQueryParam,
                                      HttpServletRequest request) {
        //首先获取判断用户的角色 如果是普通用户则只能看到审批通过的图纸 其他用户则可以看到所有图纸信息
        //从携带的token中获取当前用户的信息
        String usernameFromToken = JwtUtils.getClaim(request.getHeader("token"),"username");
        User userByUsername = userService.getUserByUsername(usernameFromToken);
        //判断一下是不是能从token中获取用户信息
        if (userByUsername == null || userByUsername.getId() == null ){
            //如果获取用户信息失败 说明无法获取操作人的用户信息
            return Result.error("201", "获取用户参数失败");
        }else{

            //验证操作用户所属的部门 如果用户所在的部门属于两个事业部 则应该先验证对应的事业部
            //如果操作用户所属的部门 不属于两个事业部 则只需要验证用户的身份就可以了
            Integer parentId = userByUsername.getDepart().getParentId();
            Depart departByparentId = departService.getDepartById(parentId);
            String departKey = departByparentId.getDepartKey();
            //如果查出来的用户的部门的父部门是物联网事业部或者新能源事业部 则需要把这个父id parentId设置到查询的条件类中
            if ("IOTB".equals(departKey) || "NETB".equals(departKey)){
                documentQueryParam.setDepartId(parentId);
            }



            //查询用户的角色
            String roleKey = userByUsername.getRole().getRoleKey();

            //如果这个用户是一个非设计部门的人员 包括两个事业部的非设计人员和其他综合部门的非设计人员 必须设置它们只能看审核完并且开发的文件
            if ("regular_user".equals(roleKey)){
                //如果用户是普通用户设置查询实体中的approvalStatus的值为1 否则则设置该字段为null
                documentQueryParam.setApprovalStatus(1);
                documentQueryParam.setPublishStatus(1);
            }else{
                documentQueryParam.setApprovalStatus(null);
            }
        }
        

        Result<?> result=  documentService.findDocumentList(index, size, documentQueryParam);
        System.out.println("this is pull test code...");
        return result;
    }


    //获取历史版本的文件列表
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer","regular_user"})
    @PostMapping("/historyList/{index}/{size}")
    public Result<?> findHistoryList(@PathVariable(value = "index") Integer index,
                                     @PathVariable(value = "size") Integer size,
                                     @RequestBody(required = true) Document document) {

        Result<?> result=  documentService.findHistoryList(index, size, document);
        System.out.println("this is pull test code...");
        return result;
    }



    // 根据图纸标号编号查询用户信息 预览用
    //@RequiresRoles(logical = Logical.OR, value = {"superuser", "designer","regular_user"})
    @GetMapping("/getId/{id}")
    public Result<?> getDocumentInfo(@PathVariable(value = "id") Integer id) {
        return documentService.getDocumentById(id);
    }


    //根据料号查询 MES接口
    @GetMapping("/getDocumentPath")
    public Result<?> getDocumentPath(@RequestParam String itemNo,@RequestParam List<Integer> documentTypeList) {
        Result<?> filePaths = documentService.getFilePath(itemNo,documentTypeList);
        return filePaths;
    }

    //根据部门查询一周的创建的文件的数量
    @GetMapping("/getOneWeekInfoList/{departId}")
    public Result<?> getOneWeekInfoList(@PathVariable(value = "departId") Integer departId) {
        LocalDate currentDate = LocalDate.now();
        LocalDate oneWeekAgo = currentDate.minusDays(6);

        List<OneWeekCreateCount> oneWeekCreateCountList = new ArrayList<>();

        LocalDate localDate = oneWeekAgo;
        while (localDate.isBefore(currentDate.plusDays(1))) {
            int value = documentService.selectByOneDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),departId);
            oneWeekCreateCountList.add(new OneWeekCreateCount(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), value));
            localDate = localDate.plusDays(1);
        }

        return Result.success(oneWeekCreateCountList) ;
    }

    //根据部门查询一周的创建的文件的数量
    @GetMapping("/getLastWeekPublishList/{departId}")
    public Result<?> getLastWeekPublishList(@PathVariable(value = "departId") Integer departId) {
        LocalDate currentDate = LocalDate.now();
        LocalDate oneWeekAgo = currentDate.minusDays(6);

        List<OneWeekCreateCount> oneWeekPublishCountList = new ArrayList<>();

        LocalDate localDate = oneWeekAgo;
        while (localDate.isBefore(currentDate.plusDays(1))) {
            int value = documentService.selectPublishByOneDate(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),departId);
            oneWeekPublishCountList.add(new OneWeekCreateCount(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()), value));
            localDate = localDate.plusDays(1);
        }

        return Result.success(oneWeekPublishCountList) ;
    }

    //根据各种类型文件的数量
    @GetMapping("/getTypeDistribution")
    public Result<?> getTypeDistribution() {
        return Result.success( documentService.selectTypeDistribution()) ;
    }

}
