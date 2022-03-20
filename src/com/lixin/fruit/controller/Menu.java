package com.lixin.fruit.controller;

import com.lixin.fruit.dao.FruitDAO;
import com.lixin.fruit.dao.impl.FruitDAOImpl;
import com.lixin.fruit.pojo.Fruit;
import java.util.List;
import java.util.Scanner;

public class Menu {

    Scanner input = new Scanner(System.in);
    FruitDAO fruitDAO = new FruitDAOImpl();

    //显示主菜单,返回一个客户选择的数字
    public int showMainMenu() {
        System.out.println("=================欢迎使用水果库存系统=================");
        System.out.println("1.查看水果库存列表");
        System.out.println("2.添加水果库存信息");
        System.out.println("3.查看特定水果库存信息");
        System.out.println("4.水果下架");
        System.out.println("5.退出");
        System.out.println("====================================================");
        System.out.print("请选择:");
        return input.nextInt();
    }

    //查看水果库存列表
    public void showFruitList() {
        List<Fruit> fruitList = fruitDAO.getFruitList();
        System.out.println("----------------------------------------------------");
        System.out.println("编号\t\t名称\t\t单价\t\t库存\t\t备注");
        if (fruitList == null || fruitList.size() <= 0) {
            System.out.println("对不起,库存为空!");
        } else {
            for (Fruit fruit : fruitList) {
                System.out.println(fruit);
            }
        }
        System.out.println("----------------------------------------------------");
    }

    //添加水果库存信息-业务方法
    public void addFruit() {
        System.out.print("请输入水果名称:");
        String fname = input.next();
        Fruit fruit = fruitDAO.getFruitByFname(fname);
        if (fruit == null) {//说明库存中没有这个名称的水果->添加
            System.out.print("请输入水果单价:");
            int price = input.nextInt();
            System.out.print("请输入水果库存量:");
            int fcount = input.nextInt();
            System.out.print("请输入水果备注:");
            String remark = input.next();
            //封装成一个新的fruit对象
            fruit = new Fruit(3, fname, price, fcount, remark);
            //调用DAO的添加方法
            fruitDAO.addFruit(fruit);
        } else {//说明库存中有这个名称的水果->修改
            System.out.print("请输入追加的库存量:");
            int fcount = input.nextInt();
            fruit.setFcount(fruit.getFcount() + fcount);
            //调用DAO的修改方法
            fruitDAO.updateFruit(fruit);
        }
        System.out.println("添加成功!");
    }

    //查看指定水果库存信息
    public void showFruitInfo() {
        System.out.print("请输入水果名称:");
        String fname = input.next();
        Fruit fruit = fruitDAO.getFruitByFname(fname);
        if (fruit == null) {
            System.out.println("对不起,没有找到指定水果的库存信息!");
        } else {
            System.out.println("----------------------------------------------------");
            System.out.println("编号\t\t名称\t\t单价\t\t库存\t\t备注");
            System.out.println(fruit);
            System.out.println("----------------------------------------------------");
        }
    }

    //水果下架
    public void delFruit() {
        System.out.print("请输入水果名称:");
        String fname = input.next();
        Fruit fruit = fruitDAO.getFruitByFname(fname);
        if (fruit == null) {
            System.out.println("对不起,没有找到需要下架的水果信息!");
        } else {
            System.out.print("是否确认下架?(Y/N)");
            String slt = input.next();
            if ("Y".equalsIgnoreCase(slt)) {
                fruitDAO.delFruit(fname);
                System.out.println("下架成功!");
            }
        }
    }

    //退出
    public boolean exit() {
        System.out.print("是否确认退出?(Y/N)");
        String slt = input.next();
        return !slt.equalsIgnoreCase("Y");
    }

}