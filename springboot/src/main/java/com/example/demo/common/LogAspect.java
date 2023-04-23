package com.example.demo.common;

import com.alibaba.fastjson.JSONObject;

//import iot.sixiang.license.jwt.UserUtils;
import com.example.demo.controller.TokenController;
import com.example.demo.controller.UserController;
import com.example.demo.entity.SysOperLog;
import com.example.demo.entity.User;
import com.example.demo.service.SysOperLogService ;
import com.example.demo.service.UserService;
import com.example.demo.utils.ThreadLocalUtils;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Aspect
@Component
@Slf4j
public class LogAspect {
    /**
     * 该Service及其实现类相关代码请自行实现，只是一个简单的插入数据库操作
     */
    @Autowired
    private SysOperLogService sysOperLogService;
    @Resource
    UserService userService;
    @Value("${jwt.secret}")
    private String secret;
    /**
     * @annotation(MyLog类的路径) 在idea中，右键自定义的MyLog类-> 点击Copy Reference
     */
    @Pointcut("@annotation(com.example.demo.common.MyLog)")
    public void logPointCut() {
        log.info("------>配置织入点");
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "logPointCut()")
    public void doAfterReturning(JoinPoint joinPoint) {
        handleLog(joinPoint, null);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e);
    }

    private void handleLog(final JoinPoint joinPoint, final Exception e) {

        // 获得MyLog注解
        MyLog controllerLog = getAnnotationLog(joinPoint);
        if (controllerLog == null) {
            return;
        }
        SysOperLog operLog = new SysOperLog();
        // 操作状态（0正常 1异常）
        operLog.setStatus(0);

        //获取请求地址
        HttpServletRequest request = getHttpServletRequest();
        operLog.setUri(request.getServletPath());

        // 在 MyLog 切面中打印当前线程 ID 和存储的 userId 值
        System.out.println("Thread ID: " + Thread.currentThread().getId());
        //登录成功之后userId就已经存入了线程缓存
        //if (ThreadLocalUtils.getCache("userId") != null){
        //
        //    Integer userId = (Integer)ThreadLocalUtils.getCache("userId");
        //    operLog.setOperCreator(userService.getUserInfolog(userId).getUsername());
        //    operLog.setOperId(userService.getUserInfolog(userId).getId());
        //}else {
        //    operLog.setOperCreator("null");
        //    operLog.setOperId(-1);
        //}
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        operLog.setOperCreator(userService.getUserInfolog(userId).getUsername());
        operLog.setOperId(userService.getUserInfolog(userId).getId());
        //operLog.setOperCreator("null");
        //operLog.setOperId(-1);
        //获取请求头中的token
        //String token = request.getHeader("token"); // 获取请求头中的token字段
        //if (token != null){
        //    Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        //}else{
        //    System.out.println("token为空");
        //}

        // 操作时间
        operLog.setOperTime(new Date());
        if (e != null) {
            operLog.setStatus(1);
            // IotLicenseException为本系统自定义的异常类，读者若要获取异常信息，请根据自身情况变通
            operLog.setErrorMsg(e.toString());
        }

        // UserUtils.getUri();获取方法上的路径 如：/login，本文实现方法如下：
        // 1、在拦截器中 String uri = request.getRequestURI();
        // 2、用ThreadLocal存放uri，UserUtils.setUri(uri);
        // 3、UserUtils.getUri();
        //String uri = "";
        //operLog.setUri(uri);
        // 处理注解上的参数
        getControllerMethodDescription(joinPoint, controllerLog, operLog);
        // 保存数据库
        sysOperLogService.insertSysLog(operLog.getTitle(), operLog.getBusinessType(), operLog.getUri(), operLog.getStatus(), operLog.getOperParam(), operLog.getErrorMsg(), operLog.getOperTime(), operLog.getOperCreator(),operLog.getOperId());
    }
    /**
     * @Description: 获取request
     */
    public HttpServletRequest getHttpServletRequest(){
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = (ServletRequestAttributes)ra;
        HttpServletRequest request = sra.getRequest();
        return request;
    }

    /**
     * 是否存在注解，如果存在就获取，不存在则返回null
     * @param joinPoint
     * @return
     */
    private MyLog getAnnotationLog(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        if (method != null) {
            return method.getAnnotation(MyLog.class);
        }
        return null;
    }

    /**
     * 获取Controller层上MyLog注解中对方法的描述信息
     * @param joinPoint 切点
     * @param myLog 自定义的注解
     * @param operLog 操作日志实体类
     */
    private void getControllerMethodDescription(JoinPoint joinPoint, MyLog myLog, SysOperLog operLog) {
        // 设置业务类型（0其它 1新增 2修改 3删除）
        operLog.setBusinessType(myLog.businessType().ordinal());
        // 设置模块标题，eg:登录
        operLog.setTitle(myLog.title());
        // 对方法上的参数进行处理，处理完：userName=xxx,password=xxx
        String operParam = getAnnotationValue(joinPoint, myLog.operParam());
        operLog.setOperParam(operParam);

    }

    /**
     * 对方法上的参数进行处理
     * @param joinPoint
     * @param name
     * @return
     */
    private String getAnnotationValue(JoinPoint joinPoint, String name) {
        String paramName = name;
        // 获取方法中所有的参数
        Map<String, Object> params = getParams(joinPoint);
        // 参数是否是动态的:#{paramName}
        if (paramName.matches("^#\\{\\D*\\}")) {
            // 获取参数名,去掉#{ }
            paramName = paramName.replace("#{", "").replace("}", "");
            // 是否是复杂的参数类型:对象.参数名
            if (paramName.contains(".")) {
                String[] split = paramName.split("\\.");
                // 获取方法中对象的内容
                Object object = getValue(params, split[0]);
                // 转换为JsonObject
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(object);
                // 获取值
                //Object o = jsonObject.get(split[1]);
                return String.valueOf(jsonObject);
            } else {// 简单的动态参数直接返回
                StringBuilder str = new StringBuilder();
                String[] paraNames = paramName.split(",");
                for (String paraName : paraNames) {

                    String val = String.valueOf(getValue(params, paraName));
                    // 组装成 userName=xxx,password=xxx,
                    str.append(paraName).append("=").append(val).append(",");
                }
                // 去掉末尾的,
                if (str.toString().endsWith(",")) {
                    String substring = str.substring(0, str.length() - 1);
                    return substring;
                } else {
                    return str.toString();
                }
            }
        }
        // 非动态参数直接返回
        return name;
    }

    /**
     * 获取方法上的所有参数，返回Map类型, eg: 键："userName",值:xxx  键："password",值:xxx
     * @param joinPoint
     * @return
     */
    public Map<String, Object> getParams(JoinPoint joinPoint) {
        Map<String, Object> params = new HashMap<>(8);
        // 通过切点获取方法所有参数值["zhangsan", "123456"]
        Object[] args = joinPoint.getArgs();
        // 通过切点获取方法所有参数名 eg:["userName", "password"]
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] names = signature.getParameterNames();
        for (int i = 0; i < args.length; i++) {
            params.put(names[i], args[i]);
        }
        return params;
    }

    /**
     * 从map中获取键为paramName的值，不存在放回null
     * @param map
     * @param paramName
     * @return
     */
    private Object getValue(Map<String, Object> map, String paramName) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey().equals(paramName)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
