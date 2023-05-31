package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
@TableName("[dbo].[Cust_ERP_MES_ItemMaster_View]")
@Data
public class ItemMaster {
    @TableField("ID")
    private Long id;
    @TableField("Org")
    private Long org;
    @TableField("Code")
    private String code;
    @TableField("Name")
    private String name;
    @TableField("SPECS")
    private String specs;
    @TableField("CreatedBy")
    private String createdBy;
    @TableField("CreatedOn")
    private Date createdOn;
    @TableField("ModifiedBy")
    private String modifiedBy;
    @TableField("ModifiedOn")
    private Date modifiedOn;
    @TableField("Effective_IsEffective")
    private Boolean effective;
    @TableField("MainItemCategory")
    private Long mainItemCategoryId;
    @TableField("State")
    private Integer state;
    @TableField("ItemFormAttribute")
    private String itemFormAttribute;
    @TableField("InventoryUOM")
    private Long inventoryUom;
    @TableField("Warehouse")
    private Long warehouseId;
    @TableField("Bin")
    private Long binId;

    // getter and setter methods
    // ...

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Boolean getEffective() {
        return effective;
    }

    public void setEffective(Boolean effective) {
        this.effective = effective;
    }

    public Long getMainItemCategoryId() {
        return mainItemCategoryId;
    }

    public void setMainItemCategoryId(Long mainItemCategoryId) {
        this.mainItemCategoryId = mainItemCategoryId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getItemFormAttribute() {
        return itemFormAttribute;
    }

    public void setItemFormAttribute(String itemFormAttribute) {
        this.itemFormAttribute = itemFormAttribute;
    }

    public Long getInventoryUom() {
        return inventoryUom;
    }

    public void setInventoryUom(Long inventoryUom) {
        this.inventoryUom = inventoryUom;
    }

    public Long getOrg() {
        return org;
    }

    public void setOrg(Long org) {
        this.org = org;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getBinId() {
        return binId;
    }

    public void setBinId(Long binId) {
        this.binId = binId;
    }

    // toString method
    @Override
    public String toString() {
        return "ItemMaster{" +
                "id=" + id +
                ", orgId=" + org +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", specs='" + specs + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", createdOn=" + createdOn +
                ", modifiedBy='" + modifiedBy + '\'' +
                ", modifiedOn=" + modifiedOn +
                ", effective=" + effective +
                ", mainItemCategoryId=" + mainItemCategoryId +
                ", state=" + state +
                ", itemFormAttribute='" + itemFormAttribute + '\'' +
                ", inventoryUom=" + inventoryUom +
                ", warehouseId=" + warehouseId +
                ", binId=" + binId +
                '}';
    }
}