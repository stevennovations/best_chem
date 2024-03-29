/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delivery;

import java.math.RoundingMode;
import java.text.NumberFormat;

/**
 *
 * @author Steven
 */
public class DRItemViewModel {
    
    private int dritemid;
    private int idsoitem;
    private String sku;
    private Integer inventory_id;
    private String skudesc;
    private int ordrqty;
    private String ordrqty1;
    private int deliveryqty;
    private String deliveryqty1;
    private int qtyremaining;
    private String qtyremaining1;
    private int soh;
    private String soh1;
    private String uom;
    
    private final NumberFormat nf2= NumberFormat.getInstance();
    
    public DRItemViewModel(Integer id){
        this.inventory_id = id;
        
        nf2.setMaximumFractionDigits(0);
        nf2.setMinimumFractionDigits(0);
        nf2.setRoundingMode(RoundingMode.HALF_EVEN);
    }

    /**
     * @return the sku
     */
    public String getSku() {
        return sku;
    }

    /**
     * @param sku the sku to set
     */
    public void setSku(String sku) {
        this.sku = sku;
    }

    /**
     * @return the skudesc
     */
    public String getSkudesc() {
        return skudesc;
    }

    /**
     * @param skudesc the skudesc to set
     */
    public void setSkudesc(String skudesc) {
        this.skudesc = skudesc;
    }

    /**
     * @return the ordrqty
     */
    public int getOrdrqty() {
        return ordrqty;
    }

    /**
     * @param ordrqty the ordrqty to set
     */
    public void setOrdrqty(int ordrqty) {
        this.ordrqty = ordrqty;
    }

    /**
     * @return the deliveryqty
     */
    public int getDeliveryqty() {
        return deliveryqty;
    }

    /**
     * @param deliveryqty the deliveryqty to set
     */
    public void setDeliveryqty(int deliveryqty) {
        this.deliveryqty = deliveryqty;
    }

    /**
     * @return the qtyremaining
     */
    public int getQtyremaining() {
        return qtyremaining;
    }

    /**
     * @param qtyremaining the qtyremaining to set
     */
    public void setQtyremaining(int qtyremaining) {
        this.qtyremaining = qtyremaining;
    }

    /**
     * @return the uom
     */
    public String getUom() {
        return uom;
    }

    /**
     * @param uom the uom to set
     */
    public void setUom(String uom) {
        this.uom = uom;
    }

    /**
     * @return the idsoitem
     */
    public int getIdsoitem() {
        return idsoitem;
    }

    /**
     * @param idsoitem the idsoitem to set
     */
    public void setIdsoitem(int idsoitem) {
        this.idsoitem = idsoitem;
    }

    /**
     * @return the inventory_id
     */
    public int getInventory_id() {
        return inventory_id;
    }
    
    public Integer getId() {
        return inventory_id;
    }

    /**
     * @param inventory_id the inventory_id to set
     */
    public void setInventory_id(int inventory_id) {
        this.inventory_id = inventory_id;
    }

    /**
     * @return the dritemid
     */
    public int getDritemid() {
        return dritemid;
    }

    /**
     * @param dritemid the dritemid to set
     */
    public void setDritemid(int dritemid) {
        this.dritemid = dritemid;
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DRItemViewModel)) {
            return false;
        }
        DRItemViewModel other = (DRItemViewModel) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId()))) {
            return false;
        }
        return true;
    }

    /**
     * @return the soh
     */
    public int getSoh() {
        return soh;
    }

    /**
     * @param soh the soh to set
     */
    public void setSoh(int soh) {
        this.soh = soh;
    }

    /**
     * @return the ordrqty1
     */
    public String getOrdrqty1() {
        return ordrqty1;
    }

    public void setOrdrqty1() {
        this.ordrqty1 = this.nf2.format(this.ordrqty);
    }

    /**
     * @return the deliveryqty1
     */
    public String getDeliveryqty1() {
        return deliveryqty1;
    }

    public void setDeliveryqty1() {
        this.deliveryqty1 = this.nf2.format(this.deliveryqty);
    }

    /**
     * @return the qtyremaining1
     */
    public String getQtyremaining1() {
        return qtyremaining1;
    }

    public void setQtyremaining1() {
        this.qtyremaining1 = this.nf2.format(this.qtyremaining);
    }

    /**
     * @return the soh1
     */
    public String getSoh1() {
        return soh1;
    }

    /**
     */
    public void setSoh1() {
        this.soh1 = this.nf2.format(this.soh);
    }
    
}
