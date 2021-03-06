package com.homeward.website.bean.PO;

import lombok.Data;

import java.io.Serializable;

@Data
public class ItemPreview implements Serializable {
    private String id;
    private String groupName;
    private String imageRegular;
    private String imageHover;
    private Integer amount;
    private Integer price;
    private Boolean discount;
    private Integer discountPercent;
}
