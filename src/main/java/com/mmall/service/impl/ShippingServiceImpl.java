package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;


    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);  // 避免横向越权
        int count = shippingMapper.insert(shipping);
        if (count > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());  // 直接返回shippingId给前端
            return ServerResponse.createBySuccess("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }


    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);   // 避免横向越权
        int count = shippingMapper.updateByShipping(shipping);
        if (count > 0){
            Map result = Maps.newHashMap();
            result.put("shippingId", shipping.getId());  // 直接返回shippingId给前端
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }


    public ServerResponse<Shipping> search(Integer userId, Integer shippingId){
       Shipping shipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);
       if (shipping == null){
           return ServerResponse.createByErrorMessage("查询不到该地址");
       }
       return ServerResponse.createBySuccess(shipping);
    }


    public ServerResponse<String> delete(Integer userId, Integer shippingId){
        // 注意横向越权，不能删掉不是这个用户的shippingId记录。 所以不能直接用deleteByPrimaryKey
        int count = shippingMapper.deleteByShippingIdAndUserId(userId,shippingId);
        if (count > 0){
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createBySuccess("删除地址失败");
    }

    /**
     * 显示当前用户的所有地址信息
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
