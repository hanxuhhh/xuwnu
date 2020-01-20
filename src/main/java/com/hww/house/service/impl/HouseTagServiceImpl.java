package com.hww.house.service.impl;

import com.hww.house.base.service.response.ServiceResponse;
import com.hww.house.entity.House;
import com.hww.house.entity.HouseTag;
import com.hww.house.exception.HouseException;
import com.hww.house.mapper.HouseTagMapper;
import com.hww.house.service.HouseService;
import com.hww.house.service.HouseTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/6
 * @Time: 17:50
 * Description:
 */
@Service
public class HouseTagServiceImpl implements HouseTagService {

    @Autowired
    private HouseTagMapper houseTagMapper;

    @Autowired
    private HouseService houseService;

    @Override
    public List<HouseTag> getHouseTagByHouseId(long houseId) {
        return houseTagMapper.getHouseTagByHouseId(houseId);
    }

    @Override
    public void save(List<HouseTag> houseTags) {
        houseTagMapper.saveHouseTags(houseTags);
    }

    @Override
    public ServiceResponse deleteTagsByHouseIdAndTag(Long id, String tag) {
        House house = houseService.getHouseById(id);
        if (house == null) {
            throw new HouseException("房屋不存在");
        }

        HouseTag houseTag = houseTagMapper.getHouseTagByNameAndHouseId(id, tag);
        if (houseTag == null) {
            throw new HouseException("标签不存在");
        }
        houseTagMapper.deleteTagsById(houseTag.getId());
        return new ServiceResponse(true, null, null);
    }

    /**
     * 单个添加房屋标签
     * @param houseId
     * @param tag
     * @return
     */
    @Override
    @Transactional
    public ServiceResponse addTag(Long houseId, String tag) {
        House house = houseService.getHouseById(houseId);
        if (house == null) {
            throw new HouseException("房屋不存在");
        }

        HouseTag houseTag = houseTagMapper.getHouseTagByNameAndHouseId(houseId,tag);
        if (houseTag != null) {
            throw new HouseException("标签已存在");
        }

        houseTagMapper.saveOne(new HouseTag(null,houseId, tag));
        return new ServiceResponse(true, null, null);


    }

    @Override
    public List<HouseTag> findAllByHouseIdIn(List<Long> houseIds) {
        return houseTagMapper.findAllByHouseIdIn(houseIds);
    }


}
