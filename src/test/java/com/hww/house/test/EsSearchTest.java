package com.hww.house.test;

import com.hww.house.HouseElkApplicationTests;
import com.hww.house.base.es.RentSearch;
import com.hww.house.base.service.response.BaseServiceResponse;
import com.hww.house.service.EsSearchService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author: heweiwei@hztianque.com
 * @Date: 2019/12/15
 * @Time: 18:25
 * Description:
 */
public class EsSearchTest extends HouseElkApplicationTests {
    @Autowired
    private EsSearchService esSearchService;


    @Test
    public void testQuery() {
        RentSearch rentSearch = new RentSearch();
        rentSearch.setCityEnName("bj");
        rentSearch.setStart(0);
        rentSearch.setSize(3);
        BaseServiceResponse<Long> baseServiceResponse = esSearchService.userEsQuery(rentSearch);
        System.out.println(baseServiceResponse.getTotal());
        baseServiceResponse.getResult().forEach(item -> {
            System.out.println("item = " + item);
        });
    }

    @Override
    public void test() {

    }
}
