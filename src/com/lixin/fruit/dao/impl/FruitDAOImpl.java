package com.lixin.fruit.dao.impl;

import com.lixin.fruit.dao.FruitDAO;
import com.lixin.fruit.dao.base.BaseDAO;
import com.lixin.fruit.pojo.Fruit;
import java.util.List;

public class FruitDAOImpl extends BaseDAO<Fruit> implements FruitDAO {

    public FruitDAOImpl() {
        super();
    }

    //查询水果库存列表
    @Override
    public List<Fruit> getFruitList() {
        String sql = "select * from fruit";
        return super.executeQuery(sql);
    }

    //新增库存
    @Override
    public boolean addFruit(Fruit fruit) {
        //这里在fruitdb数据库中设置了主键fid自增
        String sql = "insert into fruit values(0, ?, ?, ?, ?)";
        int count = super.executeUpdate(sql,
                //fruit.getFid(),
                fruit.getFname(),
                fruit.getPrice(),
                fruit.getFcount(),
                fruit.getRemark());
        //System.out.println(count);//自增键的值
        return count > 0;
    }

    //修改库存
    @Override
    public boolean updateFruit(Fruit fruit) {
        String sql = "update fruit set fcount = ? where fid = ?";
        return super.executeUpdate(sql,
                fruit.getFcount(),
                fruit.getFid()) > 0;
    }

    //根据水果名称查询特定库存
    @Override
    public Fruit getFruitByFname(String fname) {
        String sql = "select * from fruit where fname like ?";
        return super.load(sql, fname);
    }

    //删除特定水果库存记录
    @Override
    public boolean delFruit(String fname) {
        String sql = "delete from fruit where fname like ?";
        return super.executeUpdate(sql, fname) > 0;
    }

}