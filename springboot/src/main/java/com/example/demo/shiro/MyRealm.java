package com.example.demo.shiro;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.demo.entity.User;
import com.example.demo.util.JwtUtils;
import com.example.demo.service.UserService;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @program: smalabel-backend
 * @description:
 * @author: 孙靖淳
 * @create: 2021-10-26 17:52
 **/
@Component
public class MyRealm extends AuthorizingRealm {

    @Autowired
    private UserService userService;

    //    //根据token判断此Authenticator是否使用该realm
    //    //必须重写不然shiro会报错
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    /**
     * 只有当检测用户需要权限或者需要判定角色的时候才会走
     * */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("MyRealm doGetAuthorizationInfo() 方法授权 ");
        String token = principalCollection.toString();
        String username = JwtUtils.getClaim(token,"username");
        if (StringUtils.isBlank(username)) {
            throw new AuthenticationException("token认证失败");
        }
        //查询当前
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        //其实这里应该是查询当前用户的角色或者权限去的,意思就是将当前用户设置一个svip和vip角色
        //权限设置一级权限和耳机权限 正常来说应该是去读取数据库只添加当前用户的角色权限的
        //info.addRole("vip");
        //info.addRole("svip");
        //info.addStringPermission("一级权限");
        //info.addStringPermission("二级权限");
        //User user=userService.getUserByUsername(username);
        ////查询数据库来获取用户的角色
        //info.addRole(user.getRole());
        ////查询数据库来获取用户的权限
        //info.addStringPermission(user.getPermission());
        User user = userService.getUserByUsername(username);
        // 查询数据库来获取用户的角色
        String roleName = null;
        if (user.getRole() == 1) {
            roleName = "admin";
        } else if (user.getRole() == 2) {
            roleName = "designer";
        } else if (user.getRole() == 3) {
            roleName = "worker";
        }
        if (roleName != null) {
            info.addRole(roleName);
        }

        // 查询数据库来获取用户的权限
        String permissionName = user.getPermission();
        if (permissionName != null && !permissionName.isEmpty()) {
            info.addStringPermission(permissionName);
        }
        System.out.println("方法结束咯-------》》》");

        return info;
    }

    /**
     * 默认使用此方法进行用户名正确与否验证, 如果没有权限注解的话就不会去走上面的方法只会走这个方法
     * 其实就是 过滤器传过来的token 然后进行 验证 authenticationToken.toString() 获取的就是
     * 你的token字符串,然后你在里面做逻辑验证就好了,没通过的话直接抛出异常就可以了
     * */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("认证-----------》》》》");
        System.out.println("1.toString ------>>> " + authenticationToken.toString());
        System.out.println("2.getCredentials ------>>> " + authenticationToken.getCredentials().toString());
        System.out.println("3. -------------》》 " +authenticationToken.getPrincipal().toString());
        String jwt = (String) authenticationToken.getCredentials();
        String username= null;
        //decode时候出错，可能是token的长度和规定好的不一样了
        try {
            username= JwtUtils.getClaim(jwt,"username");
        }catch (Exception e){
            throw new AuthenticationException("token非法，不是规范的token，可能被篡改了，或者过期了");
        }
        if (!JwtUtils.verify(jwt)||username==null){
            throw new AuthenticationException("token认证失效，token错误或者过期，重新登陆");
        }
        User user=userService.getUserByUsername(username);
        if (user==null){
            throw new AuthenticationException("该用户不存在");
        }

//        if (!JwtUtils.verify(jwt)) {
//            throw new AuthenticationException("Token认证失败");
//        }

        return new SimpleAuthenticationInfo(jwt, jwt, "MyRealm");
    }
}

