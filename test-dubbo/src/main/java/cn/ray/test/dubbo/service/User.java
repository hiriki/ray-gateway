package cn.ray.test.dubbo.service;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Ray
 * @date 2023/12/15 22:49
 * @description
 */
@Data
public class User implements Serializable {

    private static final long serialVersionUID = -7651482540472411412L;

    private int id;

    private String name;
}
