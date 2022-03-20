package com.lixin.fruit.dao;

import com.lixin.fruit.pojo.Fruit;
import java.util.List;

public interface FruitDAO {

    //查询水果库存列表
    List<Fruit> getFruitList();

    //新增库存
    boolean addFruit(Fruit fruit);

    //修改库存
    boolean updateFruit(Fruit fruit);

    //根据水果名称查询特定库存
    Fruit getFruitByFname(String fname);

    //删除特定水果库存记录
    boolean delFruit(String fname);

}